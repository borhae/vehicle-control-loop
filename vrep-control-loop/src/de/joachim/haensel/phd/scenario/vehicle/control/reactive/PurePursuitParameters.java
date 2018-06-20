package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

public class PurePursuitParameters
{
    private double _lookahead;
    private double _v;
    private double _speedToWheelRotationFactor;

    public PurePursuitParameters(double lookahead, double speedToWheelRotationFactor)
    {
        _lookahead = lookahead;
        _speedToWheelRotationFactor = speedToWheelRotationFactor;
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

    public double getSpeedToWheelRotationFactor()
    {
        return _speedToWheelRotationFactor;
    }
}
