package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.map.Edge;
import de.joachim.haensel.phd.scenario.map.IStreetSection;
import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.Linspace;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class Navigator
{
    private RoadMap _roadMap;
    private List<IRouteBuildingListener> _routeBuildingListeners;
    private Position2D _sourcePosition;
    private Position2D _targetPosition;

    public Navigator(RoadMap roadMap)
    {
        _roadMap = roadMap;
        _routeBuildingListeners = new ArrayList<>();
    }
    
    public List<Line2D> getRoute(Position2D currentPosition, Position2D targetPosition)
    {
        _sourcePosition = currentPosition;
        _targetPosition = targetPosition;
        EdgeType startEdge = _roadMap.getClosestEdgeFor(currentPosition);
        EdgeType targetEdge = _roadMap.getClosestEdgeFor(targetPosition);
        JunctionType startJunction = _roadMap.getJunctionForName(startEdge.getTo());
        JunctionType targetJunction = _roadMap.getJunctionForName(targetEdge.getFrom());
        List<Line2D> route = getRoute(startJunction, targetJunction, startEdge, targetEdge, null);
        return route;
    }
    
    public List<Line2D> getRouteWithInitialOrientation(Position2D currentPosition, Position2D targetPosition, Vector2D orientation)
    {
        _sourcePosition = currentPosition;
        _targetPosition = targetPosition;
        EdgeType startEdge = _roadMap.getClosestEdgeForOrientationRestricted(currentPosition, orientation);
        EdgeType targetEdge = _roadMap.getClosestEdgeFor(targetPosition);
        JunctionType startJunction = _roadMap.getJunctionForName(startEdge.getTo());
        JunctionType targetJunction = _roadMap.getJunctionForName(targetEdge.getFrom());
        List<Line2D> route = getRoute(startJunction, targetJunction, startEdge, targetEdge, orientation);
        System.out.println(" Routing done");
        return route;
    }

    public List<Line2D> getRoute(JunctionType startJunction, JunctionType targetJunction, EdgeType startEdge, EdgeType targetEdge, Vector2D orientation)
    {
        IShortestPathAlgorithm shortestPathSolver = new DijkstraAlgo(_roadMap);
        shortestPathSolver.setSource(startJunction);
        shortestPathSolver.setTarget(targetJunction);
        List<Node> path = shortestPathSolver.getPath();
        notifyListeners(path, startEdge, targetEdge);
        List<Line2D> result = createLinesFromPath(path, startEdge, targetEdge, orientation);
        notifyListeners(result);
        return result;
    }

    private void notifyListeners(List<Node> path, EdgeType startEdge, EdgeType targetEdge)
    {
        List<IStreetSection> completePath = new ArrayList<>();
        completePath.add(_roadMap.getEdgeForEdgeType(startEdge));
        completePath.addAll(path);
        completePath.add(_roadMap.getEdgeForEdgeType(targetEdge));
        
        _routeBuildingListeners.forEach(listener -> listener.notifyNewRouteStreetSections(completePath));
    }

    private void notifyListeners(List<Line2D> result)
    {
        _routeBuildingListeners.forEach(listener -> listener.notifyNewRoute(result));
    }

    private List<Line2D> createLinesFromPath(List<Node> path, EdgeType startEdge, EdgeType targetEdge, Vector2D orientation)
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
        for(int idx = 0; idx < edges.size(); idx++)
        {
            EdgeType curEdge = edges.get(idx);
            regularLineAdd(result, curEdge);
        }
        // TODO start and end-points could also be literally on a crossing, I did not took care for that yet
        result = cutStartLaneShapes(result, orientation);
        result = cutEndLaneShapes(result);
        result.get(0).setP1(_sourcePosition);
        result.get(result.size() - 1).setP2(_targetPosition);
        return result;
    }

    private List<Line2D> regularLineAdd(List<Line2D> result, EdgeType curEdge)
    {
        List<LaneType> lanes = curEdge.getLane();
        String shape = lanes.get(0).getShape();
        List<Line2D> linesToAdd = Line2D.createLines(shape);
        result.addAll(linesToAdd);
        return linesToAdd;
    }
    
    private List<Line2D> cutStartLaneShapes(List<Line2D> result, Vector2D orientation)
    {
        Position2D position = _sourcePosition;
        double curDist = Double.MAX_VALUE;
        int smallestElementsBufferSize = 10;
        double[] mindDistances = new double[smallestElementsBufferSize];
        Integer[] closestLinesIdx = new Integer[smallestElementsBufferSize];
        Arrays.fill(mindDistances, Double.POSITIVE_INFINITY);
        Arrays.fill(closestLinesIdx, Integer.MAX_VALUE);
        for(int resultLineIdx = 0; resultLineIdx < result.size(); resultLineIdx++)
        {
            curDist = result.get(resultLineIdx).distancePerpendicularOrEndpoints(position);
            if(curDist < mindDistances[mindDistances.length - 1])
            {
                for(int distIdx = 0; distIdx < mindDistances.length; distIdx++)
                {
                    if(curDist < mindDistances[distIdx])
                    {
                        double prevDist = mindDistances[distIdx];
                        int prevLineIdx = closestLinesIdx[distIdx];
                        double tmp = 0.0;
                        int tmpLineIdx = Integer.MAX_VALUE;
                        for(int updateIdx = distIdx + 1; updateIdx < mindDistances.length; updateIdx++)
                        {
                            tmp = mindDistances[updateIdx];
                            tmpLineIdx = closestLinesIdx[updateIdx];
                            mindDistances[updateIdx] = prevDist;
                            closestLinesIdx[updateIdx] = prevLineIdx;
                            prevDist = tmp;
                            prevLineIdx = tmpLineIdx;
                        }
                        mindDistances[distIdx] = curDist;
                        closestLinesIdx[distIdx] = resultLineIdx;
                        break;
                    }
                }
            }
        }
        List<Integer> alignedLinesIdxs = new ArrayList<>();
        if(orientation == null)
        {
            alignedLinesIdxs = Arrays.asList(closestLinesIdx);
        }
        else
        {
            for (int idx = 0; idx < closestLinesIdx.length; idx++)
            {
                int cur = closestLinesIdx[idx];
                if(cur == Integer.MAX_VALUE)
                {
                    continue;
                }
                Line2D curLine = result.get(cur);
                if(Vector2D.computeAngle(new Vector2D(curLine), orientation) < Math.toRadians(120))
                {
                    alignedLinesIdxs.add(cur);
                }
            }
        }
        if(alignedLinesIdxs.isEmpty())
        {
            return null;
        }
        else
        {
            result = result.subList(alignedLinesIdxs.get(0), result.size());
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

    public void addRouteBuildingListeners(List<IRouteBuildingListener> segmentBuildingListeners)
    {
        _routeBuildingListeners = segmentBuildingListeners;
    }
}
