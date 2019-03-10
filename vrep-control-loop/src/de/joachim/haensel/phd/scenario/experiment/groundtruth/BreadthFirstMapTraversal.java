package de.joachim.haensel.phd.scenario.experiment.groundtruth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.joachim.haensel.phd.scenario.map.Node;
import de.joachim.haensel.phd.scenario.map.RoadMap;

public class BreadthFirstMapTraversal
{
    private RoadMap _map;

    public BreadthFirstMapTraversal(RoadMap map)
    {
        _map = map;
    }
    
    private void go()
    {
        List<Node> nodes = new ArrayList<Node>(_map.getNodes());
        HashSet<Node> visited = new HashSet<Node>();
        LinkedList<Node> queue = new LinkedList<Node>();
        
        Node s = nodes.get(0);
        visited.add(s);
        queue.add(s);
        while(queue.size() != 0)
        {
            s = queue.poll();
            Collection<Node> children = s.getOutgoingNodes();
            for (Node curChild : children)
            {
                if(!visited.contains(curChild))
                {
                    visited.add(curChild);
                    queue.add(curChild);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        String mapFilename = args[0];
        RoadMap map = new RoadMap(mapFilename);
        BreadthFirstMapTraversal traverser = new BreadthFirstMapTraversal(map);
        traverser.go();
    }
}
