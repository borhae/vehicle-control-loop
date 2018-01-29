package de.joachim.haensel.sumo2vrep;

public class IDCreator
{
    private int _currentJunctionID;
    private int _currentEdgeID;

    public IDCreator()
    {
        _currentJunctionID = 0; 
        _currentEdgeID = 0;
    }
    
    public String createJunctionID()
    {
        String result = "junction" + String.format("%05d", _currentJunctionID);
       _currentJunctionID++;
        return result;
    }

    public String createEdgeID()
    {
        String result = "edge" + String.format("%05d", _currentEdgeID);
        _currentEdgeID++;
        return result;
    }
}
