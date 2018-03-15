package de.joachim.haensel.vehicle;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerMsg;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerStates;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
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
            createTransition(ControllerStates.DRIVING, ControllerMsg.ARRIVED_AT_TARGET, null, ControllerStates.IDLE, null);
            createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, null, ControllerStates.DRIVING, driveAction);
            
            setInitialState(ControllerStates.IDLE);
            reset();
        }

        public void driveTo(Position2D target)
        {
            transition(ControllerMsg.DRIVE_TO, target);
        }

        public void arrivedAtTarget()
        {
            transition(ControllerMsg.ARRIVED_AT_TARGET, null);
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
        Position2D currentPosition = _actuatorsSensors.getPosition();
        
        if(currentPosition.equals(_expectedTarget, 0.2))
        {
            _stateMachine.arrivedAtTarget();
            return;
        }
        
        ensureBufferSize();
        chooseCurrentSegment();
        float targetWheelRotation = computeTargetWheelRotationSpeed();
        float targetSteeringAngle = computeTargetSteeringAngle();
        _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
    }

    private void chooseCurrentSegment()
    {
        _currentSegment = _segmentBuffer.peek();
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
        Position2D rearWheelPosition = _actuatorsSensors.getRearWheelCenterPosition();
        
        
        return 0.0f;
    }

    protected float computeTargetWheelRotationSpeed()
    {
        return 10.0f;
    }
}
