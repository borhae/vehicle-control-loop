package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;

public class VelocityNode implements ICountListElem, ClassificationConstants
{
    private double _velocity;
    private ICountListElem _next;

    public VelocityNode(ObservationTuple observationTuple)
    {
        double[] rawVelocity = observationTuple.getVelocity();
        _velocity = new Line2D(0.0, 0.0, rawVelocity[0], rawVelocity[1]).length();
    }

    @Override
    public void setNext(ICountListElem elem)
    {
        _next = elem;
    }

    @Override
    public ICountListElem next()
    {
        return _next;
    }

    public double getVelocity()
    {
        return _velocity;
    }

    @Override
    public int getHashRangeIdx()
    {
        return ClassificationConstants.velocityHashValue(_velocity);
    }

    @Override
    public double getNumericalValue()
    {
        return _velocity;
    }

    @Override
    public String toString()
    {
        return String.format("|Velo: %.2f|", _velocity);
    }
}
