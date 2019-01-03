package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.sun.javafx.binding.StringFormatter;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingType;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Guard;
import de.joachim.haensel.statemachine.States;
import javafx.beans.binding.StringExpression;

/**
 * First go at the stanley controller. Does not work at all. Get back when I'll have the time for it (wildly oscillating).
 * @author dummy
 *
 */
public class StanleyController implements ILowerLayerControl<PurePursuitParameters>
{
    private static final String CURRENT_SEGMENT_DEBUG_KEY = "curSeg";
    private static final int MIN_SEGMENT_BUFFER_SIZE = 5;
    private static final int SEGMENT_BUFFER_SIZE = 10;
    private Position2D _expectedTarget;
    private IActuatingSensing _actuatorsSensors;
    private DefaultReactiveControllerStateMachine _stateMachine;
    private LinkedList<Trajectory> _segmentBuffer;
    private ITrajectoryProvider _segmentProvider;
    private Trajectory _currentSegment;
    private double _lookahead;
    private IVrepDrawing _vrepDrawing;
    private PurePursuitParameters _parameters;
    private boolean _debugging;
    private boolean _debuggingCircleAttached;
    private DebugParams _debugParams;
    private double _speedToWheelRotationFactor;
    private List<IArrivedListener> _arrivedListeners;
    private double _frontwheelCurrentSegmentDistance;

    public class DefaultReactiveControllerStateMachine extends FiniteStateMachineTemplate
    {
        public DefaultReactiveControllerStateMachine()
        {
            Consumer<Position2D> driveToAction = target -> _expectedTarget = target; 
            Consumer<StanleyController> driveAction = controller -> controller.driveAction();
            Consumer<StanleyController> breakAndStopAction = controller -> controller.breakAndStopAction();
            Consumer<StanleyController> arrivedBreakAndStopAction = controller -> controller.arrivedBreakAndStopAction();

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
            boolean arrived = distance < _lookahead;
//            System.out.printf("distance %.2f \n", distance);
            return arrived;
        }

        public void driveTo(Position2D target)
        {
            transition(ControllerMsg.DRIVE_TO, target);
        }

        public void controlEvent(StanleyController controller)
        {
            transition(ControllerMsg.CONTROL_EVENT, controller);
        }

        public void stop(StanleyController controller)
        {
            transition(ControllerMsg.STOP, controller);
        }
    }
    
    public StanleyController()
    {
        _debugging = false;
        _arrivedListeners = new ArrayList<>();
        _frontwheelCurrentSegmentDistance = 0.0;
    }

    @Override
    public void activateDebugging(IVrepDrawing vrepDrawing, DebugParams debugParams)
    {
        _debugging = true;
        _debuggingCircleAttached = false;
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
        _currentSegment = null;
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
        chooseCurrentSegment(currentPosition);
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
        _currentSegment = null;
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
        chooseCurrentSegment(_actuatorsSensors.getFrontWheelCenterPosition());

        float targetWheelRotation = 0.0f;
        float targetSteeringAngle = 0.0f;
        if(_debugging)
        {
            if(!_debuggingCircleAttached)
            {
                _vrepDrawing.attachDebugCircle(_lookahead);
                _debuggingCircleAttached = true;
            }
        }
        if(_currentSegment != null)
        {
            if(_debugging)
            {
                if(!_debuggingCircleAttached)
                {
                    _vrepDrawing.attachDebugCircle(_lookahead);
                    _debuggingCircleAttached = true;
                }
                Vector2D curSegVector = _currentSegment.getVector();
                double debugMarkerHeight = _debugParams.getSimulationDebugMarkerHeight();
                _vrepDrawing.updateLine(CURRENT_SEGMENT_DEBUG_KEY, curSegVector, debugMarkerHeight, Color.RED);
            }
            
            targetWheelRotation = computeTargetWheelRotationSpeed();
            targetSteeringAngle = computeTargetSteeringAngle();
        }
        if(_debugging && _debugParams.getSpeedometer() != null)
        {
            _debugParams.getSpeedometer().updateWheelRotationSpeed(targetWheelRotation);
            _debugParams.getSpeedometer().updateCurrentSegment(_currentSegment);
            _debugParams.getSpeedometer().updateActualVelocity(_actuatorsSensors.getVehicleVelocity());
            _debugParams.getSpeedometer().repaint();
        }
        _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
    }

