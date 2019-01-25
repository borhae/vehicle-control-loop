package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class DisplacementNode implements ICountListElem, ClassificationConstants
{
    private double _displacement;
    private ICountListElem _next;

    public DisplacementNode(ObservationTuple observationTuple, TrajectoryElement trajectoryElement)
    {
        Position2D rearWheelCP = observationTuple.getRearWheelCP();
        Position2D frontWheelCP = observationTuple.getFrontWheelCP();
        Position2D vehicleCenter = Position2D.between(rearWheelCP, frontWheelCP);
        
        Vector2D trajectoryVector = trajectoryElement.getVector();
        
        Position2D intersection = Vector2D.getUnrangedPerpendicularIntersection(trajectoryVector, vehicleCenter);
        _displacement = Position2D.distance(vehicleCenter, intersection);
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
        return ClassificationConstants.displacementHashValue(_displacement);
    }

    @Override
    public double getNumericalValue()
    {
        return _displacement;
    }


    @Override
    public String toString()
    {
        return String.format("|Displ.: %.2f|", _displacement);
    }
}
