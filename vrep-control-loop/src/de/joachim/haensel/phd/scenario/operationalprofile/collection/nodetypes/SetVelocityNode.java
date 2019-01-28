package de.joachim.haensel.phd.scenario.operationalprofile.collection.nodetypes;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.ClassificationConstants;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.ICountListElem;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.OCStats;

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

    @Override
    public double getNormyValue()
    {
        return UnitConverter.meterPerSecondToKilometerPerHour(_velocity);
    }

    @Override
    public void accept(OCStats stats)
    {
        stats.visit(this);
    }
}
