package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.List;

import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.RoadMap;

public class Trajectorizer
{
    private RoadMap _roadMap;

    public Trajectorizer(RoadMap roadMap)
    {
        _roadMap = roadMap;
    }

    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        return null;
    }
}
