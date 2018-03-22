package de.joachim.haensel.vehiclecontrol.reactive.pid;

/**
 * TODO I might want to remove this class
 * @author dummy
 *
 */
public class PIDController
{
    private double _p;
    private double _i;
    private double _d;

    // good default: new PIDController(0.1, 0.001, 2.8);
    
    public PIDController(double p, double i, double d)
    {
        _p = p;
        _i = i;
        _d = d;
    }
}
