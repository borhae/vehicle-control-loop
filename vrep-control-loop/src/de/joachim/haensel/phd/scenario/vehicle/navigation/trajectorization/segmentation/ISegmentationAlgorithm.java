package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation;

import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.ISegmentBuildingListener;

public interface ISegmentationAlgorithm extends ISegmentationConstants
{
    public void quantize(Deque<Vector2D> srcRoute, Deque<Vector2D> quantizedRoute, double stepSize);
    
    public void setSegmentBuildingListeners(List<ISegmentBuildingListener> segmentBuildingListeners);
}
