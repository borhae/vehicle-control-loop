package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation;

import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.vehicle.ISegmentBuildingListener;

public interface ISegmenter
{
    public List<Trajectory> createSegments(List<Line2D> route);

    public void addSegmentBuildingListeners(List<ISegmentBuildingListener> segmentBuildingListeners);

    public void notifyUpdateTrajectory(Vector2D newVector, Deque<Vector2D> updatedList);
}
