package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.Edge;
import de.joachim.haensel.phd.scenario.sumo2vrep.Node;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.ISegmentBuildingListener;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class Navigator
{
    private RoadMap _roadMap;
    private List<ISegmentBuildingListener> _segmentBuildingListeners;
    private Position2D _sourcePosition;
    private Position2D _targetPosition;

    public Navigator(RoadMap roadMap)
    {
        _roadMap = roadMap;
        _segmentBuildingListeners = new ArrayList<>();
    }
    
    public List<Line2D> getRoute(Position2D currentPosition, Position2D targetPosition)
    {
        _sourcePosition = currentPosition;
        _targetPosition = targetPosition;
        EdgeType startEdge = _roadMap.getClosestEdgeFor(currentPosition);
        EdgeType targetEdge = _roadMap.getClosestEdgeFor(targetPosition);
        JunctionType startJunction = _roadMap.getJunctionForName(startEdge.getTo());
        JunctionType targetJunction = _roadMap.getJunctionForName(targetEdge.getFrom());
        System.out.print("Start routing: without orientation with start and target edge");
        List<Line2D> route = getRoute(startJunction, targetJunction, startEdge, targetEdge);
        return route;
    }
    
    public List<Line2D> getRouteWithInitialOrientation(Position2D currentPosition, Position2D targetPosition, Vector2D orientation)
    {
        System.out.print("Start routing: looking for closest start lane with correct orientation");
        _sourcePosition = currentPosition;
        _targetPosition = targetPosition;
        EdgeType startEdge = _roadMap.getClosestEdgeForOrientationRestricted(currentPosition, orientation);
        System.out.print("done, looking for closest target orientation");
        EdgeType targetEdge = _roadMap.getClosestEdgeFor(targetPosition);
        JunctionType startJunction = _roadMap.getJunctionForName(startEdge.getTo());
        JunctionType targetJunction = _roadMap.getJunctionForName(targetEdge.getFrom());
        System.out.print(" done, now the actual routing: ");
        List<Line2D> route = getRoute(startJunction, targetJunction, startEdge, targetEdge);
        System.out.println(" Routing done");
        return route;
    }

    public List<Line2D> getRoute(JunctionType startJunction, JunctionType targetJunction, EdgeType startEdge, EdgeType targetEdge)
    {
        IShortestPathAlgorithm shortestPathSolver = new DijkstraAlgo(_roadMap);
        shortestPathSolver.setSource(startJunction);
        shortestPathSolver.setTarget(targetJunction);
        List<Node> path = shortestPathSolver.getPath();
        System.out.print(" route found, now turning junction path into actual path... ");
        List<Line2D> result = createLinesFromPath(path, startEdge, targetEdge);
        System.out.print(", actual path computed, now notifying listeners");
        notifyListeners(result);
        return result;
    }
    
    private void notifyListeners(List<Line2D> result)
    {
        _segmentBuildingListeners.forEach(listener -> listener.notifyNewRoute(result));
    }

    private List<Line2D> createLinesFromPath(List<Node> path, EdgeType startEdge, EdgeType targetEdge)
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
        edges.add(0, startEdge);
        edges.add(targetEdge);
        for (EdgeType curEdge : edges)
        {
            List<LaneType> lanes = curEdge.getLane();
            String shape = lanes.get(0).getShape();
            result.addAll(Line2D.createLines(shape));
        }
        // epsilon has high tolerance since the start and endpoints might not exactly be on the street.
        // endpoints could also be literally on a crossing, I did not took care for that yet
        result = cutStartLaneShapes(result);
        result = cutEndLaneShapes(result);
        result.get(0).setP1(_sourcePosition);
        result.get(result.size() - 1).setP2(_targetPosition);
        return result;
    }
    
    private List<Line2D> cutStartLaneShapes(List<Line2D> result)
    {
        double minDist = Double.POSITIVE_INFINITY;
        double curDist = Double.POSITIVE_INFINITY;
        int minIdx = Integer.MAX_VALUE;
        for (int idx = 0; idx < result.size(); idx++)
        {
            Line2D curLine = result.get(idx);
            curDist = curLine.perpendicularDistanceWithEndpointLimit(_sourcePosition, 3.0);
            if(curDist < minDist)
            {
                minDist = curDist;
                minIdx = idx;
            }
        }
        if(minDist < Double.POSITIVE_INFINITY)
        {
            return result.subList(minIdx, result.size());
        }
        else
        {
            return result;
        }
    }
    
    private List<Line2D> cutEndLaneShapes(List<Line2D> result)
    {
        double minDist = Double.POSITIVE_INFINITY;
        double curDist = Double.POSITIVE_INFINITY;
        int minIdx = Integer.MAX_VALUE;
        for (int idx = 0; idx < result.size(); idx++)
        {
            Line2D curLine = result.get(idx);
            curDist = curLine.perpendicularDistanceWithEndpointLimit(_targetPosition, 3.0);
            if(curDist < minDist)
            {
                minDist = curDist;
                minIdx = idx;
            }
        }
        if(minDist < Double.POSITIVE_INFINITY)
        {
            return result.subList(0, minIdx + 1);
        }
        else
        {
            return result;
        }
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
