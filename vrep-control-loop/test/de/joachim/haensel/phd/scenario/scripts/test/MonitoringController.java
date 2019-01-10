package de.joachim.haensel.phd.scenario.scripts.test;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;

public class MonitoringController implements ILowerLayerControl<Object>
{
    private IActuatingSensing _actuatorsSensors;
    private ITrajectoryProvider _trajectoryProvider;
    private IVrepDrawing _simulatorOutput;
    private DebugParams _debugParams;

    @Override
    public void controlEvent()
    {
        _actuatorsSensors.computeAndLockSensorData();
        
        _debugParams.getSpeedometer().updateWheelRotationSpeed(0.0f);
        _debugParams.getSpeedometer().updateCurrentSegment(null);
        _debugParams.getSpeedometer().updateVelocities(_actuatorsSensors.getVehicleVelocity(), 0.0);
        _debugParams.getSpeedometer().repaint();
    }

    @Override
    public void driveTo(Position2D position)
    {
    }

    @Override
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
    {
        _actuatorsSensors = actuatorsSensors;
        _trajectoryProvider = trajectoryProvider;
    }

    @Override
    public void activateDebugging(IVrepDrawing simulatorOutput, DebugParams params)
    {
        _simulatorOutput = simulatorOutput;
        _debugParams = params;
    }

    @Override
    public void deactivateDebugging()
    {
    }

    @Override
    public void setParameters(Object parameters)
    {
    }

    @Override
    public void stop()
    {
    }

    @Override
    public void addTrajectoryRequestListener(ITrajectoryRequestListener requestListener)
    {
    }

    @Override
    public void addTrajectoryReportListener(ITrajectoryReportListener reportListener)
    {
    }

    @Override
    public void addArrivedListener(IArrivedListener arrivedListener)
    {
    }

    @Override
    public void clearSegmentBuffer()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void clearArrivedListeners()
    {
        // TODO Auto-generated method stub
        
    }
}
