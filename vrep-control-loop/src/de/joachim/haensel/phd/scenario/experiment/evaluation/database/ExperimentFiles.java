package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.io.File;

public class ExperimentFiles
{
    private String _configuration;
    private String _observation;

    public ExperimentFiles(String configuration, String observation)
    {
        _configuration = configuration;
        _observation = observation;
    }

    public File getConfigurationsFile()
    {
        return new File(_configuration);
    }

    public File getObservationsFile()
    {
        return new File(_observation);
    }
}
