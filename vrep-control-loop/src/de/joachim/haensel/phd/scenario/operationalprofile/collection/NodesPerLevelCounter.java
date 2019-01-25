package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NodesPerLevelCounter
{
    public NodesPerLevelCounter()
    {
    }
    
    public static void count(CountTreeNode root, List<Integer> result)
    {
        result.add(1);
        List<CountTreeNode> level = new ArrayList<>();
        level.addAll(root.getChildren());
        
    }
}
