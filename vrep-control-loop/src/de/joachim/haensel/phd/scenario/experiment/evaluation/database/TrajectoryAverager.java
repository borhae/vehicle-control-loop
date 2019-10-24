package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class TrajectoryAverager
{
    public static List<Vector2D> EMPTY_TRAJECTORY = 
            IntStream.range(0, 20).boxed().map(idx -> new Vector2D()).collect(Collectors.toList());

    private List<Vector2D> _runningSum = createEmpty();
    private int _runningCount = 0;
    private boolean _firstUsage = true;

    private static List<Vector2D> createEmpty()
    {
        return new ArrayList<Vector2D>(EMPTY_TRAJECTORY);
    }


    public void accept(List<Vector2D> newTrajectory)
    {
        if(_firstUsage)
        {
            _runningSum = IntStream.range(0, newTrajectory.size()).boxed().map(idx -> new Vector2D()).collect(Collectors.toList());
            _firstUsage = false;
        }
        addInto(_runningSum, newTrajectory);
        _runningCount++;
    }

    private void addInto(List<Vector2D> result, List<Vector2D> other)
    {
        if(result.size() != other.size())
        {
            _runningSum = null;
            System.out.println("can't compute sum of trajectories of different length");
        }
        else
        {
            IntStream.range(0, result.size()).boxed().forEach(idx -> result.get(idx).sumIntoThis(other.get(idx)));
        }
    }

    public void combine(TrajectoryAverager other)
    {
        _runningCount = _runningCount + other._runningCount;
        addInto(_runningSum, other._runningSum);
    }


    public List<Vector2D> getAverage()
    {
        return _runningSum.stream().map(v -> {v.mul(1.0/_runningCount); return v;}).collect(Collectors.toList());
    }
}
