package de.joachim.haensel.phd.scenario.map.sumo2vrep;

import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public interface IWholeLaneCreator
{
    void create(LaneType curLane, EdgeType curEdge, JunctionType fromJunction, JunctionType toJunction);
}
