package de.joachim.haensel.vehicle;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.IDrivingState;

public interface IUpperLayerControl extends ITrajectoryProvider, IDrivingState
{
    void buildSegmentBuffer(Position2D position2d, RoadMap roadMap);

    void initController(IActuatingSensing sensorsActuators, RoadMap roadMap);
}
