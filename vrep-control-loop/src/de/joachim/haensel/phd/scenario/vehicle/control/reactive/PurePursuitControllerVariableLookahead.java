package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

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
    private static final double LOOKAHEAD_FACTOR = 1.0;
    private static final int MIN_DYNAMIC_LOOKAHEAD = 4;
    private static final int MAX_DYNAMIC_LOOKAHEAD = 35;
    private static final String CURRENT_SEGMENT_DEBUG_KEY = "curSeg";
    private static final int MIN_SEGMENT_BUFFER_SIZE = 15;
    private static final int SEGMENT_BUFFER_SIZE = 20;
    private Position2D _expectedTarget;
    private IActuatingSensing _actuatorsSensors;
    private DefaultReactiveControllerStateMachine _stateMachine;
    private LinkedList<TrajectoryElement> _segmentBuffer;
    private List<TrajectoryElement> _droppedSegmentCollector;
    private ITrajectoryProvider _segmentProvider;
    private TrajectoryElement _currentLookaheadSegment;
    private double _lookahead;
    private IVrepDrawing _vrepDrawing;
    private PurePursuitParameters _parameters;
    private boolean _debugging;
    private DebugParams _debugParams;
    private double _speedToWheelRotationFactor;
    private List<IArrivedListener> _arrivedListeners;
    private List<ITrajectoryRequestListener> _trajectoryRequestListeners;
    private List<ITrajectoryReportListener> _trajectoryReportListeners;

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
        _droppedSegmentCollector = new ArrayList<>();
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
        _lookahead = _parameters.getLookahead();
        _speedToWheelRotationFactor = _parameters.getSpeedToWheelRotationFactor();
    }

    @Override
    public void driveTo(Position2D target)
    {
        ensureBufferSize();
        _actuatorsSensors.computeAndLockSensorData();
        _speedToWheelRotationFactor = 2 / _actuatorsSensors.getWheelDiameter(); // 2/diameter = 1/radius
        Position2D currentPosition = _actuatorsSensors.getPosition();
        _currentLookaheadSegment = chooseClosestSegmentFromBuffer(currentPosition);
        _stateMachine.driveTo(target);
    }
    
    private TrajectoryElement chooseClosestSegmentFromBuffer(Position2D currentPosition)
    {
        double minDist = Double.POSITIVE_INFINITY;
        TrajectoryElement closestElem = _segmentBuffer.peek();
        for (TrajectoryElement curElem : _segmentBuffer)
        {
            double curDist = curElem.getVector().getBase().distance(currentPosition);
            if(curDist < minDist)
            {
                closestElem = curElem;
                minDist = curDist;
            }
        }
        return closestElem;
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
        chooseCurrentLookaheadSegment(_actuatorsSensors.getRearWheelCenterPosition(), _actuatorsSensors.getOrientation(), lookahead);
        TrajectoryElement closestSegment = chooseClosestSegment(_actuatorsSensors.getPosition());
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
            _debugParams.getSpeedometer().updateVelocities(_actuatorsSensors.getVehicleVelocity(), closestSegment.getVelocity());
            _debugParams.getSpeedometer().repaint();
        }
        _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
    }

    private TrajectoryElement chooseClosestSegment(Position2D position)
    {
        if(position == null || _droppedSegmentCollector.isEmpty())
        {
            //with no input we just rely on the other segment
            return _currentLookaheadSegment;
        }
        TrajectoryElement result = null;
        double minDist = Double.POSITIVE_INFINITY;
        int minDistIdx = Integer.MAX_VALUE;
        for(int idx = 0; idx < _droppedSegmentCollector.size(); idx++)
        {
            Position2D perpendicularIntersection = Vector2D.getPerpendicularIntersection(_droppedSegmentCollector.get(idx).getVector(), position);
            if(perpendicularIntersection != null)
            {
                double curDist = perpendicularIntersection.distance(position);
                if(curDist < minDist)
                {
                    minDist = curDist;
                    minDistIdx = idx;
                }
            }
        }
        if(minDistIdx != Integer.MAX_VALUE)
        {
            result = _droppedSegmentCollector.get(0);
            for(int cnt = 0; cnt < minDistIdx; cnt++)
            {
                if(_droppedSegmentCollector.isEmpty())
                {
                    break;
                }
                result = _droppedSegmentCollector.remove(0);
            }
        }
        else
        {
            for(int idx = 0; idx < _droppedSegmentCollector.size(); idx++)
            {
                Position2D roadPoint = _droppedSegmentCollector.get(idx).getVector().getBase();
                double curDist = roadPoint.distance(position);
                if(curDist < minDist)
                {
                    minDist = curDist;
                    minDistIdx = idx;
                }
            }
            if(minDistIdx != Integer.MAX_VALUE)
            {
                result = _droppedSegmentCollector.get(0);
                for(int cnt = 0; cnt < minDistIdx; cnt++)
                {
                    if(_droppedSegmentCollector.isEmpty())
                    {
                        break;
                    }
                    result = _droppedSegmentCollector.remove(0);
                }
            }
            else
            {
                result = _currentLookaheadSegment;
            }
        }
        return result;
    }

    private void chooseCurrentLookaheadSegment(Position2D currentPosition, Vector2D orientation, double lookahead)
    {
        if(_currentLookaheadSegment != null && isInRange(currentPosition, _currentLookaheadSegment.getVector(), lookahead))
        {
            // current segment still in range, we are good here
            return;
        }
        else
        {
            boolean segmentFound = false;
            int dropCount = 0;
            for (TrajectoryElement curTraj : _segmentBuffer)
            {
                dropCount++;
                if(isInRange(currentPosition, curTraj.getVector(), lookahead))
                {
                    segmentFound = true;
                    break;
                }
            }
            if(!segmentFound)
            {
                //we have no direction here...
//                System.out.println("No new segment. Lookahead:" +  _lookahead + ", Pos: " + currentPosition + ", buffer: " + _segmentBuffer);
                return;
            }
            else
            {
                while(dropCount > 0)
                {
                    _currentLookaheadSegment = _segmentBuffer.pop();
                    _droppedSegmentCollector.add(_currentLookaheadSegment);
                    dropCount--;
                }
            }
        }
    }

    private boolean isInRange(Position2D position, Vector2D vector, double requiredDistance)
    {
        double baseDist = Position2D.distance(vector.getBase(), position);
        double tipDist = Position2D.distance(vector.getTip(), position);
        return tipDist > requiredDistance && baseDist <= requiredDistance;
    }

    private void ensureBufferSize()
    {
        if(_segmentBuffer.size() < MIN_SEGMENT_BUFFER_SIZE)
        {
            int segmentRequestSize = SEGMENT_BUFFER_SIZE - _segmentBuffer.size();
            List<TrajectoryElement> trajectories = _segmentProvider.getNewSegments(segmentRequestSize);
            if(trajectories == null)
            {
                return;
            }
            trajectories.stream().forEach(traj -> _segmentBuffer.add(traj));
            notifyTrajectoryRequestListeners(_segmentBuffer);
//            notifyTrajectoryRequestListeners(trajectories);
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
