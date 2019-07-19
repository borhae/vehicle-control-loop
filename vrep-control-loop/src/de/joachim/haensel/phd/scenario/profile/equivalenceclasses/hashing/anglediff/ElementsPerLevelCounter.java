package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ElementsPerLevelCounter
{
    public static List<Integer> count(CountTreeNode root)
    {
        List<Integer> result =  new ArrayList<>();
        int level = 0;
        result.add(1);
        while(result.get(level) != 0)
        {
            level++;
            int size = count(root, level);
            result.add(size);
        }
        return result;
    }

    private static int count(CountTreeNode node, int level)
    {
        if(level == 0)
        {
            return node.getContent().size();
        }
        else
        {
            int result = 0;
            Collection<CountTreeNode> children = node.getChildren();
            for (CountTreeNode curChild : children)
            {
                result += count(curChild, level - 1);
            }
            return result;
        }
    }
}
