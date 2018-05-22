package de.joachim.haensel.phd.scenario.vehicle.navigation;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class Trajectory
{
    public enum VelocityEdgeType
    {
        START, RAISE, FALL, UNKNOWN
    }

    private Vector2D _vector;
    private TrajectoryType _type;
    private double _velocity;
    private VelocityEdgeType _velocityEdgeType;
    private double _radius;
    private double _kappa;
    private int _index;

    public Trajectory(Vector2D vector)
    {
        _vector = vector;
        _type = TrajectoryType.UNKNOWN;
        _velocity = 0.0;
        _velocityEdgeType = VelocityEdgeType.UNKNOWN;
        _radius = 0.0;
        _kappa = 0.0;
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

    public void setRadius(double radius)
    {
        _radius = radius;
    }

    public void setKappa(double kappa)
    {
        _kappa = kappa;
    }

    public double getRadius()
    {
        return _radius;
    }

    public double getKappa()
    {
        return _kappa;
    }

    public void setIdx(int index)
    {
        _index = index;
    }

    public int getIdx()
    {
        return _index;
    }
}
