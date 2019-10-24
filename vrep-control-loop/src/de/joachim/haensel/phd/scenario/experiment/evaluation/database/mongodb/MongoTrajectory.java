package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import org.bson.Document;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.IDatabaseTrajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement.VelocityEdgeType;

public class MongoTrajectory implements IDatabaseTrajectory
{
    private MongoVector2D _vector2D;
    private double _velocity;
    private VelocityEdgeType _riseFall;
    private double _radius;
    private double _kappa;
    private int _idx;

    public MongoTrajectory()
    {
    }
    
    public MongoTrajectory(TrajectoryElement trajElem)
    {
        _vector2D = new MongoVector2D(trajElem.getVector());
        _velocity = trajElem.getVelocity();
        _riseFall = trajElem.getRiseFall();
        _radius = trajElem.getRadius();
        _kappa = trajElem.getKappa();
        _idx = trajElem.getIdx();
    }

    public MongoTrajectory(Document doc)
    {
        _vector2D = doc.get("vector2D", MongoVector2D.class);
        Double velocity = doc.getDouble("velocity");
        _velocity = velocity == null ? 0.0d : velocity.doubleValue();
        _riseFall = VelocityEdgeType.valueOf(doc.getString("riseFall"));
        Double radius = doc.getDouble("radius");
        _radius = radius == null ? 0.0d : radius.doubleValue();
        Double kappa = doc.getDouble("kappa");
        _kappa = kappa == null ? 0.0d : kappa.doubleValue();
        _idx = doc.getInteger("idx", 0);
    }

    public TrajectoryElement decode()
    {
        TrajectoryElement result = new TrajectoryElement();
        result.setVector(_vector2D.decode());
        result.setVelocity(_velocity);
        result.setRiseFall(_riseFall);
        result.setRadius(_radius);
        result.setKappa(_kappa);
        result.setIdx(_idx);
        return result;
    }
    
    public int getIdx()
    {
        return _idx;
    }

    public void setIdx(int idx)
    {
        _idx = idx;
    }

    public MongoVector2D getVector2D()
    {
        return _vector2D;
    }

    public void setVector2D(MongoVector2D mongoVector2D)
    {
        _vector2D = mongoVector2D;
    }

    public double getVelocity()
    {
        return _velocity;
    }

    public void setVelocity(double velocity)
    {
        _velocity = velocity;
    }

    public VelocityEdgeType getRiseFall()
    {
        return _riseFall;
    }

    public void setRiseFall(VelocityEdgeType riseFall)
    {
        _riseFall = riseFall;
    }

    public double getRadius()
    {
        return _radius;
    }

    public void setRadius(double radius)
    {
        _radius = radius;
    }

    public double getKappa()
    {
        return _kappa;
    }

    public void setKappa(double kappa)
    {
        _kappa = kappa;
    }
}
