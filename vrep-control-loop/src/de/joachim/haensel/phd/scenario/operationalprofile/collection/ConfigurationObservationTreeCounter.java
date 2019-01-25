package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class ConfigurationObservationTreeCounter
{
    public static CountTreeNode count(Map<Long, List<TrajectoryElement>> configurations, Map<Long, ObservationTuple> observations)
    {
        List<StateAt> states = new ArrayList<>();
        Set<Long> timeStamps = configurations.keySet();
        for(Long curTimeStamp : timeStamps)
        {
            StateAt curStateAt = new StateAt(curTimeStamp, configurations.get(curTimeStamp), observations.get(curTimeStamp));
            states.add(curStateAt);
        }
        CountTreeNode root = new CountTreeNode();
        for (StateAt curState : states)
        {
            root.enter(curState);
        }
//        TODO restore after debug
//        states.forEach(curState -> root.enter(curState));
        return root;
    }
}
