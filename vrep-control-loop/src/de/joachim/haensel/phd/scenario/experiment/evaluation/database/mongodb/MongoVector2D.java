package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class MongoVector2D
{
    private double _bX;
    private double _bY;
    private double _length;
    private double _dX;
    private double _dY;
    private double _normX;
    private double _normY;

    public MongoVector2D()
    {
    }
    
    public MongoVector2D(Vector2D vector)
    {
        _bX = vector.getbX();
        _bY = vector.getbY();
        _length = vector.getLength();
        _dX = vector.getdX();
        _dY = vector.getdY();
        _normX = vector.getNormX();
        _normY = vector.getNormY();
    }

    public double getBX()
    {
        return _bX;
    }

    public void setBX(double bX)
    {
        _bX = bX;
    }

    public double getBY()
    {
        return _bY;
    }

    public void setBY(double bY)
    {
        _bY = bY;
    }

    public double getLength()
    {
        return _length;
    }

    public void setLength(double length)
    {
        _length = length;
    }

    public double getDX()
    {
        return _dX;
    }

    public void setDX(double dX)
    {
        _dX = dX;
    }

    public double getDY()
    {
        return _dY;
    }

    public void setDY(double dY)
    {
        _dY = dY;
    }

    public double getNormX()
    {
        return _normX;
    }

    public void setNormX(double normX)
    {
        _normX = normX;
    }

    public double getNormY()
    {
        return _normY;
    }

    public void setNormY(double normY)
    {
        _normY = normY;
    }
}
