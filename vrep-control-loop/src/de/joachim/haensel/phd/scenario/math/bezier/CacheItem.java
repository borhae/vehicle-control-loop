package de.joachim.haensel.phd.scenario.math.bezier;

public class CacheItem
{
    public double _position;
    public double _xpos;
    public double _ypos;
    public double _zpos;
    public double _travelled;

    public CacheItem(double xpos, double ypos, double zpos)
    {
        _xpos = xpos;
        _ypos = ypos;
        _zpos = zpos;
    }
}