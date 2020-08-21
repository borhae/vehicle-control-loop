package de.joachim.haensel.phd.scenario.experiment.recipe;

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

    public int getEvolveRndSeed()
    {
        return _evolveRndSeed;
    }

    public int getPickSampleRndSeed()
    {
        return _pickSampleRndSeed;
    }
}
