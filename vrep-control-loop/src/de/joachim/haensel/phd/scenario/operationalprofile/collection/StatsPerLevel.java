package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatsPerLevel
{
    public static List<OCStats> count(CountTreeNode root)
    {
        List<OCStats> result =  new ArrayList<>();
        int level = 0;
        result.add(new OCStats());
        boolean done = false;
        while(!done)
        {
            OCStats stats = new OCStats();
            level++;
            List<ICountListElem> elems = collectAtLevel(root, level);
            elems.forEach(elem -> elem.accept(stats));
            result.add(stats);
            done = elems.isEmpty();
        }
        return result;
    }

    private static List<ICountListElem> collectAtLevel(CountTreeNode node, int level)
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
                result.addAll(collectAtLevel(curChild, level - 1));
            }
            return result;
        }
    }
}
