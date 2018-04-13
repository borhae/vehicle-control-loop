package de.joachim.haensel.phd.scenario.sumo2vrep;

import sumobindings.EdgeType;
import sumobindings.LaneType;

public class Edge
{
    private EdgeType _sumoEdge;

    public Edge(EdgeType sumoEdge)
    {
        _sumoEdge = sumoEdge;
    }

    public Float getLength()
    {
        LaneType firstLane = _sumoEdge.getLane().get(0);
        return firstLane.getLength();
    }

    public EdgeType getSumoEdge()
    {
        return _sumoEdge;
    }
}
