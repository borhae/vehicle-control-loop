package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingType;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Guard;
import de.joachim.haensel.statemachine.States;

public class PurePursuitController implements ILowerLayerControl<PurePursuitParameters>
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
    //-0.25
    private double _speedToWheelRotationFactor;

    public class DefaultReactiveControllerStateMachine extends FiniteStateMachineTemplate
    {
        public DefaultReactiveControllerStateMachine()
        {
            Consumer<Position2D> driveToAction = target -> _expectedTarget = target; 
            Consumer<PurePursuitController> driveAction = controller -> controller.driveAction();
            Consumer<PurePursuitController> breakAndStopAction = controller -> controller.breakAndStopAction();
            

            createTransition(ControllerStates.IDLE, ControllerMsg.DRIVE_TO, null, ControllerStates.DRIVING, driveToAction);
            
            Guard arrivedAtTargetGuard = () -> arrivedAtTarget();
            Guard notArrivedGuard = () -> !arrivedAtTargetGuard.isTrue();
            
            createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, arrivedAtTargetGuard, ControllerStates.IDLE, breakAndStopAction);
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
            return distance < 2.0;
        }

        public void driveTo(Position2D target)
        {
            transition(ControllerMsg.DRIVE_TO, target);
        }

        public void controlEvent(PurePursuitController controller)
        {
            transition(ControllerMsg.CONTROL_EVENT, controller);
        }

        public void stop(PurePursuitController controller)
        {
            transition(ControllerMsg.STOP, controller);
        }
    }
    
    public PurePursuitController()
    {
        _debugging = false;
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
        _stateMachine.driveTo(target);
        ensureBufferSize();
        _actuatorsSensors.computeAndLockSensorData();
        Position2D currentPosition = _actuatorsSensors.getPosition();
        chooseCurrentSegment(currentPosition);
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

    public void breakAndStopAction()
    {
        _actuatorsSensors.drive(0.0f, 0.0f);
    }
    
    private void driveAction()
    {
        _actuatorsSensors.computeAndLockSensorData();
        ensureBufferSize();
        chooseCurrentSegment(_actuatorsSensors.getRearWheelCenterPosition());

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
        if(_debugging)
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
        if(_currentSegment != null && isInRange(currentPosition, _currentSegment.getVector(), _lookahead))
        {
            // current segment still in range, we are good here
            return;
        }
        else
        {
            boolean segmentFound = false;
            int dropCount = 0;
            for (Trajectory curTraj : _segmentBuffer)
            {
                dropCount++;
                if(isInRange(currentPosition, curTraj.getVector(), _lookahead))
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
                    _currentSegment = _segmentBuffer.pop();
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
        Vector2D rearWheelToLookAhead = computeRearWheelToLookaheadVector(rearWheelPosition, currentSegment);
        Vector2D rearWheelToFrontWheel = new Vector2D(rearWheelPosition, frontWheelPosition);
        if(rearWheelToLookAhead == null)
        {
            return 0.0f;
        }
        else
        {
            double alpha = Vector2D.computeAngle(rearWheelToLookAhead, rearWheelToFrontWheel) * rearWheelToLookAhead.side(rearWheelToFrontWheel) * -1.0;
            double delta = Math.atan( (2.0 *_actuatorsSensors.getVehicleLength() * Math.sin(alpha)) / (rearWheelToLookAhead.length()) );
            return (float) delta;
        }
    }

    private Vector2D computeRearWheelToLookaheadVector(Position2D rearWheelPosition, Vector2D currentSegment)
    {
        //TODO handle multiple solutions: pick the one looking forward :)
        List<Vector2D> circleIntersection = Vector2D.circleIntersection(currentSegment, rearWheelPosition, _lookahead);
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
}
