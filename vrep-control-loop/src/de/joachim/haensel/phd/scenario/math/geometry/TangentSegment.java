package de.joachim.haensel.phd.scenario.math.geometry;

public class TangentSegment
{
    private Position2D _tn1;
    private Position2D _tn2;
    private Position2D _associatedPointFromPolygon;

    public TangentSegment(Position2D tn1, Position2D tn2, Position2D associatedPointFromPolygon)
    {
        _tn1 = tn1;
        _tn2 = tn2;
        _associatedPointFromPolygon = associatedPointFromPolygon;
    }

    public Position2D getTn2()
    {
        return _tn2;
    }
    
    public Position2D getTn1()
    {
        return _tn1;
    }
    
    public Position2D getAssociatedPointFromPolygon()
    {
        return _associatedPointFromPolygon;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other == null)
        {
            return false;
        }
        if(!(other instanceof TangentSegment))
        {
            return false;
        }
        TangentSegment otherCasted = (TangentSegment)other;
        
        boolean tn1Eq = false;
        boolean tn2Eq = false;
        if(_tn1 == null)
        {
            tn1Eq = otherCasted._tn1 == null;
        }
        else
        {
            tn1Eq = _tn1.equals(otherCasted._tn1);
        }
        if(_tn2 == null)
        {
            tn2Eq = otherCasted._tn2 == null;
        }
        else
        {
            tn2Eq = _tn2.equals(otherCasted._tn2);
        }
        return tn1Eq && tn2Eq;
    }

    @Override
    public String toString()
    {
        return "<tn1:" + _tn1 + ", tn2:" + _tn2 + ", p:"+ _associatedPointFromPolygon +">";
    }
}
