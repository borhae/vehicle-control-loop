package de.joachim.haensel.phd.scenario.sumo2vrep;

import java.util.Collection;
import java.util.HashMap;

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

    public Edge getOutgoingEdge(Node node)
    {
        return _outgoing.get(node);
    }

    @Override
    public String toString()
    {
        return _baseJunction.getId();
    }
}
