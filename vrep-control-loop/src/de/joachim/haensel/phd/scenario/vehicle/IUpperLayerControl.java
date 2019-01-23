package de.joachim.haensel.phd.scenario.vehicle;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

/**
 * This interface will be used when a vehicle is created and provides commands for the lower layer
 * TODO Remove route building listener interfaces. I added the listening to route building although not every upper controller will build a route on map .
 * @author dummy
 *
 */
public interface IUpperLayerControl extends ITrajectoryProvider, IDrivingState
{
    public void buildSegmentBuffer(Position2D position2d, RoadMap roadMap);

    public void initController(IActuatingSensing sensorsActuators, RoadMap roadMap);

    public void activateDebugging(DebugParams params);

    public void deactivateDebugging();

    public void addRouteBuilderListener(IRouteBuildingListener listener);
}
