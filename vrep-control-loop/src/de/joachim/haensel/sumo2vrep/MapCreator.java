package de.joachim.haensel.sumo2vrep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import coppelia.FloatWA;
import coppelia.StringWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import sumobindings.EdgeType;
import sumobindings.LaneType;
import sumobindings.NetType;

public class MapCreator
{
    /*
     * m104685450_0; 3204.2700195313; 1651.5; 3246.1499023438; 1539.7199707031; 41.8798828125; -111.78002929688; -2.669062609304; 0.35847551258881; 119.36791668618
Lua runtime error: [string "CUSTOMIZATION SCRIPT ScriptLoader"]:46: Illegal object name. (simSetObjectName)
stack traceback:
    [C]: in function 'simSetObjectName'
    [string "createEdge = function(inInts, inFloats, inS..."]:46: in function <[string "createEdge = function(inInts, inFloats, inS..."]:1>
Error: [string -unknown location]:?: Call failed. (simCallScriptFunctionEx on createEdge@ScriptLoader)
     */
    
    private static final String NETWORK_FILE_NAME = "./res/exampleMap/neumarkRealWorldJustCars.net.xml";
//    private static final String NETWORK_FILE_NAME = "./res/exampleMap/1stTestMap.net.xml";
//    private static final String NETWORK_FILE_NAME = "./res/exampleMap/superSimpleMap.net.xml";
//    private static final String NETWORK_FILE_NAME = "./res/exampleMap/testMap5Streets.net.xml";
    private static final float DOWN_SCALE_FACTOR = 1;
    private static final float STREET_WIDTH = 3.3f / DOWN_SCALE_FACTOR;
    private static final float STREET_HEIGHT = 0.4f / DOWN_SCALE_FACTOR;

    public static void main(String[] args)
    {
        try
        {
            VRepRemoteAPI vrep = VRepRemoteAPI.INSTANCE;
            int clientID = vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
            loadFunctions(vrep, clientID);
            IDCreator elementNameCreator = new IDCreator();
            NetType roadNetwork = readSumoMap();
//            List<JunctionType> junctions = roadNetwork.getJunction();
//            for (JunctionType curJunction : junctions)
//            {
//                if(curJunction.getType().equals("internal"))
//                {
//                    continue;
//                }
//                float xPos = curJunction.getX();
//                float yPos = curJunction.getY();
//                createJunction(vrep, clientID, elementNameCreator, xPos, yPos);
//                
//            }
            vrep.simxCallScriptFunction(clientID, "ScriptLoader", 6, "createCenter", null, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);            
            List<EdgeType> edges = roadNetwork.getEdge();
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
                        int numberSubLanes = lineCoordinates.length / 2;
                        if(numberSubLanes == 1)
                        {
                            String p1 = lineCoordinates[0];
                            String p2 = lineCoordinates[1];
                            createLane(vrep, clientID, elementNameCreator, curLane, p1, p2);              
                        }
                        else
                        {
                            createLaneRecursive(vrep, clientID, elementNameCreator, curLane, Arrays.asList(lineCoordinates));
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

    private static void createLaneRecursive(VRepRemoteAPI vrep, int clientID, IDCreator elementNameCreator, LaneType curLane, List<String> lineCoordinates) throws VRepException
    {
        int listSize = lineCoordinates.size();
        if(listSize >= 2)
        {
            createLane(vrep, clientID, elementNameCreator, curLane, lineCoordinates.get(0), lineCoordinates.get(1));
            if(listSize >= 3)
            {
                createLaneRecursive(vrep, clientID, elementNameCreator, curLane, lineCoordinates.subList(1, lineCoordinates.size()-1));
            }
        }
    }

    private static void createJunction(VRepRemoteAPI vrep, int clientID, IDCreator elementNameCreator, float xPos, float yPos) throws VRepException
    {
        FloatWA callParamsFA = new FloatWA(5); 
        float[] floatParameters = callParamsFA.getArray();
        floatParameters[0] = xPos/DOWN_SCALE_FACTOR;
        floatParameters[1] = yPos/DOWN_SCALE_FACTOR;
        floatParameters[2] = 0; // zPos
        floatParameters[3] = STREET_WIDTH;
        floatParameters[4] = STREET_HEIGHT;
        StringWA callParamsS = new StringWA(1);
        callParamsS.getArray()[0] = elementNameCreator.createJunctionID(); //curJunction.getId(); might be double names so skip that
        
        vrep.simxCallScriptFunction(clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript, "createJunction", null, callParamsFA, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);              
    }

    private static void createLane(VRepRemoteAPI vrep, int clientID, IDCreator elementNameCreator, LaneType curLane, String p1, String p2) throws VRepException
    {
        FloatWA callParamsF = new FloatWA(7);
        StringWA callParamsS = new StringWA(1);
        
        //(float x1, float y1, float x2, float y2, float length, height)
        float[] floatParameters = callParamsF.getArray();
        String[] coordinate1 = p1.split(",");
        String[] coordinate2 = p2.split(",");
        
        floatParameters[0] = Float.parseFloat(coordinate1[0])/DOWN_SCALE_FACTOR;
        floatParameters[1] = Float.parseFloat(coordinate1[1])/DOWN_SCALE_FACTOR;

        floatParameters[2] = Float.parseFloat(coordinate2[0])/DOWN_SCALE_FACTOR;
        floatParameters[3] = Float.parseFloat(coordinate2[1])/DOWN_SCALE_FACTOR;
        floatParameters[4] = curLane.getLength()/DOWN_SCALE_FACTOR;
        floatParameters[5] = STREET_WIDTH;
        floatParameters[6] = STREET_HEIGHT;
        
        // (String id)
        String[] stringParameters = callParamsS.getArray();
        stringParameters[0] = elementNameCreator.createEdgeID();//curLane.getId().replace('-', 'm');
        vrep.simxCallScriptFunction(clientID, "ScriptLoader", 6, "createEdge", null, callParamsF, callParamsS, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }

    private static void loadFunctions(VRepRemoteAPI vrep, int clientID)
    {
        StringWA inParamsString = new StringWA(1);
        String scriptText = null;
        try
        {
            try
            {
                scriptText = new String(Files.readAllBytes(Paths.get("./lua/ObjectCreationCustomizationScript.lua")));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }
            inParamsString.getArray()[0] = scriptText;
            StringWA returnStrings = new StringWA(1);
            vrep.simxCallScriptFunction(clientID, "ScriptLoader", 6, "loadCode", null, null, inParamsString, null, null, null, returnStrings, null, remoteApi.simx_opmode_blocking);
            if (returnStrings.getArray().length >= 1)
            {
                String loadReturnValue = returnStrings.getArray()[0];
                System.out.println("script handle: " + loadReturnValue);
            }
        }
        catch (VRepException e)
        {
            e.printStackTrace();
        }
    }

    private static NetType readSumoMap() 
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(NetType.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            FileReader mapFileReader = new FileReader(new File(NETWORK_FILE_NAME));
            return (NetType) unmarshaller.unmarshal(mapFileReader);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
