package de.joachim.haensel.vehicle;

import de.joachim.haensel.phd.scenario.vehicle.IDrivingState;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;

public interface ITopLayerControl extends ITrajectoryProvider, IDrivingState
{
    void driveTo(Position2D position2d, RoadMap roadMap);

    void driveToBlocking(Position2D position2d, RoadMap roadMap);
}
