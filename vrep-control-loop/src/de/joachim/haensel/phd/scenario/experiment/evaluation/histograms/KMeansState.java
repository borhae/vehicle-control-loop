package de.joachim.haensel.phd.scenario.experiment.evaluation.histograms;

public class KMeansState
{
    private double _minRunAverageDistance;
    private double _minRunAverageDistanceVariance;
    private int _k;
    private int _size;

    public KMeansState(int k, int size, double minRunAverageDistance, double minRunAverageDistanceVariance)
    {
        _k = k;
        _size = size;
        _minRunAverageDistance = minRunAverageDistance;
        _minRunAverageDistanceVariance = minRunAverageDistanceVariance;
    }

    public double getMinRunAverageDistance()
    {
        return _minRunAverageDistance;
    }

    public void setMinRunAverageDistance(double minRunAverageDistance)
    {
        _minRunAverageDistance = minRunAverageDistance;
    }

    public double getMinRunAverageDistanceVariance()
    {
        return _minRunAverageDistanceVariance;
    }

    public void setMinRunAverageDistanceVariance(double minRunAverageDistanceVariance)
    {
        _minRunAverageDistanceVariance = minRunAverageDistanceVariance;
    }

    public int getK()
    {
        return _k;
    }

    public void setK(int k)
    {
        _k = k;
    }

    public int getSize()
    {
        return _size;
    }

    public void setSize(int size)
    {
        _size = size;
    }
}
