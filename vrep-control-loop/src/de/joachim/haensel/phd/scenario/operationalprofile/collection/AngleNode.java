package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class AngleNode implements ICountListElem, ClassificationConstants
{
    private double _angle;
    private ICountListElem _next;

    public AngleNode(ObservationTuple observationTuple, TrajectoryElement firstTrajectoryElement)
    {
        Position2D rearWheelCP = observationTuple.getRearWheelCP();
        Position2D frontWheelCP = observationTuple.getFrontWheelCP();
        Vector2D vehicleVector = new Vector2D(rearWheelCP, frontWheelCP);
        Vector2D trajectoryStartVector = firstTrajectoryElement.getVector();
        _angle = Vector2D.computeAngle(vehicleVector, trajectoryStartVector);
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
        return ClassificationConstants.angleHashValue(_angle);
    }

    @Override
    public double getNumericalValue()
    {
        return _angle;
    }

    @Override
    public String toString()
    {
        return String.format("|Angle: %.2f|", _angle);
    }
}
