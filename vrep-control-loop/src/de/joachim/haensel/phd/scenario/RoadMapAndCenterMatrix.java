package de.joachim.haensel.phd.scenario;

import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;

public class RoadMapAndCenterMatrix
{
    private RoadMap _roadMap;
    private TMatrix _centerMatrix;

    public RoadMapAndCenterMatrix(RoadMap roadMap, TMatrix centerMatrix)
    {
        _roadMap = roadMap;
        _centerMatrix = centerMatrix;
    }

    public RoadMap getRoadMap()
    {
        return _roadMap;
    }

    public TMatrix getCenterMatrix()
    {
        return _centerMatrix;
    }
}
