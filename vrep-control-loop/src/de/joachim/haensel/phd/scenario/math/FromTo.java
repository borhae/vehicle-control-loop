package de.joachim.haensel.phd.scenario.math;

public class FromTo
{
    private double _from;
    private double _to;

    public FromTo(double from, double to)
    {
        _from = from;
        _to = to;
    }

    public double getTo()
    {
        return _to;
    }

    public double getFrom()
    {
        return _from;
    }
}
