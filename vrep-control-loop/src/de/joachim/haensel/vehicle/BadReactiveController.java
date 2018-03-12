package de.joachim.haensel.vehicle;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerMsg;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerStates;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.vehicle.BadReactiveController.DriveToAction;
import de.joachim.haensel.vehiclecontrol.reactive.pid.PIDController;

public class BadReactiveController implements ILowLevelController
{
    public class DriveToAction implements Consumer<BadReactiveController>
    {
        @Override
        public void accept(BadReactiveController controller)
        {
            //TODO Called whenever sensor data is available while driving
        }
    }

    private static final int SEGMENT_BUFFER_SIZE = 40;
    private Position2D _expectedTarget;
    private IActuatingSensing _actuatorsSensors;
    private DefaultReactiveControllerStateMachine _stateMachine;
    private PIDController _steeringPIDController;
    private Deque<Trajectory> _segmentBuffer;
    private ITrajectoryProvider _segmentProvider;

    public class DefaultReactiveControllerStateMachine extends FiniteStateMachineTemplate
    {
        public DefaultReactiveControllerStateMachine()
        {
            Consumer<Position2D> driveToAction = target -> _expectedTarget = target; 
            createTransition(ControllerStates.IDLE, ControllerMsg.DRIVE_TO, ControllerStates.DRIVING, driveToAction);
            createTransition(ControllerStates.DRIVING, ControllerMsg.ARRIVED_AT_TARGET, ControllerStates.IDLE, null);
            Consumer<BadReactiveController> driveAction = new DriveToAction();
            createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, ControllerStates.DRIVING, driveAction );
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
    
    public BadReactiveController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
    {
        _actuatorsSensors = actuatorsSensors;
        _stateMachine = new DefaultReactiveControllerStateMachine();
        _steeringPIDController = new PIDController(0.1, 0.001, 2.8);
        _segmentBuffer = new LinkedList<>();
        _segmentProvider = trajectoryProvider;
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

    private void driveToAction()
    {
        Position2D currentPosition = _actuatorsSensors.getPosition();
        
        if(currentPosition.equals(_expectedTarget, 0.2))
        {
            _stateMachine.arrivedAtTarget();
            return;
        }
        
        ensureBufferSize();
        
        float targetWheelRotation = computeTargetWheelRotationSpeed();
        float targetSteeringAngle = computeTargetSteeringAngle();
        _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
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
        Position2D position = _actuatorsSensors.getPosition();
        Position2D rearWheelPosition = _actuatorsSensors.getRearWheelCenterPosition();
        
        float cte = computeCrossTrackError(position, rearWheelPosition, _expectedTarget);
        return 0.0f;
    }

    private float computeCrossTrackError(Position2D position, Position2D rearWheelPosition, Position2D expectedTarget)
    {
        float cte = 0.0f;
        
        return cte ;
    }

    protected float computeTargetWheelRotationSpeed()
    {
        return 10.0f;
    }
}
