package de.joachim.haensel.vehiclecontrol.base;

public class Position2D
{
    private float _x;
    private float _y;

    public Position2D(float x, float y)
    {
        _x = x;
        _y = y;
    }

    public void setX(float x)
    {
        _x = x;
    }

    public float getY()
    {
        return _y;
    }

    public void setY(float y)
    {
        _y = y;
    }

    public float getX()
    {
        return _x;
    }
    
    public void setXY(float[] xy)
    {
        _x = xy[0];
        _y = xy[1];
    }

    public boolean equals(Position2D other, double epsilon)
    {
        float a = Math.abs(other._x - _x);
        float b = Math.abs(other._y - _y);
        return Math.sqrt(a*a + b*b) <= epsilon;
    }

    public void setXY(float x, float y)
    {
        _x = x;
        _y = y;
    }
}
