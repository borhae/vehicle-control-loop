package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff.ObservationTuple;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class MongoTransform
{
    public static Map<Long, List<MongoTrajectory>> transformConfigurations(Map<Long, List<TrajectoryElement>> configurations)
    {
        Map<Long, List<MongoTrajectory>> result = 
                configurations.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> transformConfiguration(entry.getValue())));
        return result;
    }

    private static List<MongoTrajectory> transformConfiguration(List<TrajectoryElement> inList)
    {
        return inList.stream().map(curTrajElem -> new MongoTrajectory(curTrajElem)).collect(Collectors.toList());
    }

    public static Map<Long, MongoObservationTuple> transformObservations(Map<Long, ObservationTuple> observations)
    {
        Map<Long, MongoObservationTuple> result = 
                observations.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> transformObservation(entry.getValue())));
        return result;
    }

    private static MongoObservationTuple transformObservation(ObservationTuple value)
    {
        return new MongoObservationTuple(value);
    }
}
