package de.joachim.haensel.phd.scenario.vrepdebugging;

public class DrawingObject
{
    private DrawingType _type;
    private int _handle;

    public DrawingObject(DrawingType type, int handle)
    {
        _type = type;
        _handle = handle;
    }

    public int getHandle()
    {
        return _handle;
    }
}
