package de.joachim.haensel.phd.scenario.tasks;

import java.util.concurrent.TimeUnit;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.control.BlockingArrivedListener;

public class DriveAtoBTask implements ITask
{
    private double _xSource;
    private double _ySource;
    private double _xTarget;
    private double _yTarget;
    private int _timeoutSec;
    private RoadMap _map;
    private IVehicleProvider _vehicle;

    public DriveAtoBTask(double xS, double yS, double xT, double yT, int timeoutSec, IVehicleProvider vehicle, RoadMap map)
    {
        _xSource = xS;
        _ySource = yS;
        _xTarget = xT;
        _yTarget = yT;
        _timeoutSec = timeoutSec;
        _vehicle = vehicle;
        _map = map;
    }

    public DriveAtoBTask(Position2D source, Position2D target, int timeoutSec, IVehicleProvider vehicle, RoadMap map)
    {
        this(source.getX(), source.getY(), target.getX(), target.getY(), timeoutSec, vehicle, map);
    }

    @Override
    public int getTimeout()
    {
        return _timeoutSec;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof DriveAtoBTask)
        {
            DriveAtoBTask task = (DriveAtoBTask)obj;
            if((task._xSource == _xSource) && (task._xTarget == _xTarget) && (task._ySource == _ySource) && (task._yTarget == _yTarget))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return String.format("source: (%f, %f), target: (%f, %f)", _xSource, _ySource, _xTarget, _yTarget);
    }

    public Position2D getSource()
    {
        return new Position2D(_xSource, _ySource);
    }

    public Position2D getTarget()
    {
        return new Position2D(_xTarget, _yTarget);
    }

    @Override
    public void execute()
    {
        try
        {
            driveSourceToTarget(getSource(), getTarget(), _map, _vehicle.getVehicle(), getTimeout());
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }
    
    private void driveSourceToTarget(Position2D source, Position2D target, RoadMap map, IVehicle vehicle, int timoutInSeconds) throws VRepException
    {
        BlockingArrivedListener listener = new BlockingArrivedListener(timoutInSeconds, TimeUnit.SECONDS);
        vehicle.driveTo(target.getX(), target.getY(), map, listener);
        listener.waitForArrival();
    }
}
