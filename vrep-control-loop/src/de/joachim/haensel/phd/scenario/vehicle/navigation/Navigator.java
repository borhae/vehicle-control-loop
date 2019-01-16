package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.Linspace;
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
    private static final double U_TURN_RADIUS = 10.0;
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
    
    public List<Line2D> getRouteWithInitialOrientation(Position2D currentPosition, Position2D targetPosition, Vector2D orientation)
    {
        _sourcePosition = currentPosition;
        _targetPosition = targetPosition;
        EdgeType startEdge = _roadMap.getClosestEdgeForOrientationRestricted(currentPosition, orientation);
        EdgeType targetEdge = _roadMap.getClosestEdgeFor(targetPosition);
        JunctionType startJunction = _roadMap.getJunctionForName(startEdge.getTo());
        JunctionType targetJunction = _roadMap.getJunctionForName(targetEdge.getFrom());
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
        for(int idx = 0; idx < edges.size(); idx++)
        {
            EdgeType curEdge = edges.get(idx);
            regularLineAdd(result, curEdge);
        }
        // TODO start and end-points could also be literally on a crossing, I did not took care for that yet
        result = cutStartLaneShapes(result);
        result = cutEndLaneShapes(result);
        result.get(0).setP1(_sourcePosition);
        result.get(result.size() - 1).setP2(_targetPosition);
        result = remove180Turns(result);
        return result;
    }
    
    private List<Line2D> remove180Turns(List<Line2D> rawResult)
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
                Vector2D nextLineVN = new Vector2D(nextLine);
                double angle = Math.toDegrees(Vector2D.computeAngle(curLineV, nextLineVN));
                if(angle > 120) //TODO add the other direction too
                {
                    addTurnAroundCircle(result, curLine, nextLine);
                }
            }
        }
        return result;
    }

    private void addTurnAroundCircle(List<Line2D> result, Line2D curLine, Line2D nextLine)
    {
        Position2D p1 = curLine.getP2();
        Position2D p2 = nextLine.getP1();
        
        Vector2D vP1P2 = new Vector2D(p1, p2);
        Vector2D vP1P2Perpendicular = vP1P2.getMiddlePerpendicularClockwise();
        double p1P2HalfLength = vP1P2.getLength() / 2.0;
        double perpendicularLength = Math.sqrt(sqr(p1P2HalfLength) + sqr(U_TURN_RADIUS));
        vP1P2Perpendicular.setLength(perpendicularLength);
        
        Position2D center = vP1P2Perpendicular.getTip();

        Position2D aNC = p1;
        Position2D bNC = p2;
        Position2D a = Position2D.minus(aNC, center);
        Position2D b = Position2D.minus(bNC, center);
        double angle1 = Math.atan2(a.getY(), a.getX());
        double angle2 = Math.atan2(b.getY(), b.getX());

        List<Double> thetaRange = new ArrayList<>();
        if(!(angle1 < 0 && angle2 > 0))
        {
            angle2 = angle2 + 2.0 * Math.PI;
        }
        thetaRange = Linspace.linspace(angle1, angle2, 10);
        List<Position2D> points = thetaRange.stream().map(theta -> new Position2D(center.getX() + Math.cos(theta) * U_TURN_RADIUS, center.getY() + Math.sin(theta) * U_TURN_RADIUS)).collect(Collectors.toList());
        
        Position2D last = null;
        for(int idx1 = 0; idx1 < points.size(); idx1++)
        {
            Position2D current = points.get(idx1);
            if(last != null)
            {
                result.add(new Line2D(last, current));
            }
            last = current;
        }
    }

    private void addTurnaroundCircle(List<Line2D> result, List<Line2D> addedLines, EdgeType nextEdge)
    {
        Line2D lastLine = addedLines.get(addedLines.size() - 1);
        Position2D p1 = lastLine.getP2();
        
        List<LaneType> lanes = nextEdge.getLane();
        String shape = lanes.get(0).getShape();
        Line2D nextLine = Line2D.createLines(shape).get(0);
        Position2D p2 = nextLine.getP1();
        
        Vector2D vP1P2 = new Vector2D(p1, p2);
        Vector2D vP1P2Perpendicular = vP1P2.getMiddlePerpendicularClockwise();
        double p1P2HalfLength = vP1P2.getLength() / 2.0;
        double perpendicularLength = Math.sqrt(sqr(p1P2HalfLength) + sqr(U_TURN_RADIUS));
        vP1P2Perpendicular.setLength(perpendicularLength);
        
        Position2D center = vP1P2Perpendicular.getTip();

        Position2D aNC = p1;
        Position2D bNC = p2;
        Position2D a = Position2D.minus(aNC, center);
        Position2D b = Position2D.minus(bNC, center);
        double angle1 = Math.atan2(a.getY(), a.getX());
        double angle2 = Math.atan2(b.getY(), b.getX());

        List<Double> thetaRange = new ArrayList<>();
        if(!(angle1 < 0 && angle2 > 0))
        {
            angle2 = angle2 + 2.0 * Math.PI;
        }
        thetaRange = Linspace.linspace(angle1, angle2, 10);
        List<Position2D> points = thetaRange.stream().map(theta -> new Position2D(center.getX() + Math.cos(theta) * U_TURN_RADIUS, center.getY() + Math.sin(theta) * U_TURN_RADIUS)).collect(Collectors.toList());
        
        Position2D last = null;
        for(int idx = 0; idx < points.size(); idx++)
        {
            Position2D current = points.get(idx);
            if(last != null)
            {
                result.add(new Line2D(last, current));
            }
            last = current;
        }
    }


    private double sqr(double x)
    {
        return x * x;
    }

    private List<Line2D> regularLineAdd(List<Line2D> result, EdgeType curEdge)
    {
        List<LaneType> lanes = curEdge.getLane();
        String shape = lanes.get(0).getShape();
        List<Line2D> linesToAdd = Line2D.createLines(shape);
        result.addAll(linesToAdd);
        return linesToAdd;
    }
    
    private boolean is180DegreeTurn(EdgeType e1, EdgeType e2)
    {
        String e1ID = e1.getId();
        String e2ID = e2.getId();
        e1ID = e1ID.replace("-", "");
        e2ID = e2ID.replace("-", "");
        
        return e1ID.equals(e2ID);
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
