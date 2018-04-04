package de.joachim.haensel.vehicle;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Line2D;

public interface ISegmentBuildingListener
{
    public void notifyNewRoute(List<Line2D> route);

    public void notifyStartOriginalTrajectory(LinkedList<Vector2D> emptyRoute);
    public void notifyStartOverlayTrajectory(Deque<Vector2D> emptyOverlay);

    public void updateTrajectory(Vector2D newVector, Deque<Vector2D> updatedList);

}
