package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.List;

import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.vehicle.ISegmentBuildingListener;

public interface ITrajectorizer
{
    public List<Trajectory> createTrajectory(List<Line2D> downscaled);
    
    public void addSegmentBuildingListeners(List<ISegmentBuildingListener> listeners);
}
