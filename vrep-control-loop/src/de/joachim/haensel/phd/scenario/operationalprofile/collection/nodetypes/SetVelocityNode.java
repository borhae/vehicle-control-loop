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

    @Override
    public int compareTo(ICountListElem o)
    {
        if(o instanceof SetVelocityNode)
        {
            double comparison = getNumericalValue() - o.getNumericalValue();
            if(Math.abs(comparison) <  EPSILON)
            {
                if(_next != null && o.next() != null)
                {
                    return _next.compareTo(o.next());
                }
                else
                {
                    return 0;
                }
            }
            return (int) comparison;
        }
        else
        {
            return Integer.MAX_VALUE;
        }
    }
}
