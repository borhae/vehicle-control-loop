package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation;

import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.vehicle.ISegmentBuildingListener;

public class AbstractSegmenter implements ISegmenter
{

    @Override
    public List<Trajectory> createSegments(List<Line2D> route)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addSegmentBuildingListeners(List<ISegmentBuildingListener> segmentBuildingListeners)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyUpdateTrajectory(Vector2D newVector, Deque<Vector2D> updatedList)
    {
        // TODO Auto-generated method stub

    }

}
