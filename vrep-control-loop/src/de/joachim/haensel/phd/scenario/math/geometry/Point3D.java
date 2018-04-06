package de.joachim.haensel.phd.scenario.math.geometry;

public class Point3D
{
    private double[] _content;

    public Point3D(double x, double y, double z)
    {
        _content = new double[3];
        _content[0] = x;
        _content[1] = y;
        _content[2] = z;
    }

    public double[] getArray()
    {
        return _content;
    }
}
