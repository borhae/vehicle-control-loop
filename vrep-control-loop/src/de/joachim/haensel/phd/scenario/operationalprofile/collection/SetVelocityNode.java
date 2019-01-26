package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import de.joachim.haensel.phd.converters.UnitConverter;

public class SetVelocityNode implements ICountListElem, ClassificationConstants
{
    private double _velocity;
    private ICountListElem _next;

    public SetVelocityNode(double velocity)
    {
        _velocity = velocity;
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

    @Override
    public int getHashRangeIdx()
    {
        return ClassificationConstants.setVelocityHashValue(_velocity);
    }

    @Override
    public double getNumericalValue()
    {
        return _velocity;
    }

    @Override
    public String toString()
    {
        return String.format("|SVelo: %.2f|", UnitConverter.meterPerSecondToKilometerPerHour(_velocity));
    }
}
