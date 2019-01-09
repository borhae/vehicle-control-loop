package de.joachim.haensel.phd.scenario.layerinterface;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class MockTrajectoryRequestListener implements ITrajectoryRequestListener
{
    @Override
    public void notifyNewTrajectories(List<TrajectoryElement> trajectories, long timestamp)
    {
    }
}
