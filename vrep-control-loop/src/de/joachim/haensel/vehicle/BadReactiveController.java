package de.joachim.haensel.vehicle;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.vehiclecontrol.reactive.ControllerStates;
import de.joachim.haensel.vehiclecontrol.reactive.pid.PIDController;

public class BadReactiveController implements ILowLevelController
{
    private static final int SEGMENT_BUFFER_SIZE = 40;
    private Position2D _expectedTarget;
    private IActuatingSensing _actuatorsSensors;
    private DefaultReactiveControllerStateMachine _stateMachine;
    private PIDController _steeringPIDController;
    private Deque<Trajectory> _segmentBuffer;
    private ITrajectoryProvider _segmentProvider;

    public class DefaultReactiveControllerStateMachine
    {
        private ControllerStates _currentState;
        
        public DefaultReactiveControllerStateMachine()
        {
            _currentState = ControllerStates.IDLE;
        }
        
        public void set(ControllerStates newState)
        {
            _currentState = newState;
        }

        public void driveTo(Position2D target)
        {
            if(_currentState == ControllerStates.IDLE)
            {
                _expectedTarget = target;
                _currentState = ControllerStates.DRIVE_TO;
            }
        }

        public ControllerStates getState()
        {
            return _currentState;
        }

        public void arrivedAtTarget()
        {
            if(_currentState == ControllerStates.DRIVE_TO)
            {
                _currentState = ControllerStates.IDLE;
            }
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
        Position2D currentPosition;
        if(_stateMachine.getState() == ControllerStates.DRIVE_TO)
        {
            currentPosition = _actuatorsSensors.getPosition();
            
            if(currentPosition.equals(_expectedTarget, 0.2))
            {
                _stateMachine.arrivedAtTarget();
                return;
            }
            
            ensureValidBuffer();
            float targetWheelRotation = computeTargetWheelRotationSpeed();
            float targetSteeringAngle = computeTargetSteeringAngle();
            _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
        }
    }

    private void ensureValidBuffer()
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
