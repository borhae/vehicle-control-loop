package de.joachim.haensel.phd.scenario.math.geometry;

public class Midpoint
{
    private Position2D _theMidpoint;
    private TangentSegment _t1;
    private TangentSegment _t2;

    public Midpoint(Position2D theMidpoint, TangentSegment t1, TangentSegment t2)
    {
        _theMidpoint = theMidpoint;
        _t1 = t1;
        _t2 = t2;
    }

    public Position2D getTheMidpoint()
    {
        return _theMidpoint;
    }

    public TangentSegment getT1()
    {
        return _t1;
    }

    public TangentSegment getT2()
    {
        return _t2;
    }

    public String toString(String seperator)
    {
        return _theMidpoint.getX() + seperator + _theMidpoint.getY();
    }
}
