package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

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
}
