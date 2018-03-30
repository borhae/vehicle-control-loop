package de.joachim.haensel.sumo2vrep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class IDCreator
{
    private int _currentJunctionID;
    private int _currentLaneID;
    private int _currentPlaneID;
    private HashMap<String, String> _vrep2SumoJunctionsMap;
    private HashMap<String, String> _sumo2VrepJunctionsMap;
    private HashMap<String, String> __vrep2SumoLanesMap;
    private HashMap<String, String> _sumo2VrepLanesMap;

    public IDCreator()
    {
        _currentJunctionID = 0; 
        _currentLaneID = 0;
        _currentPlaneID = 0;
        _vrep2SumoJunctionsMap = new HashMap<>();
        _sumo2VrepJunctionsMap = new HashMap<>();
        __vrep2SumoLanesMap = new HashMap<>();
        _sumo2VrepLanesMap = new HashMap<>();
    }
    
    public String createJunctionID(JunctionType junction)
    {
        String result = "junction" + String.format("%05d", _currentJunctionID);
        _vrep2SumoJunctionsMap.put(result, junction.getId());
        _sumo2VrepJunctionsMap.put(junction.getId(), result);
       _currentJunctionID++;
        return result;
    }

    public String createLaneID(LaneType lane)
    {
        String result = "lane" + String.format("%05d", _currentLaneID);
        String sumoLaneID = lane.getId();
        __vrep2SumoLanesMap.put(result, sumoLaneID);
        _sumo2VrepLanesMap.put(sumoLaneID, result);
        _currentLaneID++;
        return result;
    }

    public List<String> getVRepLanesForSumoEdge(EdgeType edgeType)
    {
        List<LaneType> lanes = edgeType.getLane();
        List<String> result = new ArrayList<>();
        lanes.stream().forEach(lane -> result.add(_sumo2VrepLanesMap.get(lane.getId())));
        return result;
    }

    public String createPlaneID()
    {
        String result = "map" + String.format("%05d", _currentPlaneID);
        return result;
    }
}
