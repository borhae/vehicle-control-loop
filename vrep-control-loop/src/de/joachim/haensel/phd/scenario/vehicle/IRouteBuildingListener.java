package de.joachim.haensel.phd.scenario.vehicle;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import de.joachim.haensel.phd.scenario.map.IStreetSection;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public interface IRouteBuildingListener
{
    public void notifyNewRoute(List<Line2D> route);

    public void notifyStartOriginalTrajectory(LinkedList<Vector2D> emptyRoute);
    public void notifyStartOverlayTrajectory(Deque<Vector2D> emptyOverlay);

    public void updateTrajectory(Vector2D newVector, Deque<Vector2D> updatedList);

    public void notifyNewRouteStreetSections(List<IStreetSection> path);
}
