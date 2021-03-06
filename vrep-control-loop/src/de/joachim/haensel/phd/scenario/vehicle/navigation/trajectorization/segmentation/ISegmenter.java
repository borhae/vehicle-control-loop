package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation;

import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public interface ISegmenter extends ISegmentationConstants
{
    public List<TrajectoryElement> createSegments(List<Line2D> route);

    public void addSegmentBuildingListeners(List<IRouteBuildingListener> segmentBuildingListeners);

    public void notifyUpdateTrajectory(Vector2D newVector, Deque<Vector2D> updatedList);
    
    public ISegmentationAlgorithm getAlgorithm();
}
