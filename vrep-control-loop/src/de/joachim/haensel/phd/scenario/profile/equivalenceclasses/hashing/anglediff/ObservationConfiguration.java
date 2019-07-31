package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing.anglediff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class ObservationConfiguration implements Comparable<ObservationConfiguration>
{
    private ObservationTuple _observation;
    private List<TrajectoryElement> _configuration;
    private Long _timestamp;

    public ObservationConfiguration(ObservationTuple observation, List<TrajectoryElement> configuration, Long timestamp)
    {
        _observation = observation;
        _configuration = configuration;
        _timestamp = timestamp;
    }

    public static List<ObservationConfiguration> create(Map<Long, ObservationTuple> observations, Map<Long, List<TrajectoryElement>> configurations)
    {
        List<ObservationConfiguration> result = new ArrayList<>();
        Set<Entry<Long, ObservationTuple>> observationEntries = observations.entrySet();
        for (Entry<Long, ObservationTuple> curEntry : observationEntries)
        {
            result.add(new ObservationConfiguration(curEntry.getValue(), configurations.get(curEntry.getKey()), curEntry.getKey()));
        }
        return result;
    }

    @Override
    public int compareTo(ObservationConfiguration other)
    {
        int obsComp = _observation.compareTo(other.getObservation());
        if(obsComp == 0)
        {
            List<TrajectoryElement> otherConfig = other.getConfiguration();
            int sizeComp = _configuration.size() - otherConfig.size();
            if(sizeComp == 0)
            {
                for(int idx = 0; idx < _configuration.size(); idx++)
                {
                    TrajectoryElement curThis = _configuration.get(idx);
                    TrajectoryElement curOther = otherConfig.get(idx);
                    int curElemComp = curThis.compareTo(curOther);
                    if(curElemComp != 0)
                    {
                        return curElemComp;
                    }
                }
                return sizeComp;
            }
            else
            {
                return sizeComp;
            }
        }
        else
        {
            return obsComp;
        }
    }

    public List<TrajectoryElement> getConfiguration()
    {
        return _configuration;
    }

    public ObservationTuple getObservation()
    {
        return _observation;
    }
}
