package de.joachim.haensel.phd.scenario.vehicle.control.interfacing;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public interface ITrajectoryRequestListener
{
    public void notifyNewTrajectories(List<TrajectoryElement> trajectories, long timestamp);
}
