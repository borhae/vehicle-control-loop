package de.joachim.haensel.phd.scenario.vehicle.experiment;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class RecordedTrajectoryElement 
{
    private Position2D _pos;
    private long _simTime;
    private long _sysTime;

    public RecordedTrajectoryElement()
    {
    }
    
    public RecordedTrajectoryElement(Position2D pos, long simTime, long sysTime)
    {
        _pos = pos;
        _simTime = simTime;
        _sysTime = sysTime;
    }

    public Position2D getPos()
    {
        return _pos;
    }

    public long getSimTime()
    {
        return _simTime;
    }

    public long getSysTime()
    {
        return _sysTime;
    }

    @Override
    public String toString()
    {
        return String.format("p: %s, simTime: %d, sysTime: %d", _pos.toString(), _simTime, _sysTime);
    }
}
