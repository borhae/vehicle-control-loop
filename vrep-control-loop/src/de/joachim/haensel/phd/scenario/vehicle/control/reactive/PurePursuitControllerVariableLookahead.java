package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingType;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Guard;
import de.joachim.haensel.statemachine.States;

public class PurePursuitControllerVariableLookahead implements ILowerLayerControl<PurePursuitParameters>
{
    private static final double LOOKAHEAD_FACTOR = 2.0;
    private static final int MIN_DYNAMIC_LOOKAHEAD = 4;
    private static final int MAX_DYNAMIC_LOOKAHEAD = 50;
    private static final String CURRENT_SEGMENT_DEBUG_KEY = "curSeg";
    private static final int MIN_SEGMENT_BUFFER_SIZE = 15;
    private static final int SEGMENT_BUFFER_SIZE = 20;
    private Position2D _expectedTarget;
    private IActuatingSensing _actuatorsSensors;
    private DefaultReactiveControllerStateMachine _stateMachine;
    private List<TrajectoryElement> _segmentBuffer;
    private ITrajectoryProvider _segmentProvider;
    private TrajectoryElement _currentLookaheadSegment;
    private IVrepDrawing _vrepDrawing;
    private PurePursuitParameters _parameters;
    private boolean _debugging;
    private DebugParams _debugParams;
    private double _speedToWheelRotationFactor;
    private List<IArrivedListener> _arrivedListeners;
    private List<ITrajectoryRequestListener> _trajectoryRequestListeners;
    private List<ITrajectoryReportListener> _trajectoryReportListeners;
    //if the trajectory element provider (_segment provider) doesn't deliver enough trajectory elements this will be set to true
    private boolean _routeEnding;

    public class DefaultReactiveControllerStateMachine extends FiniteStateMachineTemplate
    {
        private static final double DISTANCE_TO_TARGET_THRESHOLD = 5.0;

        public DefaultReactiveControllerStateMachine()
        {
            Consumer<Position2D> driveToAction = target -> _expectedTarget = target; 
            Consumer<PurePursuitControllerVariableLookahead> driveAction = controller -> controller.driveAction();
            Consumer<PurePursuitControllerVariableLookahead> breakAndStopAction = controller -> controller.breakAndStopAction();
            Consumer<PurePursuitControllerVariableLookahead> arrivedBreakAndStopAction = controller -> controller.arrivedBreakAndStopAction();

            createTransition(ControllerStates.IDLE, ControllerMsg.DRIVE_TO, null, ControllerStates.DRIVING, driveToAction);
            
            Guard arrivedAtTargetGuard = () -> arrivedAtTarget();
            Guard notArrivedGuard = () -> !arrivedAtTargetGuard.isTrue();
            
            createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, arrivedAtTargetGuard, ControllerStates.IDLE, arrivedBreakAndStopAction);
            createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, notArrivedGuard, ControllerStates.DRIVING, driveAction);
            
            createTransition(ControllerStates.DRIVING, ControllerMsg.STOP, null, ControllerStates.IDLE, breakAndStopAction);
            
