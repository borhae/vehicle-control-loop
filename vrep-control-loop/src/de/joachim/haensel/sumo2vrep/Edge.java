package de.joachim.haensel.sumo2vrep;

import sumobindings.EdgeType;

public class Edge
{
    private EdgeType _sumoEdge;

    public Edge(EdgeType sumoEdge)
    {
        _sumoEdge = sumoEdge;
    }

    public Float getLength()
    {
        return _sumoEdge.getLength();
    }

    public EdgeType getSumoEdge()
    {
        return _sumoEdge;
    }
}
