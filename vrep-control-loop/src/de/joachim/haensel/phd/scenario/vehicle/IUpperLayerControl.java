package de.joachim.haensel.phd.scenario.vehicle;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;

public interface IUpperLayerControl extends ITrajectoryProvider, IDrivingState
{
    public void buildSegmentBuffer(Position2D position2d, RoadMap roadMap);

    public void initController(IActuatingSensing sensorsActuators, RoadMap roadMap);

    public void activateDebugging(DebugParams params);

    public void deactivateDebugging();
}
