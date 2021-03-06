package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.nodetypes;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.ClassificationConstants;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.ICountListElem;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.OCStats;

public class SetAngleNode implements ICountListElem, ClassificationConstants
{
    private double  _angle;
    private ICountListElem _next;

    public SetAngleNode(Vector2D prev, Vector2D cur)
    {
         _angle = Vector2D.computeAngle(prev, cur);
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
        return ClassificationConstants.setAngleHashValue(_angle);
    }

    @Override
    public double getNumericalValue()
    {
        return _angle;
    }

    @Override
    public String toString()
    {
        return String.format("|SAngle: %.2f|", Math.toDegrees(_angle));
    }

    @Override
    public double getNormyValue()
    {
        return Math.toDegrees(_angle);
    }

    @Override
    public void accept(OCStats stats)
    {
        stats.visit(this);
    }

    @Override
    public int compareTo(ICountListElem o)
    {
        if(o instanceof SetAngleNode)
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
