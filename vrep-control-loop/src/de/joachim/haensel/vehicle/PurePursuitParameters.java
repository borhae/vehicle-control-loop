package de.joachim.haensel.vehicle;

public class PurePursuitParameters
{
    private double _lookahead;

    public PurePursuitParameters(double lookahead)
    {
        _lookahead = lookahead;
    }
    
    public double getLookahead()
    {
        return _lookahead;
    }
}
