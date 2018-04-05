package de.joachim.haensel.vehicle;

public class PurePursuitParameters
{
    private double _lookahead;
    private double _v;

    public PurePursuitParameters(double lookahead)
    {
        _lookahead = lookahead;
    }
    
    public double getLookahead()
    {
        return _lookahead;
    }

    public void setSpeed(double v)
    {
        _v = v;
    }

    public double getV()
    {
        return _v;
    }
}
