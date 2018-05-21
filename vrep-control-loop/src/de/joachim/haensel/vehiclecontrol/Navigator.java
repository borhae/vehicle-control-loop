package de.joachim.haensel.vehiclecontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.Edge;
import de.joachim.haensel.phd.scenario.sumo2vrep.Node;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
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
    private Position2D _currentPosition;

    public Navigator(RoadMap roadMap)
    {
        _roadMap = roadMap;
        _segmentBuildingListeners = new ArrayList<>();
    }
    
    public List<Line2D> getRoute(Position2D currentPosition, Position2D targetPosition)
    {
        _currentPosition = currentPosition;
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

        List<Line2D> beginningSections = computeSectionsBetweenPosAndNode(path.get(0), _currentPosition);
        if(beginningSections != null)
        {
            result.addAll(0, beginningSections);
        }
        return result;
    }

    /** 
     * Looks up the incoming lane from given target node that is closest to the given source position.
     * It then returns the parts of the sections of the lane that are between the position
     * and the node
     * Assumption: later elements in line shape are closer to node
     * @param node target node
     * @param position source position
     * @return
     */
    private List<Line2D> computeSectionsBetweenPosAndNode(Node node, Position2D position)
    {
        Collection<Edge> incommingEdges = node.getIncomingEdges();
        List<Line2D> closestResolvedLane = null;
        int closestSectionIdx = -1;
        
        double closestDistance = Double.MAX_VALUE;
        
        for(Edge curEdge : incommingEdges)
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
            List<Line2D> lanesBetweenCurrentPositionAndFirstJunction = closestResolvedLane.subList(closestSectionIdx, closestResolvedLane.size());
            return lanesBetweenCurrentPositionAndFirstJunction;
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
