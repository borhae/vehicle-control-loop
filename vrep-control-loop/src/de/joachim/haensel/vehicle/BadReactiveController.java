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
import de.joachim.haensel.statemachine.States;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class BadReactiveController implements ILowLevelController
{
    private static final int SEGMENT_BUFFER_SIZE = 40;
    private Position2D _expectedTarget;
    private IActuatingSensing _actuatorsSensors;
    private DefaultReactiveControllerStateMachine _stateMachine;
    private LinkedList<Trajectory> _segmentBuffer;
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
        System.out.println("acting on drive :)");
        ensureBufferSize();
        System.out.print("buffersize [V]");
        chooseCurrentSegment();
        System.out.print(", curSeg:" + _currentSegment.getVector());
        float targetWheelRotation = computeTargetWheelRotationSpeed();
        System.out.print(", v:" + targetWheelRotation);
        float targetSteeringAngle = computeTargetSteeringAngle();
        System.out.print(", delta:" + targetSteeringAngle);
        _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
        System.out.println("drive called with: v:(" + targetWheelRotation + "), delta:(" + targetSteeringAngle + ")");
    }

    private void chooseCurrentSegment()
    {
        Position2D curPos = _actuatorsSensors.getPosition();
        Trajectory firstSegment = _segmentBuffer.peek();
        Trajectory secondSegment = _segmentBuffer.get(1);
        
        double distS1 = firstSegment.getVector().distance(curPos);
        double distS2 = secondSegment.getVector().distance(curPos);
        if(distS1 > distS2)
        {
            _segmentBuffer.pop(); //discard first
            _currentSegment = secondSegment;
        }
        else
        {
            _currentSegment = firstSegment;
        }
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
        //TODO last action was here, might already work
        Position2D rearWheelPosition = _actuatorsSensors.getRearWheelCenterPosition();
        Vector2D currentSegment = _currentSegment.getVector();
        
        Vector2D rearWheelToLookAhead = computeRearWheelToLookaheadVector(rearWheelPosition, currentSegment);
        if(rearWheelToLookAhead == null)
        {
            System.out.println("line between " + rearWheelPosition + " and " + currentSegment + " with required distance " + _lookahead + " resulted in a null value");
            return 0.0f;
        }
        double alpha = Vector2D.computeAngle(rearWheelToLookAhead, currentSegment);
        
        
        double delta = Math.atan( (2.0 *_actuatorsSensors.getVehicleLength() * Math.sin(alpha)) / (rearWheelToLookAhead.length()) );
        
        return (float)delta;
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
            return circleIntersection.get(0);
        }
    }

    protected float computeTargetWheelRotationSpeed()
    {
        return 10.0f;
    }

    @Override
    public String toString()
    {
        States currentState = _stateMachine.getCurrentState();
        return "state:" + currentState + ", target:" + _expectedTarget;
    }
}
