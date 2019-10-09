package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class MongoTrajectoryTransform
{
    public static Map<Long, List<MongoTrajectory>> transform(Map<Long, List<TrajectoryElement>> configurations)
    {
        Map<Long, List<MongoTrajectory>> result = 
                configurations.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> transform(entry.getValue())));
        return result;
    }

    private static List<MongoTrajectory> transform(List<TrajectoryElement> inList)
    {
        return inList.stream().map(curTrajElem -> new MongoTrajectory(curTrajElem)).collect(Collectors.toList());
    }
}
