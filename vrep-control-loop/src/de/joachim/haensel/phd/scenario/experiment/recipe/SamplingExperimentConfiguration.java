package de.joachim.haensel.phd.scenario.experiment.recipe;

import java.util.List;

import de.joachim.haensel.phd.scenario.math.FromTo;

public class SamplingExperimentConfiguration
{
    /**
     * Seed for random generator that decides which city is more likely to be chosen by next sample
     * for example. 1001
     */
    private int _evolveRndSeed;
    /**
     * Seed for random generator that decides which element is sampled from a city
     * for example 1002
     */
    private int _pickSampleRndSeed;
    /**
     * Number of tests to add each cycle. Values that make sense are highly dependent on the setup of the other values
     * 6 is good if there is a lot of change in the use distribution and this change is also well detectable by high sampling rates per cycle
     * In other cases I had to go down to 1
     */
    private int _maxAdditionalTests;
    /**
     * How much weight will be given for the initial distribution. This value multiplies occurrence counts in the ground truth sampling set 
     * in order to possibly amplify the starting use distribution (could also be made "sparse" when values are below 1. 
     */
    private double _startWeight;
    /**
     * What is the upper bound for all the risk analysis
     * Math.pow(10.0, -4.0) was used in the paper
     */
    private double _riskUpperBound;
    /**
     * How many clusters should be used. Several are precomputed in the database. If this value does not reflect one of them the clustering 
     * has to be done and saved in the database.
     * 200 is an existing, detailed one. Currently available are: 10, .(steps of 10) . ,  40 , .(steps of 20) . , 200, 1000
     */
    private int _clusteringK;
    /**
     * Name of dataset (collection) from database. Only valid one currently is: "data_available_at_2020-01-18"; 
     */
    private String _dataSetName;
    /**
     * Name of the city to start from sampling
     */
    private String _startCityName;
    /**
     * Name of the city where sampling will evolve into
     */
    private String _evoloveIntoName;
    /**
     * How many samples should be done in one cycle (this could be interpreted as how many vehicles at the same time will provide data)
     * I started with 2000 and am now working with 20000
     */
    private int _samplesPerCycle;
    /**
     * How many cycles should be done in the experiment.
     * I had 10000 and 40000, really depends on what should be going on in the experiment
     */
    private int _cyclesToSample;
    /**
     * A piecewise function R->[0.0, 1.0] that defines how likely it is to sample from city one or from city two. 
     * 0.0 Means only starting city, 1.0 means only evolve into city. 0.5 is equivalent to sampling from both city with 
     * equal likelihood. The pieces of the function are in consecutive order and are of length _cyclesToSample / _samplingProfile.size()
     */
    private List<FromTo> _samplingProfile;

    public int getEvolveRndSeed()
    {
        return _evolveRndSeed;
    }

    public int getPickSampleRndSeed()
    {
        return _pickSampleRndSeed;
    }

    public int getMaxAdditionalTests()
    {
        return _maxAdditionalTests;
    }

    public double getStartWeight()
    {
        return _startWeight;
    }

    public double getRiskUpperBound()
    {
        return _riskUpperBound;
    }

    public int getClusteringK()
    {
        return _clusteringK;
    }

    public String getDataSetName()
    {
        return _dataSetName;
    }

    public String getStartCityName()
    {
        return _startCityName;
    }

    public String getEvolveIntoCityName()
    {
        return _evoloveIntoName;
    }

    public int getCyclesToSample()
    {
        return _cyclesToSample;
    }

    public List<FromTo> getSamplingProfile()
    {
        return _samplingProfile;
    }

    public String getEvoloveIntoName()
    {
        return _evoloveIntoName;
    }

    public void setEvoloveIntoName(String evoloveIntoName)
    {
        _evoloveIntoName = evoloveIntoName;
    }

    public int getSamplesPerCycle()
    {
        return _samplesPerCycle;
    }

    public void setSamplesPerCycle(int samplePerCycle)
    {
        _samplesPerCycle = samplePerCycle;
    }

    public void setEvolveRndSeed(int evolveRndSeed)
    {
        _evolveRndSeed = evolveRndSeed;
    }

    public void setPickSampleRndSeed(int pickSampleRndSeed)
    {
        _pickSampleRndSeed = pickSampleRndSeed;
    }

    public void setMaxAdditionalTests(int maxAdditionalTests)
    {
        _maxAdditionalTests = maxAdditionalTests;
    }

    public void setStartWeight(double startWeight)
    {
        _startWeight = startWeight;
    }

    public void setRiskUpperBound(double riskUpperBound)
    {
        _riskUpperBound = riskUpperBound;
    }

    public void setClusteringK(int clusteringK)
    {
        _clusteringK = clusteringK;
    }

    public void setDataSetName(String dataSetName)
    {
        _dataSetName = dataSetName;
    }

    public void setStartCityName(String startCityName)
    {
        _startCityName = startCityName;
    }

    public void setCyclesToSample(int cyclesToSample)
    {
        _cyclesToSample = cyclesToSample;
    }

    public void setSamplingProfile(List<FromTo> samplingProfile)
    {
        _samplingProfile = samplingProfile;
    }
}
