package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.ObservationTuple;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class ConfigurationObservationTreeCounter
{
    public static CountTreeNode count(Map<Long, List<TrajectoryElement>> configurations, Map<Long, ObservationTuple> observations)
    {
        List<EquivalenClassEntry> states = new ArrayList<>();
        Set<Long> timeStamps = configurations.keySet();
        for(Long curTimeStamp : timeStamps)
        {
            EquivalenClassEntry equivalenceClassEntry = new EquivalenClassEntry(curTimeStamp, configurations.get(curTimeStamp), observations.get(curTimeStamp));
            states.add(equivalenceClassEntry);
        }
        CountTreeNode root = new CountTreeNode(null);
        for (EquivalenClassEntry curState : states)
        {
            root.enter(curState);
        }
//        TODO restore after debug
//        states.forEach(curState -> root.enter(curState));
        return root;
    }

    public static String stats(CountTreeNode root)
    {
        List<Integer> heights = getHeight(root);
        List<Integer> elementsPerClass = getLeafsPerPath(root);
        return elementsPerClass.toString();
    }

    public static List<Integer> getLeafsPerPath(CountTreeNode node)
    {
        if(node.getChildren() != null && !node.getChildren().isEmpty())
        {
            Collection<CountTreeNode> children = node.getChildren();
            List<Integer> heights = new ArrayList<>();
            for (CountTreeNode curChild : children)
            {
                heights.addAll(getLeafsPerPath(curChild));
            }
            return heights;
        }
        else
        {
            ArrayList<Integer> elements = new ArrayList<>();
            elements.add(node.getContent().size());
            return elements;
        }
    }

    public static List<Integer> getHeight(CountTreeNode node)
    {
        if(node.getChildren() != null && !node.getChildren().isEmpty())
        {
            Collection<CountTreeNode> children = node.getChildren();
            List<Integer> heights = new ArrayList<>();
            for (CountTreeNode curChild : children)
            {
                List<Integer> childHeight = getHeight(curChild);
                List<Integer> plusOne = childHeight.stream().map(cur -> cur + 1).collect(Collectors.toList());
                heights.addAll(plusOne);
            }
            return heights;
        }
        else
        {
            ArrayList<Integer> height = new ArrayList<>();
            height.add(0);
            return height;
        }
    }
}
