package de.joachim.haensel.vehiclecontrol;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.sumo2vrep.Edge;
import de.joachim.haensel.sumo2vrep.Node;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vehicle.ISegmentBuildingListener;
import de.joachim.haensel.vehiclecontrol.navigation.DijkstraAlgo;
import de.joachim.haensel.vehiclecontrol.navigation.IShortestPathAlgorithm;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class Navigator
{
    private RoadMap _roadMap;
    private List<ISegmentBuildingListener> _segmentBuildingListeners;

    public Navigator(RoadMap roadMap)
    {
        _roadMap = roadMap;
        _segmentBuildingListeners = new ArrayList<>();
    }
    
    public List<Line2D> getRoute(Position2D currentPosition, Position2D targetPosition)
    {
        JunctionType startJunction = _roadMap.getClosestJunctionFor(currentPosition);
        JunctionType targetJunction = _roadMap.getClosestJunctionFor(targetPosition);
        return getRoute(startJunction, targetJunction);
    }

    public List<Line2D> getRoute(JunctionType startJunction, JunctionType targetJunction)
    {
        IShortestPathAlgorithm shortestPathSolver = new DijkstraAlgo(_roadMap);
        shortestPathSolver.setSource(startJunction);
        shortestPathSolver.setTarget(targetJunction);
        List<Node> path = shortestPathSolver.getPath();
        List<Line2D> result = createLinesFromPath(path);
        notifyListeners(result);
        return result;
    }
    
    private void notifyListeners(List<Line2D> result)
    {
        _segmentBuildingListeners.forEach(listener -> listener.notifyNewRoute(result));
    }

    private List<Line2D> createLinesFromPath(List<Node> path)
    {
        List<Line2D> result = new ArrayList<>();
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
            result.addAll(Line2D.createLines(shape));
        }
        return result;
    }

    private EdgeType getEdgeBetween(Node cur, Node next)
    {
        Edge edgeToNext = cur.getOutgoingEdge(next);
        return edgeToNext.getSumoEdge();
    }

    public void addSegmentBuildingListeners(List<ISegmentBuildingListener> segmentBuildingListeners)
    {
        _segmentBuildingListeners = segmentBuildingListeners;
    }
}
