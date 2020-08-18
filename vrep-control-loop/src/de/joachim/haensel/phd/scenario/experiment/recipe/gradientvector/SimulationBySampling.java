package de.joachim.haensel.phd.scenario.experiment.recipe.gradientvector;

import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.extractForCity;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.getClusterIndices;
import static de.joachim.haensel.phd.scenario.experiment.evaluation.ClusteringInformationRetreival.reverseClustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.experiment.evaluation.database.Trajectory3DSummaryStatistics;
import de.joachim.haensel.phd.scenario.experiment.evaluation.database.mongodb.MongoTrajectory;
import de.joachim.haensel.phd.scenario.math.FromTo;
import de.joachim.haensel.phd.scenario.math.Linspace;

public class SimulationBySampling
{
    private Map<Integer, Trajectory3DSummaryStatistics> _reversedMap;
    private Map<Trajectory3DSummaryStatistics, List<Integer>> _startCityClustering;
    private Map<Trajectory3DSummaryStatistics, List<Integer>> _evolveIntoCityClustering;
    private HashMap<Integer, Integer> _clusterCounts;
    private ArrayList<Integer> _startCityIndices;
    private ArrayList<Integer> _evlovIntoCityIndices;
    private int _cyclesToSample;
    private int _samplesPerCycle;
    private List<Double> _cityProbability;
    private int _currentCycle;
    private Random _evolveRandom;
    private Deque<Integer> _startCityUsableIndices;
    private Deque<Integer> _evolveIntoCityUsableIndices;
    private int _batchCntStartCity;
    private int _batchCntEvolveIntoCity;
    private Random _shuffleRandom;
    private String _startCityName;
    private String _evolveIntoCityName;
    private Map<Trajectory3DSummaryStatistics, List<Integer>> _clustering;
    private Map<Integer, MongoTrajectory> _dbTrajectories;

    public SimulationBySampling(Random evolveRandom, Random shuffleRandom, String startCityName, String evolveIntoCityName, Map<Trajectory3DSummaryStatistics, List<Integer>> clustering, Map<Integer, MongoTrajectory> dbTrajectories)
    {
        _currentCycle = -1;
        _evolveRandom = evolveRandom;
        _shuffleRandom = shuffleRandom;
        _startCityName = startCityName;
        _evolveIntoCityName = evolveIntoCityName;
        _clustering = clustering;
        _dbTrajectories = dbTrajectories;
    }

    public void initialize()
    {
        _reversedMap = reverseClustering(_clustering);

        _startCityClustering = extractForCity(_dbTrajectories, _clustering, _clustering.keySet(), _startCityName);
        _evolveIntoCityClustering = extractForCity(_dbTrajectories, _clustering, _clustering.keySet(), _evolveIntoCityName);

        _startCityIndices = new ArrayList<Integer>(getClusterIndices(_startCityClustering));
        _evlovIntoCityIndices = new ArrayList<Integer>(getClusterIndices(_evolveIntoCityClustering));
        
        _clusterCounts = initializeClusterCountsFrom(_startCityClustering);

        Collections.shuffle(_startCityIndices, _shuffleRandom);
        Collections.shuffle(_evlovIntoCityIndices, _shuffleRandom);
        
        _startCityUsableIndices = new LinkedList<Integer>(_startCityIndices);
        _evolveIntoCityUsableIndices = new LinkedList<Integer>(_evlovIntoCityIndices);
    }
    
    private HashMap<Integer, Integer> initializeClusterCountsFrom(Map<Trajectory3DSummaryStatistics, List<Integer>> clustering)
    {
        Stream<Entry<Trajectory3DSummaryStatistics, List<Integer>>> entrySetStream = clustering.entrySet().stream();
        Map<Integer, Integer> clusterNrs = entrySetStream.collect(Collectors.toMap(entry -> entry.getKey().getClusterNr(), entry -> entry.getValue().size()));
        return new HashMap<Integer, Integer>(clusterNrs);
    }

    public Map<Trajectory3DSummaryStatistics, List<Integer>> getStartCityClustering()
    {
        return _startCityClustering;
    }

    public void setCyclesToSample(int cyclesToSample)
    {
        _cyclesToSample = cyclesToSample;
    }

    public void setSamplesPerCycle(int samplesPerCycle)
    {
        _samplesPerCycle = samplesPerCycle;
    }

    public void setSamplingProfile(ArrayList<FromTo> fromToList)
    {
        int partSize = _cyclesToSample/fromToList.size();
        _cityProbability = fromToList.stream().flatMap(fromTo -> Linspace.linspace(fromTo, partSize).stream()).collect(Collectors.toList());
    }

    public Map<Integer, Integer> getClusterCounts()
    {
        return _clusterCounts;
    }

    public boolean hasMoreCycles()
    {
        return _currentCycle < _cyclesToSample - 1;
    }

    public void sample()
    {
        _currentCycle++;
        _batchCntStartCity = 0;
        _batchCntEvolveIntoCity = 0;
        for(int idx = 0; idx < _samplesPerCycle; idx++)
        {
            if(_evolveRandom.nextDouble() < _cityProbability.get(_currentCycle))
            {
                if(_evolveIntoCityUsableIndices.isEmpty())
                {
                    //refill if empty, don't forget to shuffle
                    Collections.shuffle(_evlovIntoCityIndices, _shuffleRandom);
                    _evolveIntoCityUsableIndices.addAll(_evlovIntoCityIndices);
                }
                Integer trajectoryIdx = _evolveIntoCityUsableIndices.remove();
                int clusterNr = _reversedMap.get(trajectoryIdx).getClusterNr();
                _clusterCounts.put(clusterNr, _clusterCounts.get(clusterNr) == null ? 1 : _clusterCounts.get(clusterNr) + 1);
                _batchCntEvolveIntoCity++;
            }
            else
            {
                if(_startCityUsableIndices.isEmpty())
                {
                    //refill if empty, don't forget to shuffle
                    Collections.shuffle(_startCityIndices, _shuffleRandom);
                    _startCityUsableIndices.addAll(_startCityIndices);
                }
                Integer trajectoryIdx = _startCityUsableIndices.remove();
                int clusterNr = _reversedMap.get(trajectoryIdx).getClusterNr();
                _clusterCounts.put(clusterNr, _clusterCounts.get(clusterNr) == null ? 1 : _clusterCounts.get(clusterNr) + 1);
                _batchCntStartCity++;
            }
        }
    }

    public int getBatchCntStartCity()
    {
        return _batchCntStartCity;
    }

    public int getBatchCntEvolveIntoCity()
    {
        return _batchCntEvolveIntoCity;
    }

    public int getCycle()
    {
        return _currentCycle;
    }
}
