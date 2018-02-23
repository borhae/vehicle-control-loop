package de.joachim.haensel.phd.scenario.math;

public class Triangle
{
    private float _a;
    private float _c;
    private double _alpha;
    private double _gamma;
    private double _beta;
    private double _b;

    public void setA(float a)
    {
        _a = a;
    }

    public void setC(float c)
    {
        _c = c;
    }

    public void setAlpha(double alpha)
    {
        _alpha = alpha;
    }

    public float getB()
    {
        double alphaByA = Math.sin(_alpha)/_a;
        double cTimesSinAlphaByA = _c * alphaByA;
        _gamma  = Math.sin(cTimesSinAlphaByA);
        _beta = Math.PI * 2 - _alpha - _gamma;
        _b = _a * Math.sin(_beta) / Math.sin(_alpha);
        return (float)_b;
    }
}
