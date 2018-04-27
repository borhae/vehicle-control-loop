package de.joachim.haensel.phd.scenario.vehicle.navigation;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory.VelocityEdgeType;

public class Trajectory
{
    public enum VelocityEdgeType
    {
        START, RAISE, FALL
    }

    private Vector2D _vector;
    private TrajectoryType _type;
    private double _velocity;
    private VelocityEdgeType _velocityEdgeType;

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

    public boolean hasType(TrajectoryType other)
    {
        return _type == other;
    }

    public void setVelocity(double velocity)
    {
        _velocity = velocity;
    }

    public String toCSV()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(_velocity);
        return sb.toString();
    }

    public double getVelocity()
    {
        return _velocity;
    }

    public void setRiseFall(VelocityEdgeType type)
    {
        _velocityEdgeType = type;
    }

    public VelocityEdgeType getRiseFall()
    {
        return _velocityEdgeType;
    }
}
