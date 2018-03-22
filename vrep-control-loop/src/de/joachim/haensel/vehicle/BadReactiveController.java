package de.joachim.haensel.vehicle;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerMsg;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ControllerStates;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Guard;
import de.joachim.haensel.statemachine.States;
import de.joachim.haensel.sumo2vrep.Position2D;

public class BadReactiveController implements ILowLevelController
{
    private static final int MIN_SEGMENT_BUFFER_SIZE = 5;
    private static final int SEGMENT_BUFFER_SIZE = 10;
    private Position2D _expectedTarget;
    private IActuatingSensing _actuatorsSensors;
    private DefaultReactiveControllerStateMachine _stateMachine;
    private LinkedList<Trajectory> _segmentBuffer;
    private ITrajectoryProvider _segmentProvider;
    private Trajectory _currentSegment;
    private double _lookahead;
    private int _segmentVisualHandle;

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
        ensureBufferSize();
        _actuatorsSensors.computeAndLockSensorData();
        Position2D currentPosition = _actuatorsSensors.getPosition();
        chooseCurrentSegment(currentPosition);
        _segmentVisualHandle = _actuatorsSensors.drawVector(_currentSegment.getVector(), Color.RED);
    }

    @Override
    public void controlEvent()
    {
        boolean wasDriving = _stateMachine.getCurrentState() == ControllerStates.DRIVING;
        _stateMachine.controlEvent(this);
        if(wasDriving && _stateMachine.getCurrentState() == ControllerStates.IDLE)
        {
            _actuatorsSensors.removeVector(_segmentVisualHandle);
        }
    }

    private void driveAction()
    {
        System.out.println("------------------------------------------------------------------------------");
        _actuatorsSensors.computeAndLockSensorData();
        ensureBufferSize();
        chooseCurrentSegment(_actuatorsSensors.getPosition());
        _actuatorsSensors.drawUpdateVector(_segmentVisualHandle, _currentSegment.getVector(), Color.RED);
        float targetWheelRotation = computeTargetWheelRotationSpeed();
        float targetSteeringAngle = computeTargetSteeringAngle();
        _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
        System.out.println("drive called with: v:(" + targetWheelRotation + "), delta:(" + targetSteeringAngle + ")");
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
            int segIdx = 0;
            for (Trajectory curTraj : _segmentBuffer)
            {
                segIdx++;
                if(isInRange(currentPosition, curTraj.getVector(), _lookahead))
                {
                    segmentFound = true;
                    break;
                }
            }
            if(!segmentFound)
            {
                System.out.println("No new segment. Lookahead:" +  _lookahead + ", Pos: " + currentPosition + ", buffer: " + _segmentBuffer);
                return;
            }
            else
            {
                System.out.println("Removing " + segIdx + " elements from buffer");
                while(segIdx > 0)
                {
                    _currentSegment = _segmentBuffer.pop();
                    segIdx--;
                }
            }
        }
    }

    private boolean isInRange(Position2D position, Vector2D vector, double requiredDistance)
    {
        double baseDist = Position2D.distance(vector.getBase(), position);
        double tipDist = Position2D.distance(vector.getTip(), position);
//        System.out.println("Td: " + tipDist + ", Bd: " + baseDist);
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
        Vector2D currentSegment = _currentSegment.getVector();
        _actuatorsSensors.drawUpdateVector(_segmentVisualHandle, currentSegment, Color.RED);
        Vector2D rearWheelToLookAhead = computeRearWheelToLookaheadVector(rearWheelPosition, currentSegment);
        if(rearWheelToLookAhead == null)
        {
//            System.out.println("line between " + rearWheelPosition + " and " + currentSegment + " with required distance " + _lookahead + " resulted in a null value");
            System.out.println("!!!!! no rear wheel to current segment vector of desired length");
            return 0.0f;
        }
        else
        {
//            System.out.println("line between " + rearWheelPosition + " and " + currentSegment + " with required distance " + _lookahead + " resulted in: " + rearWheelToLookAhead);
            double alpha = Vector2D.computeAngle(rearWheelToLookAhead, currentSegment);
//            System.out.print("-> angle alpha: " + alpha);
            double delta = Math.atan( (2.0 *_actuatorsSensors.getVehicleLength() * Math.sin(alpha)) / (rearWheelToLookAhead.length()) );
//            System.out.println(", angle delta: " + delta);
            double delatDegrees = Math.toDegrees(delta);
            System.out.println("Steering deg.:" + delatDegrees);
            return (float)delta;
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
        return -0.5f;
    }

    @Override
    public String toString()
    {
        States currentState = _stateMachine.getCurrentState();
        return "state:" + currentState + ", target:" + _expectedTarget;
    }
}
