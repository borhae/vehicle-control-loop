package de.joachim.haensel.phd.scenario.vehicle.control.reactive.ppvadaptable;


public class AtomicSetActualError 
{
    private double _distanceErr;
    private double _angleErr;

    public static AtomicSetActualError INITIALIZATION_REQUEST = new AtomicSetActualError(0.0, 0.0);
    public static AtomicSetActualError NO_FEEDBACK = new AtomicSetActualError(0.0, 0.0);
    
    public AtomicSetActualError(double distanceErr, double angleErr)
    {
        _distanceErr = distanceErr;
        _angleErr = angleErr;
    }

    public AtomicSetActualError add(AtomicSetActualError other)
    {
        AtomicSetActualError result = new AtomicSetActualError(_distanceErr, _angleErr);
        result._distanceErr += other._distanceErr;
        result._angleErr += other._angleErr;
        return result;
    }

    public double getDistanceErr()
    {
        return _distanceErr;
    }

    public void setDistanceErr(double distanceErr)
    {
        _distanceErr = distanceErr;
    }

    public double getAngleErr()
    {
        return _angleErr;
    }

    public void setAngleErr(double angleErr)
    {
        _angleErr = angleErr;
    }
}