    private void chooseCurrentSegment(Position2D currentPosition)
    {
        if(currentPosition == null)
        {
            return;
        }
        double minDist = Double.POSITIVE_INFINITY;
        int minDistIdx = Integer.MAX_VALUE;
        for(int idx = 0; idx < _segmentBuffer.size(); idx++)
        {
            Position2D perpendicularIntersection = Vector2D.getPerpendicularIntersection(_segmentBuffer.get(idx).getVector(), currentPosition);
            if(perpendicularIntersection != null)
            {
                double curDist = perpendicularIntersection.distance(currentPosition);
                if(curDist < minDist)
                {
                    minDist = curDist;
                    minDistIdx = idx;
                }
            }
        }
        if(minDistIdx != Integer.MAX_VALUE)
        {
            _currentSegment = _segmentBuffer.peek();
            for(int cnt = 0; cnt < minDistIdx; cnt++)
            {
                _currentSegment = _segmentBuffer.pop();
            }
            _frontwheelCurrentSegmentDistance = minDist;
        }
        else
        {
            for(int idx = 0; idx < _segmentBuffer.size(); idx++)
            {
                Position2D roadPoint = _segmentBuffer.get(idx).getVector().getBase();
                double curDist = roadPoint.distance(currentPosition);
                if(curDist < minDist)
                {
                    minDist = curDist;
                    minDistIdx = idx;
                }
            }
            if(minDistIdx != Integer.MAX_VALUE)
            {
                _currentSegment = _segmentBuffer.peek();
                for(int cnt = 0; cnt < minDistIdx; cnt++)
                {
                    _currentSegment = _segmentBuffer.pop();
                }
                _frontwheelCurrentSegmentDistance = Vector2D.getUnrangedPerpendicularIntersection(_currentSegment.getVector(), currentPosition).distance(currentPosition);
            }
            else
            {
                _currentSegment = _segmentBuffer.peek();
                _frontwheelCurrentSegmentDistance = 0.0;
//            NO SEGMENT FOUND! Do nothing
                System.out.println("panic, no segment found");
            }
        }
    }

    private void ensureBufferSize()
    {
        if(_segmentBuffer.size() < MIN_SEGMENT_BUFFER_SIZE)
        {
            int segmentRequestSize = SEGMENT_BUFFER_SIZE - _segmentBuffer.size();
            List<Trajectory> trajectories = _segmentProvider.getNewSegments(segmentRequestSize);
            if(trajectories == null)
            {
                return;
            }
            trajectories.stream().forEach(traj -> _segmentBuffer.add(traj));
        }
    }

    protected float computeTargetSteeringAngle()
    {
        Position2D rearWheelPosition = _actuatorsSensors.getRearWheelCenterPosition();
        Position2D frontWheelPosition = _actuatorsSensors.getFrontWheelCenterPosition();
        Vector2D currentSegment = _currentSegment.getVector();
        Vector2D rearWheelToFrontWheel = new Vector2D(rearWheelPosition, frontWheelPosition);

        double[] vehicleVelocityXYZ = _actuatorsSensors.getVehicleVelocity();
        double velocity = new Vector2D(0.0, 0.0, vehicleVelocityXYZ[0], vehicleVelocityXYZ[1]).getLength();
        velocity = velocity < 1 ? 1 : velocity;

        double theta_e = Vector2D.computeAngle(rearWheelToFrontWheel, currentSegment) * rearWheelToFrontWheel.side(currentSegment) * -1.0;
//        double theta_e =  Vector2D.computeSplitAngle(rearWheelToFrontWheel, currentSegment); // error angle
//        Position2D v1 = rearWheelToFrontWheel.getDir();
//        double theta = Math.atan2(v1.getY(), v1.getX()); //vehicle heading
//        Position2D v2 = currentSegment.getDir();
//        double theta_p = Math.atan2(v2.getY(), v2.getX()); //path heading
//        
//        double theta_e = theta - theta_p;
        
        double e_fa = _frontwheelCurrentSegmentDistance; // cross track error 
        
        double k = 8.0;
        double atan = Math.atan(k  * e_fa / velocity);
        double delta = theta_e + atan;
        System.out.println(String.format("theta_e: %.3f, e_fa: %.3f, atan: %.3f, delta: %.3f", theta_e, e_fa, atan, delta));
        return (float) delta;
    }

    protected float computeTargetWheelRotationSpeed()
    {
        double speed = _currentSegment.getVelocity();
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
        // TODO Auto-generated method stub
    }

    @Override
    public void addTrajectoryReportListener(ITrajectoryReportListener reportListener)
    {
        // TODO Auto-generated method stub
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
