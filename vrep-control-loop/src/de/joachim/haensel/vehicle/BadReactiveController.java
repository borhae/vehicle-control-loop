package de.joachim.haensel.vehicle;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import de.joachim.haensel.phd.scenario.math.Triangle;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerMsg;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerStates;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Guard;
import de.joachim.haensel.sumo2vrep.Position2D;

public class BadReactiveController implements ILowLevelController
{
    private static final int SEGMENT_BUFFER_SIZE = 40;
    private Position2D _expectedTarget;
    private IActuatingSensing _actuatorsSensors;
    private DefaultReactiveControllerStateMachine _stateMachine;
    private Deque<Trajectory> _segmentBuffer;
    private ITrajectoryProvider _segmentProvider;
    private Trajectory _currentSegment;
    private double _lookahead;

    public class DefaultReactiveControllerStateMachine extends FiniteStateMachineTemplate
    {
        public DefaultReactiveControllerStateMachine()
        {
            Consumer<Position2D> driveToAction = target -> _expectedTarget = target; 
            Consumer<BadReactiveController> driveAction = controller -> controller.driveAction();

            createTransition(ControllerStates.IDLE, ControllerMsg.DRIVE_TO, null, ControllerStates.DRIVING, driveToAction);
            Guard arrivedAtTargetGuard = () -> _actuatorsSensors.getPosition().equals(_expectedTarget, 0.2);
            Guard notArrivedGuard = () -> !arrivedAtTargetGuard.isTrue();
            createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, arrivedAtTargetGuard, ControllerStates.IDLE, null);
            createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, notArrivedGuard, ControllerStates.DRIVING, driveAction);
            
            setInitialState(ControllerStates.IDLE);
            reset();
        }

        public void driveTo(Position2D target)
        {
            transition(ControllerMsg.DRIVE_TO, target);
        }

        public void controlEvent(BadReactiveController badReactiveController)
        {
            transition(ControllerMsg.CONTROL_EVENT, badReactiveController);
        }
    }
    
    public BadReactiveController()
    {
    }

    @Override
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
    {
        _actuatorsSensors = actuatorsSensors;
        _stateMachine = new DefaultReactiveControllerStateMachine();
        _segmentBuffer = new LinkedList<>();
        _segmentProvider = trajectoryProvider;
        _currentSegment = null;
        _lookahead = 5.0;
    }

    @Override
    public void driveTo(Position2D target)
    {
        _stateMachine.driveTo(target);
    }

    @Override
    public void controlEvent()
    {
        _stateMachine.controlEvent(this);
    }

    private void driveAction()
    {
        ensureBufferSize();
        chooseCurrentSegment();
        float targetWheelRotation = computeTargetWheelRotationSpeed();
        float targetSteeringAngle = computeTargetSteeringAngle();
        _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
    }

    private void chooseCurrentSegment()
    {
        _currentSegment = _segmentBuffer.peek();
        //TODO define correct current segment:
        // still the topmost?
        // when to change?
    }

    private void ensureBufferSize()
    {
        if(_segmentBuffer.size() < 20)
        {
            int segmentRequestSize = SEGMENT_BUFFER_SIZE - _segmentBuffer.size();
            List<Trajectory> trajectories = _segmentProvider.getNewSegments(segmentRequestSize);
            trajectories.stream().forEach(traj -> _segmentBuffer.add(traj));
        }
    }

    protected float computeTargetSteeringAngle()
    {
        //TODO last action was here
        Position2D rearWheelPosition = _actuatorsSensors.getRearWheelCenterPosition();
        Vector2D currentSegment = _currentSegment.getVector();
        
        Vector2D rearWheelToLookAhead = cmputeRearWheelToLookaheadVector(rearWheelPosition, currentSegment);
        double alpha = Vector2D.computeAngle(rearWheelToLookAhead, currentSegment);
        
        
        double delta = Math.atan( (2.0 *_actuatorsSensors.getVehicleLength() * Math.sin(alpha)) / (rearWheelToLookAhead.length()) );
        
        return (float)delta;
    }

    private Vector2D cmputeRearWheelToLookaheadVector(Position2D rearWheelPosition, Vector2D currentSegment)
    {
        //TODO handle multiple solutions: pick the one looking forward :)
        return Vector2D.circleIntersection(currentSegment, rearWheelPosition, _lookahead).get(0);
    }

    protected float computeTargetWheelRotationSpeed()
    {
        return 10.0f;
    }
}
