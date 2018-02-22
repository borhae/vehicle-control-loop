package de.joachim.haensel.phd.scenario.math.bezier;

public class Cubic
{
    private float _a;
    private float _b;
    private float _c;
    private float _d;

    public Cubic(float a, float b, float c, float d)
    {
        _a = a;
        _b = b;
        _c = c;
        _d = d;
    }

    public float eval(float u)
    {
        return (((_d * u) + _c) * u + _b) * u + _a;
    }
}