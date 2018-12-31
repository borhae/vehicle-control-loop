package de.joachim.haensel.phd.scenario.map.sumo2vrep.test;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.StringWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.XYMinMax;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.IDCreator;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;
import sumobindings.EdgeType;
import sumobindings.LaneType;


public class MapCreationTest implements TestConstants
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    @BeforeClass
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }
    
    @AfterClass
    public static void tearDownVrep() 
    {
        _vrep.simxFinish(_clientID);
    }
    
    @After
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }
    
    @Test 
    public void testSimpleVrepObjectCreation()
    {
        StringWA inParamsString = new StringWA(1);
        String scriptText = null;
        try
        {
            try
            {
                scriptText = new String(Files.readAllBytes(Paths.get("./lua/TestScript.lua")));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }
            inParamsString.getArray()[0] = scriptText;
            StringWA returnStrings = new StringWA(1);
            _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", 6, "loadCode", null, null, inParamsString, null, null, null, returnStrings, null, remoteApi.simx_opmode_blocking);
            if (returnStrings.getArray().length >= 1)
            {
                String loadReturnValue = returnStrings.getArray()[0];
                System.out.println("script handle: " + loadReturnValue);
            }
        }
        catch (VRepException e)
        {
            fail("should not happen");
        }
    }

    @Test
    public void testLoadSimpleMap() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
    }
    
    @Test
    public void testLoadSimpleMapMesh() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        roadMap.center(0.0, 0.0);
        
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
    }

    @Test
    public void testCheckRoadMapAssignements() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/superSimpleMap.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
        IDCreator idMapper = mapCreator.getIDMapper();
        EdgeType edgeZero = getFirstNonInternal(roadMap);
        assert(edgeZero != null);

        List<LaneType> lanes = edgeZero.getLane();
        assert(lanes.size() == 1);
        LaneType lane = lanes.get(0); 
        Position2D expected = computeCenter(lane);
        
        List<String> vrepLaneNames = idMapper.getVRepLanesForSumoEdge(edgeZero);
        assert(vrepLaneNames.size() == 1); 
        
        IntW vrepLaneHandle = new IntW(0);
        _vrep.simxGetObjectHandle(_clientID, vrepLaneNames.get(0), vrepLaneHandle, remoteApi.simx_opmode_blocking);
        FloatWA vrep3dPosition = new FloatWA(3);
        _vrep.simxGetObjectPosition(_clientID, vrepLaneHandle.getValue(), -1, vrep3dPosition, remoteApi.simx_opmode_blocking);
        Position2D actual = new Position2D(vrep3dPosition);
        
        assertTrue("Broken model references", Position2D.equals(expected, actual, 0.0001f));
    }

    private Position2D computeCenter(LaneType lane)
    {
        String[] pointsOnSumoLane = lane.getShape().split(" ");
        Position2D p1 = new Position2D(pointsOnSumoLane[0]);
        Position2D p2 = new Position2D(pointsOnSumoLane[1]);
        return Position2D.between(p1, p2);
    }

    private EdgeType getFirstNonInternal(RoadMap roadMap)
    {
        List<EdgeType> edges = roadMap.getEdges();
        Optional<EdgeType> firstEdge = edges.stream().filter(edge -> edge.getFunction() == null || edge.getFunction().isEmpty()).findFirst();
        return firstEdge.get();
    }
    
    @Test 
    public void testMidRangeSyntheticMap() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testMap5Streets.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
    }

    @Test
    public void testLoadRealWorldMap() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createSimplesShapeBasedMap(roadMap);
    }
    
    @Test
    public void testLoadRealWorlMapPlaneVisualization()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMapSizedRectangle(roadMap, true);
    }
    
    @Test
    public void testLoadRealWorldMapCheckPlaneVsRoads()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMapSizedRectangle(roadMap, true);
        mapCreator.createSimplesShapeBasedMap(roadMap);
    }
    
    @Test 
    public void testLoadMidRangeSyntheticMapTextureOnPlane()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testMap5Streets.net.xml");
        XYMinMax mapDimensions = roadMap.computeMapDimensions();
        TMatrix centerMatrix = TMatrix.createCenterMatrix(mapDimensions);
        roadMap.transform(centerMatrix);
        
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMapSizedRectangleWithMapTexture(roadMap);
    }
    
    @Test 
    public void testLoadMidRangeSyntheticMapTextureOnPlaneAndSimpleShapes()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testMap5Streets.net.xml");
        XYMinMax mapDimensions = roadMap.computeMapDimensions();
        TMatrix centerMatrix = TMatrix.createCenterMatrix(mapDimensions);
        roadMap.transform(centerMatrix);
        
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMapSizedRectangleWithMapTexture(roadMap);
        mapCreator.createSimplesShapeBasedMap(roadMap);
    }
    
    @Test 
    public void testLoadRealWorldMapTextureOnPlane()
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        XYMinMax mapDimensions = roadMap.computeMapDimensions();
        TMatrix centerMatrix = TMatrix.createCenterMatrix(mapDimensions);
        roadMap.transform(centerMatrix);
        
        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMapSizedRectangleWithMapTexture(roadMap);
    }
    
    @Test 
    public void testLoadRealWorldMapMesh() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        XYMinMax mapDimensions = roadMap.computeMapDimensions();
        TMatrix centerMatrix = TMatrix.createCenterMatrix(mapDimensions);
        roadMap.transform(centerMatrix);
        
        VRepMap mapCreator = new VRepMap(1.6f, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
    }

    @Test
    public void testLoadRealWorldJustPlanMapAndStartSimulation() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/neumarkRealWorldJustCars.net.xml");
        roadMap.transform(DOWN_SCALE_FACTOR, 0.0f, 0.0f);

        VRepMap mapCreator = new VRepMap(STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMapSizedRectangle(roadMap, true);
        
        ShapeParameters cubeParams = new ShapeParameters();
        cubeParams.setIsDynamic(true);
        cubeParams.setIsRespondable(true);
        cubeParams.setMass(10);
        cubeParams.setName("dropme");
        cubeParams.setOrientation(0.0f, 0.0f, 0.0f);
        cubeParams.setPosition(4588.0f, 3701.0f, 40.0f);
        cubeParams.setRespondableMask(ShapeParameters.GLOBAL_AND_LOCAL_RESPONDABLE_MASK);
        cubeParams.setSize(10.0f, 10.0f, 10.0f);
        cubeParams.setType(EVRepShapes.CUBOID);
        _objectCreator.createPrimitive(cubeParams);
        
        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
    }
    
    @Test
    public void testSizeIssues() throws VRepException
    {
        ShapeParameters planeParams = new ShapeParameters();
        planeParams.setIsDynamic(false);
        planeParams.setIsRespondable(false);
        planeParams.setMass(10);
        planeParams.setName("plane");
        planeParams.setOrientation(0.0f, 0.0f, 0.0f);
        planeParams.setPosition(4588.0f, 3701.0f, 0.0f);
        planeParams.setRespondableMask(ShapeParameters.GLOBAL_AND_LOCAL_RESPONDABLE_MASK);
        planeParams.setSize(100.0f, 100.0f, 1.0f);
        planeParams.setType(EVRepShapes.CUBOID);
        _objectCreator.createPrimitive(planeParams);

        ShapeParameters cubeParams = new ShapeParameters();
        cubeParams.setIsDynamic(true);
        cubeParams.setIsRespondable(true);
        cubeParams.setMass(10);
        cubeParams.setName("dropme");
        cubeParams.setOrientation(0.0f, 0.0f, 0.0f);
        cubeParams.setPosition(4588.0f, 3701.0f, 40.0f);
        cubeParams.setRespondableMask(ShapeParameters.GLOBAL_AND_LOCAL_RESPONDABLE_MASK);
        cubeParams.setSize(10.0f, 10.0f, 10.0f);
        cubeParams.setType(EVRepShapes.CUBOID);
        _objectCreator.createPrimitive(cubeParams);
        
        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
    }
}
