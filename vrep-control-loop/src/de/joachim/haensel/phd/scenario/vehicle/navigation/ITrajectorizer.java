package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssigner;

public interface ITrajectorizer
{
    public List<TrajectoryElement> createTrajectory(List<Line2D> mapBasedLineRoute);
    
    public void addSegmentBuildingListeners(List<IRouteBuildingListener> listeners);

    public IVelocityAssigner getVelocityAssigner();
}
