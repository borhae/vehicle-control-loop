package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.List;

public class NamedRow <T>
{
    private List<T> _trajectoryAngles;
    private String _name;

    public NamedRow(List<T> trajectoryAngles, String name)
    {
        _trajectoryAngles = trajectoryAngles;
        _name = name;
    }

    public List<T> getTrajectoryAngles()
    {
        return _trajectoryAngles;
    }

    public void setTrajectoryAngles(List<T> trajectoryAngles)
    {
        _trajectoryAngles = trajectoryAngles;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }
}
