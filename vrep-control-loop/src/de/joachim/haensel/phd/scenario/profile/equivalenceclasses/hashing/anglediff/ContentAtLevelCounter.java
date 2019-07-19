package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContentAtLevelCounter
{
    public static List<Integer> count(CountTreeNode root)
    {
        List<Integer> result =  new ArrayList<>();
        int level = 0;
        result.add(0);
        while(result.get(level) != null)
        {
            level++;
            List<ICountListElem> contentAtLevel = locateContentAtLevel(root, level);
            result.add(contentAtLevel.size());
        }
        return result;
    }

    private static List<ICountListElem> locateContentAtLevel(CountTreeNode node, int level)
    {
        if(level == 0)
        {
            return node.getContent();
        }
        else
        {
            List<ICountListElem> result = new ArrayList<>();
            Collection<CountTreeNode> children = node.getChildren();
            for (CountTreeNode curChild : children)
            {
                result.addAll(locateContentAtLevel(curChild, level - 1));
            }
            return result;
        }
    }
}
