package de.joachim.haensel.phd.scenario.math.bezier;

class CacheItem
{
    public float _position;
    public float _xpos;
    public float _ypos;
    public float _zpos;
    public float _travelled;

    public CacheItem(float xpos, float ypos, float zpos)
    {
        this._xpos = xpos;
        this._ypos = ypos;
        this._zpos = zpos;
    }
}