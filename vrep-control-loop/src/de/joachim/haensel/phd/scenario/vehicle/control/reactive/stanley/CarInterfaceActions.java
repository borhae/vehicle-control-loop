package de.joachim.haensel.phd.scenario.vehicle.control.reactive.stanley;

import java.util.Arrays;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ICarInterfaceActions;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.IDebugVisualizer;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.NoOpDebugger;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

/**
 * computeTargetSteeringAngle implements the actual control logic of the Stanley controller. Not the original algorithm, which I can't get to work...
 * Might be timing issues (to much lag in control?) 
 * @author Simulator
 *
 */
public class CarInterfaceActions implements ICarInterfaceActions
{
    private double[] _speedBuf;
    private IActuatingSensing _actuatorsSensors;
    private IDebugVisualizer _debugger;
    private StanleyTargetProvider _targetProvider;
    private double _speedToWheelRotationFactor;

    private List<IArrivedListener> _arrivedListeners;

    public CarInterfaceActions(IActuatingSensing actuatorsSensors, List<IArrivedListener> arrivedListeners, StanleyTargetProvider targetProvider)
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
        _targetProvider.loopPrepare();
        _speedToWheelRotationFactor = 2.0 / _actuatorsSensors.getWheelDiameter(); // 2/diameter = 1/radius
    }

    public void clearSegmentBuffer()
    {
        _targetProvider.reset();
    }

    public void driveLoopAction()
    {
        TrajectoryElement closestTrajectoryElement = _targetProvider.getClosestTrajectoryElement();
        
        float targetWheelRotation = 0.0f;
        float targetSteeringAngle = 0.0f;
        targetWheelRotation = computeTargetWheelRotationSpeed(closestTrajectoryElement);
        targetSteeringAngle = computeTargetSteeringAngle(closestTrajectoryElement);

        _debugger.showVelocities(targetWheelRotation, closestTrajectoryElement, closestTrajectoryElement);
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
    
    /**
     * Modified Stanley Controller. We additionally divide track-error by velocity and 
     * the tangent error by sqrt(velocity). 
     * @param currentTrajectory
     * @return
     */
    protected float computeTargetSteeringAngle(TrajectoryElement currentTrajectory)
    {
        Position2D rearWheelPosition = _actuatorsSensors.getRearWheelCenterPosition();
        Position2D frontWheelPosition = _actuatorsSensors.getFrontWheelCenterPosition();
        Vector2D rearWheelToFrontWheel = new Vector2D(rearWheelPosition, frontWheelPosition);
        Vector2D streetTangentVec = currentTrajectory.getVector();
        Line2D streetTangent = streetTangentVec.toLine();
        double deltaError = Vector2D.computeSplitAngle(streetTangentVec, rearWheelToFrontWheel);
        double[] vehicleVelocityXYZ = _actuatorsSensors.getVehicleVelocity();
        double vehicleVelocity = new Vector2D(0.0, 0.0, vehicleVelocityXYZ[0], vehicleVelocityXYZ[1]).getLength();
        double trackError = -(streetTangent.distance(frontWheelPosition) * streetTangent.side(frontWheelPosition));
        double k = 2.0;
//        double tanTrackErr = Math.atan((k * trackError) / vehicleVelocity);
        double tanTrackErr = Math.atan((k * trackError)/vehicleVelocity);
        double result = deltaError/Math.sqrt(vehicleVelocity) + tanTrackErr/vehicleVelocity;
//        System.out.format("%.4f, %.4f, %.4f, %.4f\n", deltaError, tanTrackErr, trackError, vehicleVelocity);
        result = cap(result);
        return (float) result;
    }

    private double cap(double result)
    {
        if(result >= (Math.PI / 4.0))
        {
            result = Math.PI / 4.0;
        }
        else if(result <=  -Math.PI / 4.0)
        {
            result = -Math.PI / 4.0;
        }
        return result;
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

    @Override
    public boolean hasLookahead()
    {
        return false;
    }
}
