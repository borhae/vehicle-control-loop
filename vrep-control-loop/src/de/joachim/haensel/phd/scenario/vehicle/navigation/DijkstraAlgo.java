package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.map.RoadMap;
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
        System.out.println("finding path");
        init();
        _distances.put(_source, 0.0f);
        int neighbourCnt = 0;
        int size = _unvisitedNodes.size();
        boolean foundTarget = false;
        while(!_unvisitedNodes.isEmpty())
        {
            Node u = minimumDistance(_unvisitedNodes);
            
            _unvisitedNodes.remove(u);
            size--;
            if(u.equals(_target))
            {
                foundTarget = true;
                break;
            }
            Collection<Node> neighbors = neighbors(u);
            for (Node v : neighbors)
            {
                Float distanceOfU = _distances.get(u);
                Float distanceBetweenUAndV = u.distance(v);
                float alternativeDistance = distanceOfU + distanceBetweenUAndV;
                if(alternativeDistance < _distances.get(v))
                {
                    _distances.put(v, alternativeDistance);
                    _previousNodesOptimalPath.put(v, u);
                }
                neighbourCnt++;
            }
            if(size % 100 == 0)
            {
//                System.out.format("size: %d, neighbours looked at: %d %n", size, neighbourCnt);
                System.out.print(".");
                neighbourCnt = 0;
            }
        }
        System.out.println("path found");
        if(foundTarget) 
        {
            return toPath(_previousNodesOptimalPath, _target);
        }
        else
        {
            return null;
        }
    }

    private List<Node> toPath(HashMap<Node, Node> previousNodesOptimalPath, Node target)
    {
        Stack<Node> collectionStack = new Stack<>();
        Node cur = target;
        while(cur != null)
        {
            collectionStack.push(cur);
            cur = _previousNodesOptimalPath.get(cur);
        }
        List<Node> result = new ArrayList<>();
        while(!collectionStack.isEmpty())
        {
            result.add(collectionStack.pop());
        }
        return result;
    }

    private Collection<Node> neighbors(Node node)
    {
        return node.getOutgoingNodes();
    }

    private Node minimumDistance(HashSet<Node> unvisitedNodes)
    {
        return unvisitedNodes.stream().min((j1, j2) -> Float.compare(_distances.get(j1), _distances.get(j2))).get();
    }

    private void init()
    {
        _distances = new HashMap<>();
        _nodes.stream().forEach(node -> _distances.put(node, Float.MAX_VALUE));
        _previousNodesOptimalPath = new HashMap<>();
        _nodes.stream().forEach(node -> _previousNodesOptimalPath.put(node, null));
        _unvisitedNodes = new HashSet<>();
        _nodes.stream().forEach(node -> _unvisitedNodes.add(node));
    }
}
