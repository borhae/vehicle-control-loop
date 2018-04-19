package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.bezier;

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