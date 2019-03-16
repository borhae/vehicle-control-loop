package de.joachim.haensel.phd.scenario.map;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import sumobindings.JunctionType;

public class Node implements IStreetSection
{
    private JunctionType _baseJunction;
    private HashMap<Node, Edge> _outgoing;
    private HashMap<Node, Edge> _incomming;
    private List<Position2D> _polygon;

    public Node(JunctionType baseJunction)
    {
        _baseJunction = baseJunction;
        _outgoing = new HashMap<>();
        _incomming = new HashMap<>();
    }

    public boolean isValid()
    {
        return _baseJunction != null;
    }
    
    public void addIncomming(Edge edge, Node sourceJunction)
    {
        _incomming.put(sourceJunction, edge);
    }

    public void addOutgoing(Edge edge, Node target)
    {
        _outgoing.put(target, edge);
    }

    public Collection<Node> getOutgoingNodes()
    {
        return _outgoing.keySet();
    }
    
    public Collection<Node> getIncomingNodes()
    {
        return _incomming.keySet();
    }

    public Float distance(Node v)
    {
        Edge edgeToNode = _outgoing.get(v);
        if(edgeToNode == null)
        {
            edgeToNode = _incomming.get(v);
        }
        return edgeToNode.getLength();
    }
    
    @Override
    public double getDistance(Position2D position)
    {
        if(_polygon == null)
        {
            String shape = _baseJunction.getShape();
            
            String[] coordinatesString = shape.split(" ");
            _polygon = Arrays.asList(coordinatesString).stream().map(curCoordinate -> new Position2D(curCoordinate)).collect(Collectors.toList());
        }
        double minDistance = Double.POSITIVE_INFINITY;
        for(int idx = 0; idx < _polygon.size() - 1; idx++)
        {
            double curDist = (new Line2D(_polygon.get(idx), _polygon.get(idx+1))).distancePerpendicularOrEndpoints(position);
            if(curDist < minDistance)
            {
                minDistance  = curDist;
            }
        }
        return minDistance;
    }

    public Edge getOutgoingEdge(Node node)
    {
        return _outgoing.get(node);
    }
    
    public Collection<Edge> getIncomingEdges()
    {
        return _incomming.values();
    }
    
    public Collection<Edge> getOutgoingEdges()
    {
        return _outgoing.values();
    }

    @Override
    public String toString()
    {
        return _baseJunction.getId();
    }

    public String getShape()
    {
        return _baseJunction.getShape();
    }

    @Override
    public Vector2D getAPosition()
    {
        return new Vector2D(_baseJunction.getX(), _baseJunction.getY(), 0.0, 0.0);
    }

    public Edge getIncomingEdge(Node node)
    {
        return _incomming.get(node);
    }
}
