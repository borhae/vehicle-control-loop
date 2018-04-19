package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.bezier;

public class Cubic
{
    private double _a;
    private double _b;
    private double _c;
    private double _d;

    public Cubic(double a, double b, double c, double d)
    {
        _a = a;
        _b = b;
        _c = c;
        _d = d;
    }

    public double eval(double u)
    {
        return (((_d * u) + _c) * u + _b) * u + _a;
    }
}