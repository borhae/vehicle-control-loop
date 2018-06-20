package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.vehicle.ISegmentBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssigner;

public interface ITrajectorizer
{
    public List<Trajectory> createTrajectory(List<Line2D> mapBasedLineRoute);
    
    public void addSegmentBuildingListeners(List<ISegmentBuildingListener> listeners);

    public IVelocityAssigner getVelocityAssigner();
}