            setInitialState(ControllerStates.IDLE);
            reset();
        }

        private boolean arrivedAtTarget()
        {
            _actuatorsSensors.computeAndLockSensorData(); 
            Position2D curPos = _actuatorsSensors.getPosition();
            double distance = Position2D.distance(curPos, _expectedTarget);
            boolean arrived = distance < DISTANCE_TO_TARGET_THRESHOLD;
            return arrived;
        }

        public void driveTo(Position2D target)
        {
            transition(ControllerMsg.DRIVE_TO, target);
        }

        public void controlEvent(PurePursuitControllerVariableLookahead controller)
        {
            transition(ControllerMsg.CONTROL_EVENT, controller);
        }

        public void stop(PurePursuitControllerVariableLookahead controller)
        {
            transition(ControllerMsg.STOP, controller);
        }
    }
    
    public PurePursuitControllerVariableLookahead()
    {
        _debugging = false;
        _arrivedListeners = new ArrayList<>();
        _trajectoryRequestListeners = new ArrayList<>();
        _trajectoryReportListeners = new ArrayList<>();
    }

    @Override
    public void activateDebugging(IVrepDrawing vrepDrawing, DebugParams debugParams)
    {
        _debugging = true;
        _debugParams = debugParams;
        _vrepDrawing = vrepDrawing;
        _vrepDrawing.registerDrawingObject(CURRENT_SEGMENT_DEBUG_KEY, DrawingType.LINE, Color.RED);
    }
    
    @Override
    public void deactivateDebugging()
    {
        _vrepDrawing.removeAllDrawigObjects();
    }

    @Override
    public void setParameters(PurePursuitParameters parameters)
    {
        _parameters = parameters;
    }
    
    @Override
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
    {
        _actuatorsSensors = actuatorsSensors;
        _stateMachine = new DefaultReactiveControllerStateMachine();
        _segmentBuffer = new LinkedList<>();
        _segmentProvider = trajectoryProvider;
        _currentLookaheadSegment = null;
        _speedToWheelRotationFactor = _parameters.getSpeedToWheelRotationFactor();
    }

    @Override
    public void driveTo(Position2D target)
    {
        _routeEnding = false;
        ensureBufferSize();
        _actuatorsSensors.computeAndLockSensorData();
        _speedToWheelRotationFactor = 2 / _actuatorsSensors.getWheelDiameter(); // 2/diameter = 1/radius
        Position2D currentPosition = _actuatorsSensors.getPosition();
        _currentLookaheadSegment = chooseClosestSegmentFromBuffer(_actuatorsSensors.getOrientation(), currentPosition);
        _stateMachine.driveTo(target);
    }

    @Override
    public void stop()
    {
        _stateMachine.stop(this);
    }

    @Override
    public void controlEvent()
    {
        _stateMachine.controlEvent(this);
    }
    
    @Override
    public void clearSegmentBuffer()
    {
        _currentLookaheadSegment = null;
        _segmentBuffer.clear();
    }

    public void breakAndStopAction()
    {
        System.out.println("pure pursuit stoped");
        _actuatorsSensors.drive(0.0f, 0.0f);
    }
    
    public void arrivedBreakAndStopAction()
    {
        System.out.println("pure pursuit arrived");
        _actuatorsSensors.drive(0.0f, 0.0f);
        Position2D position = _actuatorsSensors.getFrontWheelCenterPosition();
        _arrivedListeners.forEach(listener -> listener.arrived(position));
        _arrivedListeners.clear();
    }
    
    private void driveAction()
    {
        _actuatorsSensors.computeAndLockSensorData();
        ensureBufferSize();

        double[] vehicleVelocityXYZ = _actuatorsSensors.getVehicleVelocity();
        double velocity = new Vector2D(0.0, 0.0, vehicleVelocityXYZ[0], vehicleVelocityXYZ[1]).getLength();
        double k = LOOKAHEAD_FACTOR;
        double kv = k * velocity;
        kv = kv > MAX_DYNAMIC_LOOKAHEAD ? MAX_DYNAMIC_LOOKAHEAD : kv;
        kv = kv < MIN_DYNAMIC_LOOKAHEAD ? MIN_DYNAMIC_LOOKAHEAD : kv;

        double lookahead = kv;
        chooseCurrentLookaheadSegment(_actuatorsSensors.getRearWheelCenterPosition(), _actuatorsSensors.getLockedOrientation(), lookahead);
        TrajectoryElement closestSegment = chooseClosestSegment(_actuatorsSensors.getPosition(), _actuatorsSensors.getLockedOrientation());
        
        float targetWheelRotation = 0.0f;
        float targetSteeringAngle = 0.0f;
        if(_currentLookaheadSegment != null)
        {
            if(_debugging)
            {
                Vector2D curSegVector = _currentLookaheadSegment.getVector();
                double debugMarkerHeight = _debugParams.getSimulationDebugMarkerHeight();
                _vrepDrawing.updateLine(CURRENT_SEGMENT_DEBUG_KEY, curSegVector, debugMarkerHeight, Color.RED);
            }
            
            targetWheelRotation = computeTargetWheelRotationSpeed(closestSegment);
            targetSteeringAngle = computeTargetSteeringAngle(lookahead);
        }
        if(_debugging && _debugParams.getSpeedometer() != null)
        {
            _debugParams.getSpeedometer().updateWheelRotationSpeed(targetWheelRotation);
            _debugParams.getSpeedometer().updateCurrentSegment(_currentLookaheadSegment);
            _debugParams.getSpeedometer().updateVelocities(_actuatorsSensors.getVehicleVelocity(), _actuatorsSensors.getLockedOrientation(), closestSegment.getVelocity());
            _debugParams.getSpeedometer().repaint();
        }
        _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
    }

    private TrajectoryElement chooseClosestSegment(Position2D position, Vector2D orientation)
    {
        TrajectoryElement result = null;
        if(position == null || _segmentBuffer.isEmpty())
        {
            //with no input we just rely on the other segment
            result = _currentLookaheadSegment;
        }
        else
        {
            double minDist = Double.POSITIVE_INFINITY;
            int minDistIdx = Integer.MAX_VALUE;
            for(int idx = 0; idx < _segmentBuffer.size(); idx++)
            {
                TrajectoryElement curElem = _segmentBuffer.get(idx);
                Vector2D curVector = curElem.getVector();
                if(Math.toDegrees(Vector2D.computeAngle(curVector, orientation)) < 120)
                {
                    double distance = curVector.toLine().distancePerpendicularOrEndpoints(position);
                    if(distance < minDist)
                    {
                        minDist = distance;
                        minDistIdx  = idx;
                        result = curElem;
                    }
                }
            }
          if(minDistIdx != 0 && minDistIdx != Integer.MAX_VALUE)
          {
              System.out.println(String.format("Buffer--------------------------------------------------minIDX: %d-------------", minDistIdx));
              _segmentBuffer.forEach(elem -> System.out.println(elem.getVector().toLine().toPyplotString("green")));
              System.out.println(position.toPyPlotString("red")); //current position
              System.out.println(_expectedTarget.toPyPlotString("blue")); //target position
              System.out.println(              "Buffer-------------------------------------------------------------------------");
              _segmentBuffer = _segmentBuffer.subList(minDistIdx, _segmentBuffer.size());
              System.out.println("Current Trajectory Element / Current orientation (pos)-------");
              System.out.println(result.getVector().toLine().toPyplotString("cyan"));
              System.out.println(orientation.toLine().toPyplotString());
              System.out.println("Cur Trj elem / Cur orient------------------------------------");
          }

        }
        return result;
    }
    
    private TrajectoryElement chooseClosestSegmentFromBuffer(Vector2D vehicleOrientation, Position2D currentPosition)
    {
        double minDist = Double.POSITIVE_INFINITY;
        List<TrajectoryElement> closeElements = new ArrayList<>();
        closeElements.add(_segmentBuffer.get(0));
        for (TrajectoryElement curElem : _segmentBuffer)
        {
            double curDist = curElem.getVector().getBase().distance(currentPosition);
            if(curDist < minDist)
            {
                closeElements.add(curElem);
                minDist = curDist;
            }
        }
        Collections.reverse(closeElements);
        int maxSurround = Math.min(10, closeElements.size());
        TrajectoryElement closestElement = closeElements.get(0);
        closeElements = closeElements.subList(0, maxSurround);
        
        TrajectoryElement rightDegreeElement = null;
        if(!closeElements.isEmpty())
        {
        //don't allow to choose a segment that is too much in the opposite direction
            List<TrajectoryElement> closestAlignedElements = closeElements.stream().filter(element -> Math.toDegrees(Vector2D.computeAngle(element.getVector(), vehicleOrientation)) < 140).collect(Collectors.toList());
            if(!closestAlignedElements.isEmpty())
            {
                rightDegreeElement = closestAlignedElements.get(0);
            }
        }
        closestElement =  rightDegreeElement == null ? closestElement : rightDegreeElement;
        return closestElement;
    }

    private void chooseCurrentLookaheadSegment(Position2D position, Vector2D orientation, double lookahead)
    {
        if(_currentLookaheadSegment == null || !isInRange(_currentLookaheadSegment, position, lookahead))
        {
            int bestMatchingSegmentIdx = 0;
            Map<TrajectoryElement, Integer> matchingElements = new HashMap<>();
            for(int idx = 0; idx < _segmentBuffer.size(); idx++)
            {
                TrajectoryElement curElement = _segmentBuffer.get(idx);
                if(isInRange(curElement, position, lookahead))
                {
                    matchingElements.put(curElement, idx);
                }
            }
            if(matchingElements.size() > 0)
            {
                List<Entry<TrajectoryElement, Integer>> matchingOrientation = matchingElements.entrySet().stream().filter(entry -> Math.toDegrees(Vector2D.computeAngle(entry.getKey().getVector(), orientation)) < 120).collect(Collectors.toList());
                if(matchingOrientation.size() > 0)
                {
                    bestMatchingSegmentIdx = matchingOrientation.get(0).getValue();
                }
                else
                {
                    List<Double> elementDegrees = matchingElements.entrySet().stream().map(entry -> Math.toDegrees(Vector2D.computeAngle(entry.getKey().getVector(), orientation))).collect(Collectors.toList());
                    System.out.println("Current lookahead: " + lookahead + "Warning: no trajectory element found matching our orientation close enough (120 degrees). Matching elements have these angle differences: " + elementDegrees);
                    
                    bestMatchingSegmentIdx = matchingElements.entrySet().iterator().next().getValue();
                }
                TrajectoryElement newLookaheadTrajectoryElement = _segmentBuffer.get(bestMatchingSegmentIdx);
                //don't go back to a former trajectory element (would be the case for new idx < old idx)
                if(newLookaheadTrajectoryElement.getIdx() > _currentLookaheadSegment.getIdx())
                {
                    _currentLookaheadSegment = newLookaheadTrajectoryElement;
                }
            }
            else
            {
                if(!_routeEnding)
                {
                    System.out.println("Warning: No matching trajectory element found, waiting for next buffer read");
//                    _segmentBuffer.clear(); // TODO this made sense actually. 
                    
                }
                //otherwise nothing needs to be done since it's the last trajectory element of the route
                System.out.println("no routes left");
            }
            System.out.println("lookahead: " + lookahead);
            double angle1 = 0.0;
            double angle2 = 2.0 * Math.PI;
            System.out.println(String.format("arc %f %f %f %f %f %f %f %f %f %f %f " + "left", 
                    position.getX(), position.getY(), lookahead, angle1, angle2, 
                    position.getX(), position.getY(), position.getX(), position.getY(), position.getX(), position.getY()));
            System.out.println(_currentLookaheadSegment.getVector().toLine().toPyplotString("orange"));
        }
    }

    // don't know which one is more resource intensive
