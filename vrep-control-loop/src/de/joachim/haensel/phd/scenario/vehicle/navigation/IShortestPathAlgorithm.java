package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.List;

import de.joachim.haensel.phd.scenario.sumo2vrep.Node;
import sumobindings.JunctionType;

public interface IShortestPathAlgorithm
{

    public void setSource(JunctionType startJunction);

    public void setTarget(JunctionType targetJunction);

    public List<Node> getPath();

}