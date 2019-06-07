package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public interface ITrajectoryProvider
{
    public List<TrajectoryElement> getNewElements(int segmentRequestSize);

	public boolean segmentsLeft();

    public boolean hasElements(int elementRequestSize);
}
