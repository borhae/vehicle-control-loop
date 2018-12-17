package de.joachim.haensel.phd.scenario.sumo2vrep;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import sumobindings.JunctionType;

public class Node
{
    private JunctionType _baseJunction;
    private HashMap<Node, Edge> _outgoing;
    private HashMap<Node, Edge> _incomming;

    public Node(JunctionType baseJunction)
    {
        _baseJunction = baseJunction;
        _outgoing = new HashMap<>();
        _incomming = new HashMap<>();
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

    public Float distance(Node v)
    {
        Edge edgeToNode = _outgoing.get(v);
        return edgeToNode.getLength();
    }

    public double distance(Position2D p1)
    {
        String shape = _baseJunction.getShape();
        
        String[] coordinatesString = shape.split(" ");
        List<Position2D> polygon = Arrays.asList(coordinatesString).stream().map(curBlock -> new Position2D(curBlock)).collect(Collectors.toList());
        double minDistance = Double.POSITIVE_INFINITY;
        for(int idx = 0; idx < polygon.size() - 1; idx++)
        {
            double curDist = (new Line2D(polygon.get(idx), polygon.get(idx+1))).distance(p1);
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
}
