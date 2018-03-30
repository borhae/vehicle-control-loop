package de.joachim.haensel.vehicle;

import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.sumo2vrep.Position2D;

public interface ILowLevelController<P>
{
    public void controlEvent();
    public void driveTo(Position2D position);
    public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider);
    public void activateDebugging(IVrepDrawing actuatingSensing);
    public void deactivateDebugging();
    public void setParameters(P parameters);
}
