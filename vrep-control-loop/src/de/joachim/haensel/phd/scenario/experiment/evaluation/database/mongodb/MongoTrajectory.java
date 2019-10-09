package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.IDatabaseTrajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement.VelocityEdgeType;

public class MongoTrajectory implements IDatabaseTrajectory
{
    private MongoVector2D _Vector2D;
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
        _Vector2D = new MongoVector2D(trajElem.getVector());
        _velocity = trajElem.getVelocity();
        _riseFall = trajElem.getRiseFall();
        _radius = trajElem.getRadius();
        _kappa = trajElem.getKappa();
        _idx = trajElem.getIdx();
        
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
        return _Vector2D;
    }

    public void setVector2D(MongoVector2D mongoVector2D)
    {
        _Vector2D = mongoVector2D;
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