//    private boolean isInRange(TrajectoryElement trajectoryElement, Position2D currentPosition, double lookahead)
//    {
//        return !Vector2D.circleIntersection(trajectoryElement.getVector(), currentPosition, lookahead).isEmpty();
//    }

    private boolean isInRange(TrajectoryElement trajectoryElement, Position2D position, double lookahead)
    {
        Vector2D vector = trajectoryElement.getVector();
        double baseDist = Position2D.distance(vector.getBase(), position);
        double tipDist = Position2D.distance(vector.getTip(), position);
        return tipDist >= lookahead && baseDist <= lookahead;
    }

    private void ensureBufferSize()
    {
        if(_segmentBuffer.size() < MIN_SEGMENT_BUFFER_SIZE)
        {
            int segmentRequestSize = SEGMENT_BUFFER_SIZE - _segmentBuffer.size();
            List<TrajectoryElement> trajectories = _segmentProvider.getNewSegments(segmentRequestSize);
            if(trajectories == null || trajectories.size() < segmentRequestSize)
            {
                _routeEnding = true;
            }
            trajectories.stream().forEach(traj -> _segmentBuffer.add(traj));
            notifyTrajectoryRequestListeners(_segmentBuffer);
        }
    }

    private void notifyTrajectoryRequestListeners(List<TrajectoryElement> trajectories)
    {
        List<TrajectoryElement> copy = new ArrayList<>();
        trajectories.forEach(t -> copy.add(t.deepCopy()));
        _trajectoryRequestListeners.stream().forEach(listener -> listener.notifyNewTrajectories(copy, _actuatorsSensors.getTimeStamp()));
    }

    protected float computeTargetSteeringAngle(double lookahead)
    {
        Position2D rearWheelPosition = _actuatorsSensors.getRearWheelCenterPosition();
        Position2D frontWheelPosition = _actuatorsSensors.getFrontWheelCenterPosition();
        Vector2D currentSegment = _currentLookaheadSegment.getVector();
        Vector2D rearWheelToLookAhead = computeRearWheelToLookaheadVector(rearWheelPosition, currentSegment, lookahead);
        Vector2D rearWheelToFrontWheel = new Vector2D(rearWheelPosition, frontWheelPosition);
        if(rearWheelToLookAhead == null)
        {
            return 0.0f;
        }
        else
        {
            double alpha = Vector2D.computeAngle(rearWheelToLookAhead, rearWheelToFrontWheel) * rearWheelToLookAhead.side(rearWheelToFrontWheel) * -1.0;
            double L = _actuatorsSensors.getVehicleLength();
            double[] vehicleVelocityXYZ = _actuatorsSensors.getVehicleVelocity();
            double velocity = new Vector2D(0.0, 0.0, vehicleVelocityXYZ[0], vehicleVelocityXYZ[1]).getLength();
            double k = LOOKAHEAD_FACTOR;
            double kv = k * velocity;
            kv = kv > MAX_DYNAMIC_LOOKAHEAD ? MAX_DYNAMIC_LOOKAHEAD : kv;
            kv = kv < MIN_DYNAMIC_LOOKAHEAD ? MIN_DYNAMIC_LOOKAHEAD : kv;
            double delta = Math.atan( (2.0 * L * Math.sin(alpha)) / kv);
            return (float) delta;
        }
    }

    private Vector2D computeRearWheelToLookaheadVector(Position2D rearWheelPosition, Vector2D currentSegment, double lookahead)
    {
        //TODO handle multiple solutions: pick the one looking forward :)
        List<Vector2D> circleIntersection = Vector2D.circleIntersection(currentSegment, rearWheelPosition, lookahead);
        if(circleIntersection == null || circleIntersection.isEmpty())
        {
            return null;
        }
        else
        {
            Vector2D vectorSegmentIntersection = circleIntersection.get(0);
            Vector2D result = new Vector2D(rearWheelPosition, vectorSegmentIntersection.getTip());
            return result;
        }
    }

    protected float computeTargetWheelRotationSpeed(TrajectoryElement closestSegment)
    {
        double speed = closestSegment.getVelocity();
        return (float) (speed * _speedToWheelRotationFactor);
    }

    @Override
    public String toString()
    {
        States currentState = _stateMachine.getCurrentState();
        return "state:" + currentState + ", target:" + _expectedTarget;
    }

    @Override
    public void addTrajectoryRequestListener(ITrajectoryRequestListener requestListener)
    {
        _trajectoryRequestListeners.add(requestListener);
    }

    @Override
    public void addTrajectoryReportListener(ITrajectoryReportListener reportListener)
    {
        _trajectoryReportListeners.add(reportListener);
    }
    @Override
    public void addArrivedListener(IArrivedListener arrivedListener)
    {
        _arrivedListeners.add(arrivedListener);
    }

    @Override
    public void clearArrivedListeners()
    {
        _arrivedListeners.clear();
    }
}
