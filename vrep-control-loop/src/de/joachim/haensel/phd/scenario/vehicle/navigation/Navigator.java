package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private static final double MAX_DISTANCE_BETWEEN_FOLLOW_UP_LANES = 0.5;
    
    private RoadMap _roadMap;
    private List<IRouteBuildingListener> _routeBuildingListeners;
    private Position2D _sourcePosition;
    private Position2D _targetPosition;
    private double _segmentSize;

    public Navigator(RoadMap roadMap)
    {
        _roadMap = roadMap;
        _routeBuildingListeners = new ArrayList<>();
    }

    public void setSourceTarget(Position2D source, Position2D target)
    {
        _sourcePosition = source;
        _targetPosition = target;
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
    
    public List<Line2D> getRouteWithInitialOrientation(Position2D currentPosition, Position2D targetPosition, double segmentSize, Vector2D orientation)
    {
        _sourcePosition = currentPosition;
        _targetPosition = targetPosition;
        _segmentSize = segmentSize;
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
        List<Node> path = computePath(startJunction, targetJunction);
        if(path != null)
        {
            notifyListeners(path, startEdge, targetEdge);
            List<Line2D> result = createLinesFromPath(path, startEdge, targetEdge);
            notifyListeners(result);
            return result;
        }
        else
        {
            //no path found
            return null;
        }
    }
    
    //TODO public only for testing
    public List<Node> computePath(JunctionType startJunction, JunctionType targetJunction)
    {
        IShortestPathAlgorithm shortestPathSolver = new DijkstraAlgo(_roadMap);
        shortestPathSolver.setSource(startJunction);
        shortestPathSolver.setTarget(targetJunction);
        List<Node> path = shortestPathSolver.getPath();
        return path;
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

    public List<Line2D> createLinesFromPath(List<Node> path, EdgeType startEdge, EdgeType targetEdge)
    {
        List<Line2D> result = createLinesFromPathNoSharpTurnRemoval(path, startEdge, targetEdge);
        return result;
    }

    public List<Line2D> traverse(List<Line2D> rawResult, IRouteAdaptor adaptor, IRouteProperyDetector routeProperty)
    {
        List<Line2D> result = new ArrayList<>();
        for(int idx = 0; idx < rawResult.size(); idx++)
        {
            Line2D curLine = rawResult.get(idx);
            result.add(curLine);
            if(idx + 1 < rawResult.size())
            {
                Line2D nextLine = rawResult.get(idx + 1);
                Vector2D curLineV = new Vector2D(curLine);
                Vector2D nextLineV = new Vector2D(nextLine);
                if(routeProperty.holds(rawResult, curLine, nextLine, curLineV, nextLineV))
                {
                    adaptor.adapt(result, curLine, nextLine, curLineV, nextLineV);
                }
            }
        }
        return result;
    }

    public List<Line2D> createLinesFromPathNoSharpTurnRemoval(List<Node> path, EdgeType startEdge, EdgeType targetEdge)
    {
        List<Line2D> result = new ArrayList<>();
        List<EdgeType> edges = transformNodePathToEdgePath(path, startEdge, targetEdge);
        for(int idx = 0; idx < edges.size(); idx++)
        {
            EdgeType curEdge = edges.get(idx);
            if(curEdge != targetEdge)
            {
                regularLineAdd(result, curEdge);
            }
            else
            {
                List<LaneType> lanes = curEdge.getLane();
                List<String> lanesAsString = lanes.stream().map(lane -> lane.getShape()).collect(Collectors.toList());
                List<List<Line2D>> lineSelectionToChooseFrom = lanesAsString.stream().map(shape -> Line2D.createLines(shape)).collect(Collectors.toList());
                double minDist = Double.POSITIVE_INFINITY;
                double curDist = Double.POSITIVE_INFINITY;
                List<Line2D> linesToAdd = lineSelectionToChooseFrom.get(0);
                for(List<Line2D> curShape : lineSelectionToChooseFrom)
                {
                    curShape = cutEndLaneShapes(curShape, targetEdge);
                    curDist = Position2D.distance(curShape.get(curShape.size() - 1).getP2(), _targetPosition);
                    if(curDist < minDist)
                    {
                        minDist = curDist;
                        linesToAdd = curShape;
                    }
                }
                result.addAll(linesToAdd);
            }
        }
        return result;
    }

    public List<EdgeType> transformNodePathToEdgePath(List<Node> path, EdgeType startEdge, EdgeType targetEdge)
    {
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
        return edges;
    }

    private List<Line2D> regularLineAdd(List<Line2D> result, EdgeType curEdge)
    {
        List<Line2D> linesToAdd = edgeToLines(curEdge);
        result.addAll(linesToAdd);
        return linesToAdd;
    }

    private List<Line2D> edgeToLines(EdgeType edge)
    {
        List<LaneType> lanes = edge.getLane();
        String shape = lanes.get(0).getShape();
        return Line2D.createLines(shape);
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
            result.get(0).setP1(_sourcePosition);
            return result;
        }
    }
    
    private List<Line2D> cutEndLaneShapes(List<Line2D> result, EdgeType targetEdge)
    {
        double minDist = Double.POSITIVE_INFINITY;
        double curDist = Double.POSITIVE_INFINITY;
        int minIdx = Integer.MAX_VALUE;
        
        Position2D targetIntersectionPoint = null;
        for (int idx = 0; idx < result.size(); idx++)
        {
            Line2D curLine = result.get(idx);
            Vector2D v = new Vector2D(curLine);
            Position2D intersectionPoint = Vector2D.getPerpendicularIntersection(v, _targetPosition);
            
            curDist = intersectionPoint == null ? Double.POSITIVE_INFINITY : Position2D.distance(intersectionPoint, _targetPosition);
            if(curDist < minDist)
            {
                minDist = curDist;
                minIdx = idx;
                targetIntersectionPoint = intersectionPoint;
            }
        }
        if(minDist < Double.POSITIVE_INFINITY && targetIntersectionPoint != null)
        {
            result.get(minIdx).setP2(targetIntersectionPoint);
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

    public void setSegmentSize(double segmentSize)
    {
        _segmentSize = segmentSize;
    }
}
