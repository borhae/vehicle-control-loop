package de.joachim.haensel.phd.scenario.math.bezier;

public class CacheItem
{
    public float _position;
    public float _xpos;
    public float _ypos;
    public float _zpos;
    public float _travelled;

    public CacheItem(float xpos, float ypos, float zpos)
    {
        _xpos = xpos;
        _ypos = ypos;
        _zpos = zpos;
    }
}