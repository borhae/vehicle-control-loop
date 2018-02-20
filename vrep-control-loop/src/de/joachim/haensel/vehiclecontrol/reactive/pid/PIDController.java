package de.joachim.haensel.vehiclecontrol.reactive.pid;

public class PIDController
{
    private double _p;
    private double _i;
    private double _d;

    public PIDController(double p, double i, double d)
    {
        _p = p;
        _i = i;
        _d = d;
    }
}
