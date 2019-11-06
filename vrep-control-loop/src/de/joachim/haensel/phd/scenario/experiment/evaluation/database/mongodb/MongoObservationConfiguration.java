package de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb;

import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.ObservationConfiguration;

public class MongoObservationConfiguration
{
    private String _experimentID;
    private MongoObservationTuple _observation;
    private List<MongoTrajectoryElement> _configuration;
    private Long _timeStamp;
    
    public MongoObservationConfiguration()
    {
    }

    public MongoObservationConfiguration(String experimentID, List<MongoTrajectoryElement> configuration, MongoObservationTuple observation, Long timeStamp)
    {
        _experimentID = experimentID;
        _configuration = configuration;
        _observation = observation;
        _timeStamp = timeStamp;
    }

    public ObservationConfiguration decode()
    {
        ObservationConfiguration result = new ObservationConfiguration(_observation.decode(), _configuration.stream().map(m -> m.decode()).collect(Collectors.toList()), _timeStamp);
        return result ;
    }
    
    public String getExperimentID()
    {
        return _experimentID;
    }

    public void setExperimentID(String experimentID)
    {
        _experimentID = experimentID;
    }

    public MongoObservationTuple getObservation()
    {
        return _observation;
    }

    public void setObservation(MongoObservationTuple observation)
    {
        _observation = observation;
    }

    public List<MongoTrajectoryElement> getConfiguration()
    {
        return _configuration;
    }

    public void setConfiguration(List<MongoTrajectoryElement> configuration)
    {
        _configuration = configuration;
    }

    public Long getTimeStamp()
    {
        return _timeStamp;
    }

    public void setTimeStamp(Long timeStamp)
    {
        _timeStamp = timeStamp;
    }
}
