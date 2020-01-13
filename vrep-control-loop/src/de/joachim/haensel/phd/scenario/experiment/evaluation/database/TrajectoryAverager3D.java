package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.stream.IntStream;

public class TrajectoryAverager3D
{
    private double[][] _runningSum = createEmpty();
    private int _runningCount = 0;
    private boolean _firstUsage = true;

    private static double[][] createEmpty()
    {
        return new double[20][3];
    }

    public void accept(double[][] newTrajectory)
    {
        if(_firstUsage)
        {
            _runningSum = new double[newTrajectory.length][3];
            _firstUsage = false;
        }
        addInto(_runningSum, newTrajectory);
        _runningCount++;
    }

    private void addInto(double[][] result, double[][] other)
    {
        if(result.length != other.length)
        {
            _runningSum = null;
            System.out.println("can't compute sum of trajectories of different length");
        }
        else
        {
            IntStream.range(0, result.length).boxed().forEach(idx -> sumInto(result[idx], other[idx]));
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


    public double[][] getAverage()
    {
        double[][] result = new double[_runningSum.length][3];
        for(int idx = 0; idx < _runningSum.length; idx++)
        {
            double[] cur = _runningSum[idx];
            result[idx] = divide(cur, _runningCount);
        }
        return result;
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
