package de.joachim.haensel.sumo2vrep;

import java.util.Arrays;
import java.util.List;

import coppelia.FloatWA;
import coppelia.StringWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import sumobindings.EdgeType;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class VRepMap
{
    private float _downScaleFactor;
    private float _streetWidth;
    private float _streetHeight;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _vrepObjectCreator;
    private IDCreator _elementNameCreator;
    
    public VRepMap(float downScaleFactor, float streetWidth, float streetHeight, VRepRemoteAPI vrep, int clientID, VRepObjectCreation vrepObjectCreator)
    {   
        _downScaleFactor = downScaleFactor;
        _streetWidth = streetWidth;
        _streetHeight = streetHeight;
        _vrep = vrep;
        _clientID = clientID;
        _vrepObjectCreator = vrepObjectCreator;
    }

    public void createMap(RoadMap roadMap)
    {
        _elementNameCreator = new IDCreator();
        try
        {
            List<JunctionType> junctions = roadMap.getJunctions();
            for (JunctionType curJunction : junctions)
            {
                if(curJunction.getType().equals("internal"))
                {
                    continue;
                }
                createJunction(_vrep, _clientID, _elementNameCreator, curJunction);
            }
            _vrepObjectCreator.createMapCenter();
            List<EdgeType> edges = roadMap.getEdges();
            int numOfLanes = computeNumOfLanes(edges);
            System.out.println("about to create: " + numOfLanes + " lanes!!");
            for (EdgeType curEdge : edges)
            {
                String function = curEdge.getFunction();
                if(function == null || function.isEmpty())
                {
                    List<LaneType> lanes = curEdge.getLane();
                    for (LaneType curLane : lanes)
                    {
                        String shape = curLane.getShape();
                        String[] lineCoordinates = shape.split(" ");
                        int numberCoordinates = lineCoordinates.length;
                        if(numberCoordinates == 2)
                        {
                            String p1 = lineCoordinates[0];
                            String p2 = lineCoordinates[1];
                            createLane(_vrep, _clientID, _elementNameCreator, curLane, p1, p2);              
                        }
                        else
                        {
                            createLaneRecursive(_vrep, _clientID, _elementNameCreator, curLane, Arrays.asList(lineCoordinates));
                        }
                    }
                }
            }
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
    }

    private int computeNumOfLanes(List<EdgeType> edges)
    { 
        int result = 0;
        for (EdgeType curEdge : edges)
        {
            String function = curEdge.getFunction();
            if(function == null || function.isEmpty())
            {
                List<LaneType> lanes = curEdge.getLane();
                for (LaneType curLane : lanes)
                {
                    String shape = curLane.getShape();
                    String[] lineCoordinates = shape.split(" ");
                    result += lineCoordinates.length - 1;
                }
            }
        }

        return result;
    }

    public void createLaneRecursive(VRepRemoteAPI vrep, int clientID, IDCreator elementNameCreator, LaneType curLane, List<String> lineCoordinates) throws VRepException
    {
        int listSize = lineCoordinates.size();
        if(listSize >= 2)
        {
            createLane(vrep, clientID, elementNameCreator, curLane, lineCoordinates.get(0), lineCoordinates.get(1));
            if(listSize >= 3)
            {
                createLaneRecursive(vrep, clientID, elementNameCreator, curLane, lineCoordinates.subList(1, lineCoordinates.size()));
            }
        }
    }

    public void createJunction(VRepRemoteAPI vrep, int clientID, IDCreator elementNameCreator, JunctionType junction) throws VRepException
    {
        float xPos = junction.getX();
        float yPos = junction.getY();

        FloatWA callParamsFA = new FloatWA(5); 
        float[] floatParameters = callParamsFA.getArray();
        floatParameters[0] = xPos/_downScaleFactor;
        floatParameters[1] = yPos/_downScaleFactor;
        floatParameters[2] = 0; // zPos
        floatParameters[3] = _streetWidth;
        floatParameters[4] = _streetHeight;
        StringWA callParamsS = new StringWA(1);
        callParamsS.getArray()[0] = elementNameCreator.createJunctionID(junction);
        
        vrep.simxCallScriptFunction(clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript, "createJunction", null, callParamsFA, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public void createLane(VRepRemoteAPI vrep, int clientID, IDCreator elementNameCreator, LaneType curLane, String p1, String p2) throws VRepException
    {
        FloatWA callParamsF = new FloatWA(7);
        StringWA callParamsS = new StringWA(1);
        
        float[] floatParameters = callParamsF.getArray();
        String[] coordinate1 = p1.split(",");
        String[] coordinate2 = p2.split(",");
        
        floatParameters[0] = Float.parseFloat(coordinate1[0])/_downScaleFactor;
        floatParameters[1] = Float.parseFloat(coordinate1[1])/_downScaleFactor;

        floatParameters[2] = Float.parseFloat(coordinate2[0])/_downScaleFactor;
        floatParameters[3] = Float.parseFloat(coordinate2[1])/_downScaleFactor;
        floatParameters[4] = curLane.getLength()/_downScaleFactor;
        floatParameters[5] = _streetWidth;
        floatParameters[6] = _streetHeight;
        
        String[] stringParameters = callParamsS.getArray();
        stringParameters[0] = elementNameCreator.createLaneID(curLane);
        vrep.simxCallScriptFunction(clientID, "ScriptLoader", 6, "createEdge", null, callParamsF, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }
    
    public void deleteAll() throws VRepException
    {
        _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", 6, "deleteCreated", null, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    public IDCreator getIDMapper()
    {
        return _elementNameCreator;
    }
}
