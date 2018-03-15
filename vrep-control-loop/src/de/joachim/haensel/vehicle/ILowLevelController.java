package de.joachim.haensel.vehicle;

import de.joachim.haensel.sumo2vrep.Position2D;

public interface ILowLevelController
{
    public void controlEvent();
    public void driveTo(Position2D position);
    void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider);
}
