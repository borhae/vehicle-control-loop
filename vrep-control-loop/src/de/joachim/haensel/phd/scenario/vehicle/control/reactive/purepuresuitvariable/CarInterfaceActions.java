package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    private TrajectoryElement _cachedLookaheadTrajectoryElement;
    private double _cashedLookahead;

    public CarInterfaceActions(IActuatingSensing actuatorsSensors, List<IArrivedListener> arrivedListeners, PurePuresuitTargetProvider targetProvider)
    {
        _actuatorsSensors = actuatorsSensors;
        _debugger = new NoOpDebugger();
        _speedBuf = new double[3];
        Arrays.parallelSetAll(_speedBuf, idx -> 0.0);
        _arrivedListeners = arrivedListeners;
        _targetProvider = targetProvider;
        _cachedLookaheadTrajectoryElement = null;
        _cashedLookahead = 0.0;
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
        _cashedLookahead = lookahead;
        
        TrajectoryElement closestTrajectoryElement = _targetProvider.getClosestTrajectoryElement();   
        TrajectoryElement lookaheadTrajectoryElement = _targetProvider.getLookaheadTrajectoryElement(lookahead);
        _cachedLookaheadTrajectoryElement  = lookaheadTrajectoryElement;
        
        float targetWheelRotation = 0.0f;
        float targetSteeringAngle = 0.0f;
        if(lookaheadTrajectoryElement != null)
        {
            _debugger.showLookaheadElement(lookaheadTrajectoryElement);
            targetWheelRotation = computeTargetWheelRotationSpeed(closestTrajectoryElement);
            targetSteeringAngle = computeTargetSteeringAngle(lookahead, lookaheadTrajectoryElement);
            
            if(lookaheadTrajectoryElement.isReverse()) 
            {
                targetWheelRotation = - targetWheelRotation;
            }
        }
        else
        {
            System.out.println("No lookahead assigned, speed and steering set to 0.0!");
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
        Vector2D currentTrajectoryElementVector = lookaheadTrajectoryElement.getVector();
        Vector2D rearWheelToLookAhead = computeRearWheelToLookaheadVector(rearWheelPosition, currentTrajectoryElementVector, lookahead);
        Vector2D rearWheelToFrontWheel = new Vector2D(rearWheelPosition, frontWheelPosition);
        if(rearWheelToLookAhead == null)
        {
            rearWheelToLookAhead = new Vector2D(rearWheelPosition, currentTrajectoryElementVector.getTip());
        }
        double alpha = Vector2D.computeAngle(rearWheelToLookAhead, rearWheelToFrontWheel) * rearWheelToLookAhead.side(rearWheelToFrontWheel) * -1.0;
        double L = _vehicleLength;
        double kv = computeKTimesVelocity();
        
        double delta = Math.atan( (2.0 * L * Math.sin(alpha)) / kv);
        return (float) delta;
    }

    private Vector2D computeRearWheelToLookaheadVector(Position2D rearWheelPosition, Vector2D currentSegment, double lookahead)
    {
        List<Vector2D> result1 = new ArrayList<>();
        // formula: ((x1-x0)t+x0-h)^2+((y1-y0)t+y0-k)^2=r^2  with (h, k) centerpoint and r the radius
        // after collection: at^2 + bt +c with
        // a=(x1-x0)^2+(y1-y0)^2
        // b=2(x1-x0)(x0-h)+2(y1-y0)(y0-k)
        // c=(x0-h)^2+(y0-k)^2-r^2
        // t = (-b +- sqr(b^2-4ac)/2a
        double h = rearWheelPosition.getX();
        double k = rearWheelPosition.getY();
        double x0 = currentSegment.getbX();
        double y0 = currentSegment.getbY();
        double x0h = x0 - h;
        double y0k = y0 - k;
        double dx = currentSegment.getdX();
        double dy = currentSegment.getdY();
        
        double a = dx * dx + dy * dy;
        double b = 2*dx*x0h + 2*dy*y0k; 
        double c = x0h * x0h +  y0k * y0k - lookahead * lookahead;
        
        double root = b * b - 4*a*c;
        String errT1 = "";
        String errT2 = "";
        String errRoot = "";
        if(root >= 0)
        {
            double t1 = (-b + Math.sqrt(root))/(2*a);
            double t2 = (-b - Math.sqrt(root))/(2*a);
            
            if(0 <= t1 && t1 <= 1)
            {
                Vector2D v1 = new Vector2D(x0, y0, t1 * dx, t1 * dy);
                result1.add(v1);
            }
            else
            {
                errT1 = String.format("t1: %.6f", t1);
            }
            if(0 <= t2 && t2 <= 1)
            {
                Vector2D v2 = new Vector2D(x0, y0, t2 * dx, t2 * dy);
                result1.add(v2);
            }
            else
            {
                errT2 = String.format("t2: %.6f", t2);
            }
        }
        else
        {
            errRoot = String.format("root: %.4f", root);
        }
        
        List<Vector2D> circleIntersection = result1;
        if(circleIntersection.isEmpty())
        {
            System.out.println("----------------------------------------------------------------------------------");
            System.out.format(Locale.US, "circle intersection returned empty value: %s, %s, %s\n", errT1, errT2, errRoot);
            Position2D curTip = currentSegment.getTip();
            System.out.format(Locale.US, "center: (%.2f, %.2f), line: (%.2f, %.2f), (%.2f, %.2f)\n", h, k, x0, y0, curTip.getX(), curTip.getY());
            System.out.format(Locale.US, "lookahead: %.4f\n", lookahead);
            System.out.println("----------------------------------------------------------------------------------");
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

    public TrajectoryElement getCurrentLookaheadTrajectoryElement()
    {
        return _cachedLookaheadTrajectoryElement;
    }
    
    public TrajectoryElement getNearestReverseElement()
    {
        return _targetProvider.getNearestReverseElement();
    }
    
    public double getCurrentLookahead()
    {
        return _cashedLookahead;
    }
}
