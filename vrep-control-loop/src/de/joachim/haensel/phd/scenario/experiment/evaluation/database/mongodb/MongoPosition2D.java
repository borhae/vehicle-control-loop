package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class MongoPosition2D
{
    private double _x;
    private double _y;
    
    public MongoPosition2D()
    {
    }
    
    public MongoPosition2D(Position2D pos)
    {
        _x = pos.getX();
        _y = pos.getY();
    }

    public double getX()
    {
        return _x;
    }

    public void setX(double x)
    {
        _x = x;
    }

    public double getY()
    {
        return _y;
    }

    public void setY(double y)
    {
        _y = y;
    }

    public Position2D decode()
    {
        return new Position2D(_x, _y);
    }
}
