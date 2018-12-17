package de.joachim.haensel.phd.scenario.tasks.creation;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class PointListTaskCreatorConfig implements ITaskCreatorConfig
{
    private int _numOfTasks;
    private List<Position2D> _targetPoints;
    private int _sourceIdx;

    public PointListTaskCreatorConfig(int numOfTasks)
    {
        _numOfTasks = numOfTasks;
        _sourceIdx = 0;
        _targetPoints = new ArrayList<>();
    }

    public void setTargetPoints(List<Position2D> targetPoints)
    {
        _targetPoints = targetPoints;
        _sourceIdx = 0;
    }

    @Override
    public int getNumOfTasks()
    {
        return _numOfTasks;
    }

    @Override
    public Task getNext()
    {
        Task task = new Task(_targetPoints.get(_sourceIdx), _targetPoints.get(_sourceIdx + 1), 5000);
        _sourceIdx++;
        return task;
    }
}    