package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug.DistanceCache;

public class Trajectory3DSummaryStatistics
{
    private List<double[][]> _accumulatedData;
    private double[][] _runningSum = createEmpty();
    private double[][] _average = null;
    private double[][] _variance = null;
    
    private int _runningCount = 0;
    private List<Double> _averageDistances;
    private Double _averageDistance = null;
    private Integer _idx;

    public Trajectory3DSummaryStatistics(int idx)
    {
        _idx = idx;
    }
    
    public Trajectory3DSummaryStatistics()
    {
        _accumulatedData = new ArrayList<double[][]>();
    }
    
    public Trajectory3DSummaryStatistics(double[][] center)
    {
        _average = center;
    }

    public Trajectory3DSummaryStatistics(double[][] center, Integer idx)
    {
        this(center);
        _idx = idx;
    }

    private static double[][] createEmpty()
    {
        return new double[21][3];
    }

    public void accept(double[][] newTrajectory)
    {
        _accumulatedData.add(newTrajectory);
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
            IntStream.range(0, result.length).boxed().forEach(idx -> addInto(result[idx], other[idx]));
        }
    }

    private double[][] subtract(double[][] one, double[][] other)
    {
        if(one.length != other.length)
        {
            _runningSum = null;
            System.out.println("can't subtract trajectories of different length");
            return null;
        }
        else
        {
            double[][] result = createEmpty();
            for(int idx = 0; idx < result.length; idx++)
            {
                result[idx] = subtract(one[idx], other[idx]);
            }
            return result;
        }
    }

    private void square(double[][] result)
    {
        IntStream.range(0, result.length).boxed().forEach(idx -> square(result[idx]));
    }

    private void addInto(double[] v1, double[] v2)
    {
        for(int idx = 0; idx < v1.length; idx++)
        {
            v1[idx] += v2[idx];
        }
    }

    private double[] subtract(double[] v1, double[] v2)
    {
        double[] vR = new double[v1.length];
        for(int idx = 0; idx < v1.length; idx++)
        {
            vR[idx] = v1[idx] - v2[idx];
        }
        return vR;
    }
    
    private void square(double[] v1)
    {
        for(int idx = 0; idx < v1.length; idx++)
        {
            double cur = v1[idx];
            v1[idx] += cur * cur;
        }
    }

    public void combine(Trajectory3DSummaryStatistics other)
    {
        _runningCount = _runningCount + other._runningCount;
        _accumulatedData.addAll(other._accumulatedData);
    }
    
    public int getClusterNr()
    {
        return _idx;
    }

    public void setClusterNr(int clusterNr)
    {
        _idx = clusterNr;
    }

    public double[][] getAverage()
    {
        if(_average == null)
        {
            double[][] result = createEmpty();
            for(int idx = 0; idx < _accumulatedData.size(); idx++)
            {
                addInto(_runningSum, _accumulatedData.get(idx));
            }
            for(int idx = 0; idx < _runningSum.length; idx++)
            {
                double[] cur = _runningSum[idx];
                result[idx] = divide(cur, _runningCount);
            }
            _average = result;
        }
        return _average;
    }
    
    public double[][] getVariance()
    {
        if(_variance == null)
        {
            double[][] result = createEmpty();
            double[][] average = getAverage();
            for(int idx = 0; idx < _accumulatedData.size(); idx++)
            {
                double[][] cur = _accumulatedData.get(idx);
                double[][] div = subtract(cur, average);
                square(div);
                addInto(_runningSum, div);
            }
            for(int idx = 0; idx < _runningSum.length; idx++)
            {
                double[] cur = _runningSum[idx];
                result[idx] = divide(cur, _runningCount);
            }
            _variance = result;
        }
        return _variance;
    }

    private double[] divide(double[] v, double divisor)
    {
        for(int idx = 0; idx < v.length; idx++)
        {
            v[idx] = v[idx] / divisor;
        }
        return v;
    }
    
    public List<Double> getDistancesToAverage()
    {
        if(_averageDistances == null)
        {
            _averageDistances = _accumulatedData.parallelStream().map(trajectory -> DistanceCache.distance(trajectory, getAverage())).collect(Collectors.toList());
        }
        return _averageDistances;
    }

    @Override
    public String toString()
    {
        double avg = getAverageDistance();
        double var = getAverageDistanceVariance();
        return String.format("Average: %f, Variance: %f", avg, var);
    }
    
    public double getAverageDistance()
    {
        if(_averageDistance == null)
        {
            List<Double> distances = getDistancesToAverage();
            _averageDistance  = distances.stream().mapToDouble(Double::doubleValue).sum() / distances.size();
        }
        return _averageDistance;
    }

    public double getAverageDistanceVariance()
    {
        List<Double> distancesToAverage = getDistancesToAverage();
        return distancesToAverage.parallelStream().map(distance -> sqr(distance - getAverageDistance())).mapToDouble(Double::doubleValue).sum() / distancesToAverage.size();
    }
    
    private double sqr(double val)
    {
        return val * val;
    }
}
