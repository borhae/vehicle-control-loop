package de.joachim.haensel.vehicle;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;

public interface ILowLevelController<P>
{
    public void controlEvent();
    public void driveTo(Position2D position);
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider);
    public void activateDebugging(IVrepDrawing actuatingSensing, double zValue);
    public void deactivateDebugging();
    public void setParameters(P parameters);
}
