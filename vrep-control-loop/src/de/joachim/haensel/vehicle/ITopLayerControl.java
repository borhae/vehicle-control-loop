package de.joachim.haensel.vehicle;

import de.joachim.haensel.sumo2vrep.Position2D;

public interface ITopLayerControl extends ITrajectoryProvider
{
    void driveTo(Position2D position2d);

    void driveToBlocking(Position2D position2d);
}
