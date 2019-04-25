package de.joachim.haensel.phd.scenario.navigation.test;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class MultiLoopDetectionResult
{
    private List<int[]> _multiloops;
    private List<TrajectoryElement> _trajectoryElements;
    private List<Position2D> _sharpTurnIntersections;
    private String _id;

    public MultiLoopDetectionResult(List<int[]> multiloops, List<TrajectoryElement> trajectoryElements, List<Position2D> sharpTurnIntersections, String id)
    {
        _multiloops = multiloops;
        _trajectoryElements = trajectoryElements;
        _sharpTurnIntersections = sharpTurnIntersections;
        _id = id;
    }

    public List<Position2D> getSharpTurnIntersections()
    {
        return _sharpTurnIntersections;
    }

    public List<int[]> getMultiLoops()
    {
        return _multiloops;
    }

    public String getId()
    {
        return _id;
    }

    public List<TrajectoryElement> getTrajectoryElements()
    {
        return _trajectoryElements;
    }
}
