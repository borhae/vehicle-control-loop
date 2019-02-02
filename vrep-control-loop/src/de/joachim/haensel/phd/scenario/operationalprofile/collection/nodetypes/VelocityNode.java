package de.joachim.haensel.phd.scenario.operationalprofile.collection.nodetypes;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.ClassificationConstants;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.ICountListElem;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.OCStats;
import de.joachim.haensel.phd.scenario.operationalprofile.collection.ObservationTuple;

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
        return String.format("|Velo: %.2f|", UnitConverter.meterPerSecondToKilometerPerHour(_velocity));
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
        if(o instanceof VelocityNode)
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
