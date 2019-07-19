package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NodesPerLevelPrinter
{

    public static List<List<String>> print(CountTreeNode root)
    {
        List<List<String>> result =  new ArrayList<>();
        int level = 0;
        List<String> levelZero = new ArrayList<>();
        levelZero.add(root.toString());
        result.add(levelZero);
        while(!result.get(level).isEmpty())
        {
            level++;
            List<String> levelContent = count(root, level);
            result.add(levelContent);
        }
        return result;
    }

    private static List<String> count(CountTreeNode curNode, int level)
    {
        if(level == 0)
        {
            List<String> result = new ArrayList<>();
            result.add(curNode.toString());
            return result;
        }
        else
        {
            List<String> result = new ArrayList<>();
            Collection<CountTreeNode> children = curNode.getChildren();
            for (CountTreeNode curChild : children)
            {
                result.addAll(count(curChild, level - 1));
            }
            return result;
        }
    }
}
