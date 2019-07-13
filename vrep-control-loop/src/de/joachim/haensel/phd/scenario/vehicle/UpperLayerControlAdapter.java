package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class UpperLayerControlAdapter implements IUpperLayerControl
{
    @Override
    public List<TrajectoryElement> getNewElements(int segmentRequestSize)
    {
        return null;
    }

    @Override
    public boolean segmentsLeft()
    {
        return false;
    }

    @Override
    public boolean hasElements(int elementRequestSize)
    {
        return false;
    }

    @Override
    public double getTrajectoryElementLength()
    {
        return 0;
    }

    @Override
    public void buildSegmentBuffer(Position2D position2d, RoadMap roadMap)
    {
    }

    @Override
    public void initController(IActuatingSensing sensorsActuators, RoadMap roadMap)
    {
    }

    @Override
    public void activateDebugging(DebugParams params)
    {
    }

    @Override
    public void deactivateDebugging()
    {
    }

    @Override
    public void addRouteBuilderListener(IRouteBuildingListener listener)
    {
    }
}
