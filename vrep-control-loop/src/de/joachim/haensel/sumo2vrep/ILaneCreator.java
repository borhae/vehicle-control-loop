package de.joachim.haensel.sumo2vrep;

import sumobindings.LaneType;

public interface ILaneCreator
{
    void create(LaneType curLane, String p1, String p2);
}
