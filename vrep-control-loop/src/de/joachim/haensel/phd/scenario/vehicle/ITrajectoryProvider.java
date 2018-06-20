package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;

public interface ITrajectoryProvider
{
    List<Trajectory> getNewSegments(int segmentRequestSize);
}
