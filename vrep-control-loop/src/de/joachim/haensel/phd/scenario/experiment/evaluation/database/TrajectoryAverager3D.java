package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class TrajectoryAverager3D
{
    public static List<double[]> EMPTY_TRAJECTORY = 
            IntStream.range(0, 20).boxed().map(idx -> new double[] {0.0, 0.0, 0.0}).collect(Collectors.toList());

    private List<double[]> _runningSum = createEmpty();
    private int _runningCount = 0;
    private boolean _firstUsage = true;

    private static List<double[]> createEmpty()
    {
        return new ArrayList<double[]>(EMPTY_TRAJECTORY);
    }


    public void accept(List<double[]> newTrajectory)
    {
        if(_firstUsage)
        {
            _runningSum = IntStream.range(0, newTrajectory.size()).boxed().map(idx -> new double[] {0.0, 0.0, 0.0}).collect(Collectors.toList());
            _firstUsage = false;
        }
        addInto(_runningSum, newTrajectory);
        _runningCount++;
    }

    private void addInto(List<double[]> result, List<double[]> other)
    {
        if(result.size() != other.size())
        {
            _runningSum = null;
            System.out.println("can't compute sum of trajectories of different length");
        }
        else
        {
            IntStream.range(0, result.size()).boxed().forEach(idx -> sumInto(result.get(idx), other.get(idx)));
        }
    }

    private void sumInto(double[] v1, double[] v2)
    {
        for(int idx = 0; idx < v1.length; idx++)
        {
            v1[idx] += v2[idx];
        }
    }


    public void combine(TrajectoryAverager3D other)
    {
        _runningCount = _runningCount + other._runningCount;
        addInto(_runningSum, other._runningSum);
    }


    public List<double[]> getAverage()
    {
        return _runningSum.stream().map(v -> divide(v, (double)_runningCount)).collect(Collectors.toList());
    }

    private double[] divide(double[] v, double divisor)
    {
        for(int idx = 0; idx < v.length; idx++)
        {
            v[idx] = v[idx] / divisor;
        }
        return v;
    }
}
