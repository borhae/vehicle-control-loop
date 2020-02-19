package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.ObservationTuple;

public class MongoObservationTuple
{
    private MongoVector2D _orientation;
    private MongoPosition2D _frontWheelCP;
    private MongoPosition2D _rearWheelCP;
    private List<Double> _velocity;
    private long _timeStamp;

    public MongoObservationTuple()
    {
    }
    
    public MongoObservationTuple(ObservationTuple value)
    {
        _orientation = new MongoVector2D(value.getOrientation());
        _frontWheelCP = new MongoPosition2D(value.getFrontWheelCP());
        _rearWheelCP = new MongoPosition2D(value.getRearWheelCP());
        _velocity = Arrays.stream(value.getVelocity()).boxed().collect(Collectors.toList());
        _timeStamp = value.getTimeStamp();
    }

    public MongoVector2D getOrientation()
    {
        return _orientation;
    }

    public void setOrientation(MongoVector2D orientation)
    {
        _orientation = orientation;
    }

    public MongoPosition2D getFrontWheelCP()
    {
        return _frontWheelCP;
    }

    public void setFrontWheelCP(MongoPosition2D frontWheelCP)
    {
        _frontWheelCP = frontWheelCP;
    }

    public MongoPosition2D getRearWheelCP()
    {
        return _rearWheelCP;
    }

    public void setRearWheelCP(MongoPosition2D rearWheelCP)
    {
        _rearWheelCP = rearWheelCP;
    }

    public List<Double> getVelocity()
    {
        return _velocity;
    }

    public void setVelocity(List<Double> velocity)
    {
        _velocity = velocity;
    }

    public ObservationTuple decode()
    {
        return new ObservationTuple(_rearWheelCP.decode(), _frontWheelCP.decode(), toArray(_velocity), _timeStamp);
    }

    private double[] toArray(List<Double> list)
    {
        return list.stream().mapToDouble(d -> d).toArray();
    }
}
