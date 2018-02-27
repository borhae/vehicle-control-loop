package de.joachim.haensel.phd.scenario.vehicle.navigation;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;

public class Trajectory
{
    private Vector2D _vector;

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
}
