package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class MongoHistogram3DEntry
{
    private Integer _clusterNr;
    private List<List<MongoTrajectory>> _trajectories;

    public MongoHistogram3DEntry(Integer clusterNr, List<Integer> clusterContent, List<List<TrajectoryElement>> trajectories)
    {
        _clusterNr = clusterNr;
        _trajectories = clusterContent.stream().map(idx -> trajectories.get(idx).stream().map(t -> new MongoTrajectory(t)).collect(Collectors.toList())).collect(Collectors.toList());
    }

    public Integer getClusterNr()
    {
        return _clusterNr;
    }

    public void setClusterNr(Integer clusterNr)
    {
        _clusterNr = clusterNr;
    }

    public List<List<MongoTrajectory>> getTrajectories()
    {
        return _trajectories;
    }

    public void setTrajectories(List<List<MongoTrajectory>> trajectories)
    {
        _trajectories = trajectories;
    }
}
