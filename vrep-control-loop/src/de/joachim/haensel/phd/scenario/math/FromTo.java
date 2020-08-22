package de.joachim.haensel.phd.scenario.math;

public class FromTo
{
    private double _from;
    private double _to;

    public FromTo()
    {
    }
    
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

    public void setFrom(double from)
    {
        _from = from;
    }

    public void setTo(double to)
    {
        _to = to;
    }
}
