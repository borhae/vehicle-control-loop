package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
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
        List<Line2D> route = getRoute(startJunction, targetJunction, startEdge, targetEdge);
        return route;
    }

    public List<Line2D> getRoute(JunctionType startJunction, JunctionType targetJunction, EdgeType startEdge, EdgeType targetEdge)
    {
        IShortestPathAlgorithm shortestPathSolver = new DijkstraAlgo(_roadMap);
        shortestPathSolver.setSource(startJunction);
        shortestPathSolver.setTarget(targetJunction);
        List<Node> path = shortestPathSolver.getPath();
        List<Line2D> result = createLinesFromPath(path, startEdge, targetEdge);
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
        boolean resultContainsSourcePosition = false;
        for(int idx = 0; idx < result.size(); idx++)
        {
            if(result.get(idx).contains(_sourcePosition, 0.5))
            {
                resultContainsSourcePosition = true;
                break;
            }
        }
        if(resultContainsSourcePosition)
        {
            for(Line2D curLine = result.get(0); !curLine.contains(_sourcePosition, 0.5); curLine = result.get(0))
            {
                result.remove(0);
                if(result.isEmpty())
                {
                    break;
                }
            }
        }
        boolean resultContainsTargetPosition = false;
        for(int idx = 0; idx < result.size(); idx++)
        {
            if(result.get(idx).contains(_targetPosition, 0.5))
            {
                resultContainsTargetPosition = true;
                break;
            }
        }
        if(resultContainsTargetPosition)
        {
            for(Line2D curLine = result.get(result.size() - 1); !curLine.contains(_targetPosition, 0.5); curLine = result.get(result.size() - 1))
            {
                result.remove(result.size() - 1);
                if(result.isEmpty())
                {
                    break;
                }
            }
        }
        result.get(0).setP1(_sourcePosition);
        result.get(result.size() - 1).setP2(_targetPosition);
//        List<Line2D> beginningSections = computeSectionsBetweenPosAndNode(path.get(0), _sourcePosition, true);
//        if(beginningSections != null)
//        {
//            result.addAll(0, beginningSections);
//        }
//        List<Line2D> endingSections = computeSectionsBetweenPosAndNode(path.get(path.size() - 1), _targetPosition, false);
//        Collections.reverse(endingSections);
//        if((endingSections != null) && !endingSections.isEmpty())
//        {
//            result.addAll(result.size(), endingSections);
//        }
        return result;
    }

    /** 
     * Looks up the incoming (or outgoing) lane from given target node that is closest to the given source position.
     * It then returns the parts of the sections of the lane that are between the position
     * and the node
     * Assumption: later elements in line shape are closer to node
     * @param node target node
     * @param position source position
     * @return
     */
    private List<Line2D> computeSectionsBetweenPosAndNode(Node node, Position2D position, boolean computeForIncomming)
    {
        Collection<Edge> edgesFromNode;
        if(computeForIncomming)
        {
            edgesFromNode = node.getIncomingEdges();
        }
        else
        {
            edgesFromNode = node.getOutgoingEdges();
        }
        List<Line2D> closestResolvedLane = null;
        int closestSectionIdx = -1;
        
        double closestDistance = Double.MAX_VALUE;
        
        for(Edge curEdge : edgesFromNode)
        {
            List<LaneType> lanes = curEdge.getSumoEdge().getLane();
            for (LaneType curLane : lanes)
            {
                String shape = curLane.getShape();
                if(shape == null)
                {
                    continue;
                }
                List<Line2D> laneSections = Line2D.createLines(shape);
                for (int idx = 0; idx < laneSections.size(); idx++)
                {
                    double curDistance = laneSections.get(idx).distance(position);
                    if(curDistance < closestDistance)
                    {
                        closestDistance = curDistance;
                        closestResolvedLane = laneSections;
                        closestSectionIdx = idx;
                    }
                }
            }
        }
        if(closestResolvedLane == null)
        {
            return null;
        }
        else
        {
            if(closestResolvedLane.size() == 1)
            {
                Line2D onlyLine = closestResolvedLane.get(0);
                if(node.distance(onlyLine.getP1()) <= node.distance(onlyLine.getP2()))
                {
                    onlyLine.setP2(position);
                }
                else
                {
                    onlyLine.setP1(position);
                }
                return closestResolvedLane;
            }
            else
            {
                // TODO check out which direction is shorter! this way we wouldn't need the assumption
                List<Line2D> lanesBetweenCurrentPositionAndFirstJunction = closestResolvedLane.subList(closestSectionIdx, closestResolvedLane.size());
                Line2D firstLane = lanesBetweenCurrentPositionAndFirstJunction.get(0);
                firstLane.setP1(position);
                return lanesBetweenCurrentPositionAndFirstJunction;
            }
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
