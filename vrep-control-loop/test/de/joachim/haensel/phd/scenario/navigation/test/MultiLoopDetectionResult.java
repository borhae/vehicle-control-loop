package de.joachim.haensel.phd.scenario.navigation.test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class MultiLoopDetectionResult
{
    private List<int[]> _multiLoopIdxs;
    private List<TrajectoryElement> _trajectoryElements;
    private List<Position2D> _sharpTurns;
    private String _id;
    private List<EdgeLine> _sharpTurnEdges;
    private Map<Integer, EdgeLine> _idxToEdgeMap;
    private Position2D _startPos;
    private Position2D _endPos;

    public MultiLoopDetectionResult(List<TrajectoryElement> trajectoryElements, Position2D startPos, Position2D endPos, List<Position2D> sharpTurns, List<int[]> multiLoopIdxs, String id)
    {
        _multiLoopIdxs = multiLoopIdxs;
        _trajectoryElements = trajectoryElements;
        _startPos = startPos;
        _endPos = endPos;
        _sharpTurns = sharpTurns;
        _id = id;
        _sharpTurnEdges = null;
        _idxToEdgeMap = null;
    }

    public MultiLoopDetectionResult(List<TrajectoryElement> trajectoryElements, Position2D startPos, Position2D endPos, List<EdgeLine> sharpTurnEdges, List<int[]> multiLoopIdxs, Map<Integer, EdgeLine> idxToEdgeMap, String id)
    {
        _id = id;
        _trajectoryElements = trajectoryElements;
        _startPos = startPos;
        _endPos = endPos;
        _sharpTurnEdges = sharpTurnEdges;
        _sharpTurns = sharpTurnEdges.stream().map(edgeLine -> edgeLine.getCenter()).collect(Collectors.toList());
        _multiLoopIdxs = multiLoopIdxs;
        _idxToEdgeMap = idxToEdgeMap;
    }

    public List<Position2D> getSharpTurnIntersections()
    {
        return _sharpTurns;
    }

    public List<int[]> getMultiLoops()
    {
        return _multiLoopIdxs;
    }

    public String getId()
    {
        return _id;
    }

    public List<TrajectoryElement> getTrajectoryElements()
    {
        return _trajectoryElements;
    }

    public List<EdgeLine> getSharpTurnEdges()
    {
        return _sharpTurnEdges;
    }

    public Map<Integer, EdgeLine> getIdxToEdgeMap()
    {
        return _idxToEdgeMap;
    }

    public Position2D getStartPos()
    {
        return _startPos;
    }

    public Position2D getEndPos()
    {
        return _endPos;
    }
}
