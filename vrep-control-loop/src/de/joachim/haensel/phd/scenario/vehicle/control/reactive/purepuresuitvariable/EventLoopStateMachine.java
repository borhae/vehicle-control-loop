package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import java.util.function.Consumer;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerMsg;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerStates;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.statemachine.DebuggableGuard;
import de.joachim.haensel.statemachine.EmptyParam;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;

public class EventLoopStateMachine extends FiniteStateMachineTemplate
{
    private static final double DISTANCE_TO_TARGET_THRESHOLD = 3.0;
    private IActuatingSensing _actuatorsSensors;
    private Position2D _target;
    private CarInterfaceActions _carInterface;

    
    public EventLoopStateMachine(IActuatingSensing actuatorsSensors, CarInterfaceActions carInterface, PurePuresuitTargetProvider targetProvider)
    {
        _carInterface = carInterface;
        _actuatorsSensors = actuatorsSensors;
        Consumer<Position2D> updateTargetAndReinitCar = target -> {_target = target; carInterface.reInit();};

        Consumer<EmptyParam> driveAction = dummy -> { _actuatorsSensors.computeAndLockSensorData(); targetProvider.loopPrepare(); carInterface.driveLoopAction();};
        Consumer<EmptyParam> brakeAndStopAction = dummy -> carInterface.brakeAndStopAction();
        Consumer<EmptyParam> arrivedBrakeAndStopAction = dummy -> { _actuatorsSensors.computeAndLockSensorData(); carInterface.arrivedBrakeAndStopAction();};
        Consumer<Position2D> informFailedAction = newTarget -> informFailed(newTarget);
        
//        Guard notArrivedGuard = () -> {return !arrivedAtTarget() && !lostTrack();};
//        Guard lostTrack = () -> {return !arrivedAtTarget() && lostTrack();};
//        Guard arrivedAtTargetGuard = () -> arrivedAtTarget();
        DebuggableGuard notArrivedGuard = new DebuggableGuard()
        {
            @Override
            public boolean isTrue()
            {
                return !arrivedAtTarget() && !lostTrack();
            }
            
            @Override
            public String guardAsString()
            {
                return "!arrivedAtTarget() && !lostTrack();";
            }
        };
        DebuggableGuard lostTrack = new DebuggableGuard()
        {
            @Override
            public boolean isTrue()
            {
                return !arrivedAtTarget() && lostTrack();
            }
            
            @Override
            public String guardAsString()
            {
                return "!arrivedAtTarget() && lostTrack()";
            }
        };
        DebuggableGuard arrivedAtTargetGuard = new DebuggableGuard()
        {
            @Override
            public boolean isTrue()
            {
                return arrivedAtTarget();
            }
            
            @Override
            public String guardAsString()
            {
                return "arrivedAtTarget()";
            }
        };

        createTransition(ControllerStates.IDLE, ControllerMsg.DRIVE_TO, TRUE_GUARD, ControllerStates.DRIVING, updateTargetAndReinitCar);
        
        createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, arrivedAtTargetGuard, ControllerStates.IDLE, arrivedBrakeAndStopAction);
        createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, notArrivedGuard, ControllerStates.DRIVING, driveAction);
        createTransition(ControllerStates.DRIVING, ControllerMsg.CONTROL_EVENT, lostTrack, ControllerStates.FAILED, brakeAndStopAction);

        createTransition(ControllerStates.DRIVING, ControllerMsg.STOP, TRUE_GUARD, ControllerStates.IDLE, brakeAndStopAction);
        
        createTransition(ControllerStates.FAILED, ControllerMsg.DRIVE_TO, TRUE_GUARD, ControllerStates.FAILED, informFailedAction);
   
        setInitialState(ControllerStates.IDLE);
        reset();
    }

    private void informFailed(Position2D newTarget)
    {
        System.out.println("Will not drive another route because we failed already. New target is supposed to be: " + newTarget.toString());
    }

    private boolean lostTrack()
    {
        Position2D curPos = _actuatorsSensors.getRearWheelCenterPosition();
        TrajectoryElement curLookaheadElement = _carInterface.getCurrentLookaheadTrajectoryElement();
        double lookahead = _carInterface.getCurrentLookahead();
        if(curLookaheadElement == null)
        {
            System.out.println("\nno lookahead element yet");
            return false;
        }
        Vector2D vector = curLookaheadElement.getVector();
        double baseDist = Position2D.distance(vector.getBase(), curPos);
        double tipDist = Position2D.distance(vector.getTip(), curPos);
        double allowedDeviation = 50.0;
        boolean inRange = tipDist + allowedDeviation > lookahead && baseDist - allowedDeviation <= lookahead;
        if(!inRange)
        {
            System.out.format("\nTip dist: %.2f, base dist: %.2f, lookahead: %.2f", tipDist, baseDist, lookahead);
        }
        return !inRange;
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
