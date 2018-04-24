package de.joachim.haensel.phd.scenario.vehicle.navigation;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class Trajectory
{
    private Vector2D _vector;
    private TrajectoryType _type;

    public Trajectory(Vector2D vector)
    {
        _vector = vector;
    }

    public Vector2D getVector()
    {
        return _vector;
    }

    @Override
    public String toString()
    {
        return _vector.toString();
    }

    public void setIsOverlay()
    {
        _type = TrajectoryType.OVERLAY;
    }

    public void setIsOriginal()
    {
        _type = TrajectoryType.ORIGINAL;
    }
}
