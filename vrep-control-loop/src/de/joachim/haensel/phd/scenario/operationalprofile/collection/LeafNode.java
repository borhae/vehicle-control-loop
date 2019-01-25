package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class LeafNode implements ICountListElem
{
    private List<TrajectoryElement> _trajectory;
    private ObservationTuple _observationTuple;
    private ICountListElem _next;

    public LeafNode(List<TrajectoryElement> trajectory, ObservationTuple observationTuple)
    {
        _trajectory = trajectory;
        _observationTuple = observationTuple;
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
        return 0;
    }

    @Override
    public double getNumericalValue()
    {
        return 0;
    }

    @Override
    public String toString()
    {
        return "Leaf";
    }
}
