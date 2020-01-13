package de.joachim.haensel.phd.scenario.experiment.evaluation.database.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanceCache
{
    private Map<Integer, Double>[] _cache;
    private List<double[][]> _allTrajectories;
    private int _cacheMissCnt;
    private int _cacheHitCnt;
    private int _cnt;

    public DistanceCache(List<double[][]> allTrajectories)
    {
        int size = allTrajectories.size();
        _cache = new Map[size];
        for(int idxI = 0; idxI < _cache.length; idxI++)
        {
            _cache[idxI] = new HashMap<Integer, Double>();
        }
        _allTrajectories = allTrajectories;
        _cacheMissCnt = 1;
        _cacheHitCnt = 1;
        _cnt = 0;
    }

    public synchronized double getDistance(int trajectoryIdx1, int trajectoryIdx2, String caller)
    {
        _cnt++;
        double result = 0.0;
        if(trajectoryIdx1 != trajectoryIdx2)
        {
            if(_cache[trajectoryIdx1].get(trajectoryIdx2) == null && _cache[trajectoryIdx2].get(trajectoryIdx1) == null)
            {
                _cacheMissCnt++;
                result = distanceByIndex(trajectoryIdx1, trajectoryIdx2);
                _cache[trajectoryIdx1].put(trajectoryIdx2, result);
            }
            else
            {
                _cacheHitCnt++;
                Double result1 = _cache[trajectoryIdx1].get(trajectoryIdx2);
                if(result1 == null)
                {
                    result = _cache[trajectoryIdx2].get(trajectoryIdx1);
                }
                else
                {
                    result = result1;
                }
            }
        }
        if(_cnt % 1000 == 0)
        {
            System.out.format("Cache stats: hits: %d, misses: %d, ratio: %.2f, called by: %s\n", _cacheHitCnt - 1, _cacheMissCnt - 1, (double)_cacheMissCnt/(double)_cacheHitCnt, caller);
        }
        return result;
    }

    private double distanceByIndex(int trajectoryIdx1, int trajectoryIdx2)
    {
        double[][] t1 = _allTrajectories.get(trajectoryIdx1);
        double[][] t2 = _allTrajectories.get(trajectoryIdx2);
        return distance(t1, t2);
    }

    public double distance(double[][] trajectory1, double[][] trajectory2)
    {
        double result = 0.0;
        for(int idx = 0; idx < trajectory1.length; idx++)
        {
            double[] p1 = trajectory1[idx];
            double[] p2 = trajectory2[idx];
            double dx = p2[0] - p1[0];
            double dy = p2[1] - p1[1];
            double dz = p2[2] - p1[2];
            result += Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        return result;
    }

    public double getManhattenDistance(Integer trajectoryIdx1, Integer trajectoryIdx2)
    {
        double result = 0.0;
        double[][] trajectory1 = _allTrajectories.get(trajectoryIdx1);
        double[][] trajectory2 = _allTrajectories.get(trajectoryIdx2);
        for(int idx = 0; idx < trajectory1.length; idx++)
        {
            double[] p1 = trajectory1[idx];
            double[] p2 = trajectory2[idx];
            double dx = Math.abs(p2[0] - p1[0]);
            double dy = Math.abs(p2[1] - p1[1]);
            double dz = Math.abs(p2[2] - p1[2]);
            result += dx + dy + dz;
        }
        return result;
    }
}
