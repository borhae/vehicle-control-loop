package de.joachim.haensel.phd.scenario.scripts.test;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.vehicle.IActuatingSensing;
import de.joachim.haensel.vehicle.ILowerLayerControl;
import de.joachim.haensel.vehicle.ITrajectoryProvider;

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
        _debugParams.getSpeedometer().updateActualVelocity(_actuatorsSensors.getVehicleVelocity());
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
}
