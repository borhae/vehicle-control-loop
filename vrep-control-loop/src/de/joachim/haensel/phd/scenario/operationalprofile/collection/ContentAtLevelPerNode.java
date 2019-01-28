package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContentAtLevelPerNode
{
    public static List<List<Integer>> count(CountTreeNode root)
    {
        List<List<Integer>> result =  new ArrayList<>();
        int level = 0;
        List<Integer> rootAmount = new ArrayList<>();
        rootAmount.add(0);
        result.add(rootAmount);
        while(!result.get(level).isEmpty())
        {
            level++;
            List<Integer> size = count(root, level);
            result.add(size);
        }
        return result;
    }

    private static List<Integer> count(CountTreeNode node, int level)
    {
        if(level == 0)
        {
            List<Integer> result = new ArrayList<>();
            result.add(node.getContent().size());
            return result;
        }
        else
        {
            List<Integer> result = new ArrayList<>();
            Collection<CountTreeNode> children = node.getChildren();
            for (CountTreeNode curChild : children)
            {
                result.addAll(count(curChild, level - 1));
            }
            return result;
        }
    }
}
