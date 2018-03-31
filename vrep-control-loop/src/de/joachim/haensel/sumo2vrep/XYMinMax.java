package de.joachim.haensel.sumo2vrep;

public class XYMinMax
{
    @Override
    public String toString()
    {
        return "Min Max [(" + _curXMin + ", " + _curYMin + ")" + ", (" + _curXMax + ", " + _curYMax + "), <" + distX() + ", " + distY() + ">]";
    }

    private float _curXMin;
    private float _curXMax;
    private float _curYMin;
    private float _curYMax;

    public XYMinMax()
    {
        _curXMin = Float.POSITIVE_INFINITY;
        _curXMax = Float.NEGATIVE_INFINITY;
        _curYMin = Float.POSITIVE_INFINITY;
        _curYMax = Float.NEGATIVE_INFINITY;
    }
    
    public void update(float xPos, float yPos)
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

    public float distX()
    {
        return _curXMax - _curXMin;
    }

    public float distY()
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
}