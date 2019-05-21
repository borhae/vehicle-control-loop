package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import java.util.Arrays;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class CarInterfaceActions
{
    private static final double LOOKAHEAD_FACTOR = 2.0;
    private static final double MIN_DYNAMIC_LOOKAHEAD = 5.1;
    private static final double MAX_DYNAMIC_LOOKAHEAD = 30.0;
    
    private double[] _speedBuf;
    private IActuatingSensing _actuatorsSensors;
    private IDebugVisualizer _debugger;
    private PurePuresuitTargetProvider _targetProvider;
    private double _speedToWheelRotationFactor;
    private double _vehicleLength;

    private List<IArrivedListener> _arrivedListeners;

    public CarInterfaceActions(IActuatingSensing actuatorsSensors, List<IArrivedListener> arrivedListeners, PurePuresuitTargetProvider targetProvider)
    {
        _actuatorsSensors = actuatorsSensors;
        _debugger = new NoOpDebugger();
        _speedBuf = new double[3];
        Arrays.parallelSetAll(_speedBuf, idx -> 0.0);
        _arrivedListeners = arrivedListeners;
        _targetProvider = targetProvider;
    }
    
    public void reInit()
    {
        _actuatorsSensors.computeAndLockSensorData();
        _vehicleLength = _actuatorsSensors.getVehicleLength();
        _targetProvider.loopPrepare();
        _speedToWheelRotationFactor = 2.0 / _actuatorsSensors.getWheelDiameter(); // 2/diameter = 1/radius
    }

    public void clearSegmentBuffer()
    {
        _targetProvider.reset();
    }

    public void driveLoopAction()
    {
        double kv = computeKTimesVelocity();
        double lookahead = kv;
        
        TrajectoryElement closestTrajectoryElement = _targetProvider.getClosestTrajectoryElement();
        TrajectoryElement lookaheadTrajectoryElement = _targetProvider.getLookaheadTrajectoryElement(lookahead);
        
        float targetWheelRotation = 0.0f;
        float targetSteeringAngle = 0.0f;
        if(lookaheadTrajectoryElement != null)
        {
            _debugger.showLookaheadElement(lookaheadTrajectoryElement);
            targetWheelRotation = computeTargetWheelRotationSpeed(closestTrajectoryElement);
            targetSteeringAngle = computeTargetSteeringAngle(lookahead, lookaheadTrajectoryElement);
        }
        _debugger.showVelocities(targetWheelRotation, lookaheadTrajectoryElement, closestTrajectoryElement);
        _actuatorsSensors.drive(targetWheelRotation, targetSteeringAngle);
    }

    private float computeTargetWheelRotationSpeed(TrajectoryElement closestSegment)
    {
        if(closestSegment != null)
        {
            double setSpeed = closestSegment.getVelocity();
            double speed = lowPassFilter(setSpeed);
            return (float) (speed * _speedToWheelRotationFactor);
        }
        else
        {
            return 0.0f;
        }
    }

    private double lowPassFilter(double setSpeed)
    {
        double speed = (_speedBuf[0] + _speedBuf[1] + _speedBuf[2] + setSpeed) / 4.0;
        _speedBuf[0] = _speedBuf[1];
        _speedBuf[1] = _speedBuf[2];
        _speedBuf[2] = setSpeed;
        return speed;
    }
    
    protected float computeTargetSteeringAngle(double lookahead, TrajectoryElement lookaheadTrajectoryElement)
    {
        Position2D rearWheelPosition = _actuatorsSensors.getRearWheelCenterPosition();
        Position2D frontWheelPosition = _actuatorsSensors.getFrontWheelCenterPosition();
        Vector2D currentSegment = lookaheadTrajectoryElement.getVector();
        Vector2D rearWheelToLookAhead = computeRearWheelToLookaheadVector(rearWheelPosition, currentSegment, lookahead);
        Vector2D rearWheelToFrontWheel = new Vector2D(rearWheelPosition, frontWheelPosition);
        if(rearWheelToLookAhead == null)
        {
            return 0.0f;
        }
        else
        {
            double alpha = Vector2D.computeAngle(rearWheelToLookAhead, rearWheelToFrontWheel) * rearWheelToLookAhead.side(rearWheelToFrontWheel) * -1.0;
            double L = _vehicleLength;
            double kv = computeKTimesVelocity();
            
            double delta = Math.atan( (2.0 * L * Math.sin(alpha)) / kv);
            return (float) delta;
        }
    }

    private Vector2D computeRearWheelToLookaheadVector(Position2D rearWheelPosition, Vector2D currentSegment, double lookahead)
    {
        //TODO handle multiple solutions: pick the one looking forward :)
        List<Vector2D> circleIntersection = Vector2D.circleIntersection(currentSegment, rearWheelPosition, lookahead);
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

    private double computeKTimesVelocity()
    {
        double[] vehicleVelocityXYZ = _actuatorsSensors.getVehicleVelocity();
        double velocity = new Vector2D(0.0, 0.0, vehicleVelocityXYZ[0], vehicleVelocityXYZ[1]).getLength();
        double k = LOOKAHEAD_FACTOR;
        double kv = k * velocity;
        kv = kv > MAX_DYNAMIC_LOOKAHEAD ? MAX_DYNAMIC_LOOKAHEAD : kv;
        kv = kv < MIN_DYNAMIC_LOOKAHEAD ? MIN_DYNAMIC_LOOKAHEAD : kv;
        return kv;
    }
    
    public void brakeAndStopAction()
    {
        System.out.println("Brake!");
        _actuatorsSensors.drive(0.0f, 0.0f);
    }

    public void arrivedBrakeAndStopAction()
    {
        System.out.println("Arrived!");
        _actuatorsSensors.drive(0.0f, 0.0f);
        Position2D position = _actuatorsSensors.getFrontWheelCenterPosition();
//        Hopefully I don't get a concurrent modification exception if the list is copied to an array first
//        weird though... TODO rethink this when I have the time
//        _arrivedListeners.forEach(listener -> listener.arrived(position));
        System.out.println("copying arrived listeners to have no concurrent situation");
        IArrivedListener[] array = _arrivedListeners.toArray(new IArrivedListener[0]);
        for (int idx = 0; idx < array.length; idx++)
        {
            array[idx].arrived(position);
        }
        System.out.println("all listeners called, clearing the list");
        _arrivedListeners.clear();
    }

    public void setDebugger(IDebugVisualizer debugger)
    {
        _debugger = debugger;
    }
}
