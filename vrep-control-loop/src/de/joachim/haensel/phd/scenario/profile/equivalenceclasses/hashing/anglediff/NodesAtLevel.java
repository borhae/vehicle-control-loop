package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NodesAtLevel
{
    public static List<List<CountTreeNode>> get(CountTreeNode root)
    {
        List<List<CountTreeNode>> result =  new ArrayList<>();
        int level = 0;
        List<CountTreeNode> rootAmount = new ArrayList<>();
        rootAmount.add(root);
        result.add(rootAmount);
        while(!result.get(level).isEmpty())
        {
            level++;
            List<CountTreeNode> size = count(root, level);
            result.add(size);
        }
        return result;
    }

    private static List<CountTreeNode> count(CountTreeNode node, int level)
    {
        if(level == 0)
        {
            List<CountTreeNode> result = new ArrayList<>();
            result.add(node);
            return result;
        }
        else
        {
            List<CountTreeNode> result = new ArrayList<>();
            Collection<CountTreeNode> children = node.getChildren();
            for (CountTreeNode curChild : children)
            {
                result.addAll(count(curChild, level - 1));
            }
            return result;
        }
    }
}
