package de.joachim.haensel.vehiclecontrol.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import de.joachim.haensel.sumo2vrep.Node;
import de.joachim.haensel.sumo2vrep.RoadMap;
import sumobindings.JunctionType;

public class DijkstraAlgo implements IShortestPathAlgorithm
{
    private RoadMap _roadMap;
    private Collection<Node> _nodes;

    private Node _source;
    private Node _target;
    
    private HashMap<Node, Float> _distances;
    private HashMap<Node, Node> _previousNodesOptimalPath;
    private HashSet<Node> _unvisitedNodes;

    public DijkstraAlgo(RoadMap roadMap)
    {
        _roadMap = roadMap;
        _nodes = _roadMap.getNodes();
    }

    public void setSource(JunctionType source)
    {
        _source = _roadMap.getNode(source);
    }

    public void setTarget(JunctionType target)
    {
        _target = _roadMap.getNode(target);
    }

    public List<Node> getPath()
    {
        init();
        _distances.put(_source, 0.0f);
        while(!_unvisitedNodes.isEmpty())
        {
            Node u = minimumDistance(_distances);
            
            _distances.remove(u);
            
            if(u.equals(_target))
            {
                break;
            }
            Collection<Node> neighbors = neighbors(u);
            for (Node v : neighbors)
            {
                float alternativeDistance = _distances.get(u) + u.distance(v);
                if(alternativeDistance < _distances.get(v))
                {
                    _distances.put(v, alternativeDistance);
                    _previousNodesOptimalPath.put(v, u);
                }
            }
        }
        return toPath(_previousNodesOptimalPath, _target);
    }

    private List<Node> toPath(HashMap<Node, Node> previousNodesOptimalPath, Node target)
    {
        Stack<Node> result = new Stack<>();
        Node cur = target;
        while(cur != null)
        {
            result.push(cur);
            cur = _previousNodesOptimalPath.get(cur);
        }
        return new ArrayList<>(result);
    }

    private Collection<Node> neighbors(Node node)
    {
        return node.getOutgoingNodes();
    }

    private Node minimumDistance(HashMap<Node, Float> distances)
    {
        return distances.keySet().stream().min((j1, j2) -> Float.compare(_distances.get(j1), _distances.get(j2))).get();
    }

    private void init()
    {
        _distances = new HashMap<>();
        _nodes.stream().forEach(junction -> _distances.put(junction, Float.MAX_VALUE));
        _previousNodesOptimalPath = new HashMap<>();
        _nodes.stream().forEach(node -> _previousNodesOptimalPath.put(node, null));
        _unvisitedNodes = new HashSet<>();
    }
}
