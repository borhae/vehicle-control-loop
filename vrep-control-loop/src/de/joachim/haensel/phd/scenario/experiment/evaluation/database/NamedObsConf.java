package de.joachim.haensel.phd.scenario.experiment.evaluation.database;

import java.util.List;

import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.ObservationTuple;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.ObservationConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class NamedObsConf extends ObservationConfiguration
{
    private String _name;

    public NamedObsConf(ObservationTuple observation, List<TrajectoryElement> configuration, Long timestamp)
    {
        super(observation, configuration, timestamp);
    }

    public NamedObsConf(ObservationTuple observation, List<TrajectoryElement> configuration, Long timestamp, String name)
    {
        this(observation, configuration, timestamp);
        _name = name;
    }

    public NamedObsConf(ObservationConfiguration observationConfiguration, String name)
    {
        this(observationConfiguration.getObservation(), observationConfiguration.getConfiguration(), observationConfiguration.getTimeStamp(), name);
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }
}
