package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoVector2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class MongoHistogramEntry
{
    private Integer _clusterNr;
    private List<List<MongoVector2D>> _trajectories;
    
    public MongoHistogramEntry()
    {
    }

    public MongoHistogramEntry(Integer clusterNr, List<Integer> clusterContent, List<List<Vector2D>> trajectories)
    {
        _clusterNr = clusterNr;
        _trajectories = clusterContent.stream().map(idx -> trajectories.get(idx).stream().map(v -> new MongoVector2D(v)).collect(Collectors.toList())).collect(Collectors.toList());
    }

    public Integer getClusterNr()
    {
        return _clusterNr;
    }

    public void setClusterNr(Integer clusterNr)
    {
        _clusterNr = clusterNr;
    }

    public List<List<MongoVector2D>> getTrajectories()
    {
        return _trajectories;
    }

    public void setTrajectories(List<List<MongoVector2D>> trajectories)
    {
        _trajectories = trajectories;
    }
}
