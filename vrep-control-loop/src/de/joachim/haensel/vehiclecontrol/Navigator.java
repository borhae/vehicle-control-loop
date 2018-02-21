package de.joachim.haensel.vehiclecontrol;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.sumo2vrep.Node;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.sumo2vrep.Segment;
import de.joachim.haensel.vehiclecontrol.navigation.DijkstraAlgo;
import de.joachim.haensel.vehiclecontrol.navigation.IShortestPathAlgorithm;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class Navigator
{
    private RoadMap _roadMap;

    public Navigator(RoadMap roadMap)
    {
        _roadMap = roadMap;
    }

    public List<Segment> getRoute(JunctionType startJunction, JunctionType targetJunction)
    {
        IShortestPathAlgorithm shortestPathSolver = new DijkstraAlgo(_roadMap);
        shortestPathSolver.setSource(startJunction);
        shortestPathSolver.setTarget(targetJunction);
        List<Node> path = shortestPathSolver.getPath();
        return createSegmentsFromPath(path);
    }

    private List<Segment> createSegmentsFromPath(List<Node> path)
    {
        List<Segment> result = new ArrayList<>();
        List<EdgeType> edges = new ArrayList<>();
        for(int idx = 0; idx < path.size() - 1; idx++)
        {
            Node cur = path.get(idx);
            Node next = path.get(idx + 1);
            EdgeType sumoEdge = getEdgeBetween(cur, next);
            edges.add(sumoEdge);
        }
        for (EdgeType curEdge : edges)
        {
            List<LaneType> lanes = curEdge.getLane();
            String shape = lanes.get(0).getShape();
            result.addAll(Segment.createSegments(shape));
        }
        return result;
    }

    private EdgeType getEdgeBetween(Node cur, Node next)
    {
        return cur.getOutgoingEdge(next).getSumoEdge();
    }
}
