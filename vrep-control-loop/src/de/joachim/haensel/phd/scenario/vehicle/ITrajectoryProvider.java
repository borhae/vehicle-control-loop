package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public interface ITrajectoryProvider
{
    List<TrajectoryElement> getNewSegments(int segmentRequestSize);
}
