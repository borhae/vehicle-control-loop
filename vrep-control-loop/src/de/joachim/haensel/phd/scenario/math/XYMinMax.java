package de.joachim.haensel.phd.scenario.math;

public class XYMinMax
{
    @Override
    public String toString()
    {
        return "Min Max [(" + _curXMin + ", " + _curYMin + ")" + ", (" + _curXMax + ", " + _curYMax + "), <" + distX() + ", " + distY() + ">]";
    }

    private double _curXMin;
    private double _curXMax;
    private double _curYMin;
    private double _curYMax;

    public XYMinMax()
    {
        _curXMin = Double.POSITIVE_INFINITY;
        _curXMax = Double.NEGATIVE_INFINITY;
        _curYMin = Double.POSITIVE_INFINITY;
        _curYMax = Double.NEGATIVE_INFINITY;
    }
    
    public void update(double xPos, double yPos)
    {
        if(xPos < _curXMin)
        {
            _curXMin = xPos;
        }
        if(xPos > _curXMax)
        {
            _curXMax = xPos;
        }
        if(yPos < _curYMin)
        {
            _curYMin = yPos;
        }
        if(yPos > _curYMax)
        {
            _curYMax = yPos;
        }
    }

    public double distX()
    {
        return _curXMax - _curXMin;
    }

    public double distY()
    {
        return _curYMax - _curYMin;
    }

    public double minX()
    {
        return _curXMin;
    }

    public double minY()
    {
        return _curYMin;
    }

    public boolean isInRange(double x, double y)
    {
        return (_curXMin <= x) && (x <= _curXMax) && (_curYMin <= y) && (y <= _curYMax);
    }
}