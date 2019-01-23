package de.joachim.haensel.phd.scenario.map.sumo2vrep;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class OrientedPosition
{
    private Position2D _pos;
    private double _angle;

    public OrientedPosition(Position2D pos, double angle)
    {
        _pos = pos;
        _angle = angle;
    }

    public Position2D getPos()
    {
        return _pos;
    }

    public void setPos(Position2D pos)
    {
        _pos = pos;
    }

    public double getAngle()
    {
        return _angle;
    }

    public void setAngle(double angle)
    {
        _angle = angle;
    }
}
