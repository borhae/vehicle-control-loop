package de.joachim.haensel.phd.scenario.sumo2vrep;

import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public interface IWholeLaneCreator
{
    void create(LaneType curLane, EdgeType curEdge, JunctionType fromJunction, JunctionType toJunction);
}
