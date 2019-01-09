package de.joachim.haensel.phd.scenario.debug;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public interface INavigationListener
{
    public void notifySegmentsChanged(List<TrajectoryElement> segments);

    public void notifyRouteChanged(List<Line2D> route);

    public void activateRouteDebugging();

    public void activateSegmentDebugging();
}
