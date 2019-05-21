package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import java.util.function.Consumer;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerMsg;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerStates;
import de.joachim.haensel.statemachine.EmptyParam;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Guard;

public class EventLoopStateMachine extends FiniteStateMachineTemplate
{
    private static final double DISTANCE_TO_TARGET_THRESHOLD = 2.5;
    private IActuatingSensing _actuatorsSensors;
    private Position2D _target;

    
    public EventLoopStateMachine(IActuatingSensing actuatorsSensors, CarInterfaceActions carInterface, PurePuresuitTargetProvider targetProvider)
    {
        _actuatorsSensors = actuatorsSensors;
        Consumer<Position2D> updateTargetAndReinitCar = target -> {_target = target; carInterface.reInit();};

        Consumer<EmptyParam> driveAction = dummy -> { _actuatorsSensors.computeAndLockSensorData(); targetProvider.loopPrepare(); carInterface.driveLoopAction();};
        Consumer<EmptyParam> brakeAndStopAction = dummy -> carInterface.brakeAndStopAction();
        Consumer<EmptyParam> arrivedBrakeAndStopAction = dummy -> { _actuatorsSensors.computeAndLockSensorData(); carInterface.arrivedBrakeAndStopAction();};
        
        Guard arrivedAtTargetGuard = () -> arrivedAtTarget();
        Guard notArrivedGuard = () -> !arrivedAtTargetGuard.isTrue();

        createTransition(ControllerStates.IDLE, ControllerMsg.DRIVE_TO, TRUE_GUARD, ControllerStates.DRIVING, updateTargetAndReinitCar);
        
        createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, arrivedAtTargetGuard, ControllerStates.IDLE, arrivedBrakeAndStopAction);
        createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, notArrivedGuard, ControllerStates.DRIVING, driveAction);

        createTransition(ControllerStates.DRIVING, ControllerMsg.STOP, TRUE_GUARD, ControllerStates.IDLE, brakeAndStopAction);
   
        setInitialState(ControllerStates.IDLE);
        reset();
    }

    private boolean arrivedAtTarget()
    {
        Position2D curPos = _actuatorsSensors.getFrontWheelCenterPosition();
        double distance = Position2D.distance(curPos, _target);
        boolean arrived = distance < DISTANCE_TO_TARGET_THRESHOLD;
        return arrived;
    }
    
    public void triggerDriveToEvent(Position2D target)
    {
        transition(ControllerMsg.DRIVE_TO, target);
    }

    public void triggerControlEvent()
    {
        transition(ControllerMsg.CONTROL_EVENT, EMPTY_PARAMETER);
    }

    public void triggerStopEvent()
    {
        transition(ControllerMsg.STOP, EMPTY_PARAMETER);
    }
}
