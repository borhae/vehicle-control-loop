package de.joachim.haensel.phd.scenario.math.geometry;

public class Midpoint extends Position2D
{
    private TangentSegment _t1;
    private TangentSegment _t2;
    private Position2D _begin; //start-point of associated polygon
    private Position2D _end; //end-point of associated polygon

    public Midpoint(Position2D theMidpoint, TangentSegment t1, TangentSegment t2)
    {
        super(theMidpoint.getX(), theMidpoint.getY());
        _t1 = t1;
        _t2 = t2;
        _begin = _t1.getAssociatedPointFromPolygon();
        _end = _t2.getAssociatedPointFromPolygon();
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
        return getX() + seperator + getY();
    }

    public Position2D getAssociatedEndPosition()
    {
        return _end;
    }

    public Position2D getAssociatedStartPosition()
    {
        return _begin;
    }
}
