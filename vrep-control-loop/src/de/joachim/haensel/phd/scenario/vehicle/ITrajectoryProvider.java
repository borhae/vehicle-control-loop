package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public interface ITrajectoryProvider
{
    public List<TrajectoryElement> getNewSegments(int segmentRequestSize);

	public boolean segmentsLeft();
}
