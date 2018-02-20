package de.joachim.haensel.vehicle;

import de.joachim.haensel.vehiclecontrol.base.Position2D;
import de.joachim.haensel.vehiclecontrol.reactive.ControllerStates;
import de.joachim.haensel.vehiclecontrol.reactive.pid.PIDController;

public class BadReactiveController implements ILowLevelController
{
    private Position2D _expectedTarget;
    private IActuatingSensing _actuatorsSensors;
    private DefaultReactiveControllerStateMachine _stateMachine;
    private PIDController _steeringPIDController;

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
    
    public BadReactiveController(IActuatingSensing actuatorsSensors)
    {
        _actuatorsSensors = actuatorsSensors;
        _stateMachine = new DefaultReactiveControllerStateMachine();
        _steeringPIDController = new PIDController(0.1, 0.001, 2.8);
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
            System.out.println("should drive");
            currentPosition = _actuatorsSensors.getPosition();
            
            if(currentPosition.equals(_expectedTarget, 0.2))
            {
                _stateMachine.arrivedAtTarget();
                return;
            }
            System.out.println("haven't reached target");
            float targetWheelRotation = computeTargetWheelRotationSpeed();
            float targetSteeringAngle = computeTargetSteeringAngle();
            _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
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
