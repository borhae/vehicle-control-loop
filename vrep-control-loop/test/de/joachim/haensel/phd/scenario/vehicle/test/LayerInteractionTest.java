package de.joachim.haensel.phd.scenario.vehicle.test;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.test.TestConstants;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.sumo2vrep.VRepMap;
import de.joachim.haensel.vehicle.BadReactiveController;
import de.joachim.haensel.vehicle.ILowerLayerFactory;
import de.joachim.haensel.vehicle.IUpperLayerFactory;
import de.joachim.haensel.vehicle.NavigationController;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vehicle.VehicleCreator;
import de.joachim.haensel.vehiclecontrol.Navigator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class LayerInteractionTest implements TestConstants
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
    public static void tearDownVrep() throws VRepException 
    {
        waitForRunningSimulationToStop();
        _vrep.simxFinish(_clientID);
    }

    private static void waitForRunningSimulationToStop() throws VRepException
    {
        IntWA simStatus = new IntWA(1);
        _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript, "simulationState", null, null, null, null, simStatus, null, null, null, remoteApi.simx_opmode_blocking);
        while(simStatus.getArray()[0] != remoteApi.sim_simulation_stopped)
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException exc)
            {
                exc.printStackTrace();
            }
            _vrep.simxCallScriptFunction(_clientID, "ScriptLoader", remoteApi.sim_scripttype_customizationscript, "simulationState", null, null, null, null, simStatus, null, null, null, remoteApi.simx_opmode_blocking);
        }
    }

    @After
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }
    
    @Test
    public void testRouteFollow3JunctionMap() throws VRepException
    {
        RoadMap roadMap = new RoadMap("./res/roadnetworks/testing3Junctions2Edges2Lanes.net.xml");
        Navigator navigator = new Navigator(roadMap);
        Position2D startPosition = new Position2D(11.4f, 101.4f);
        Position2D destinationPosition = new Position2D(101.81f, 9.23f);
        List<Line2D> route = navigator.getRoute(startPosition, destinationPosition);
        VRepMap mapCreator = new VRepMap(DOWN_SCALE_FACTOR, STREET_WIDTH, STREET_HEIGHT, _vrep, _clientID, _objectCreator);
        mapCreator.createMap(roadMap);
        
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator);
        Line2D firstLine = route.get(0);
        Position2D startingPoint = new Position2D(firstLine.getX1(), firstLine.getY1());

        Line2D lastLine = route.get(route.size() - 1);
        Position2D target = new Position2D(lastLine.getX1(), lastLine.getY1());
        
        IUpperLayerFactory uperFact = () -> {return new NavigationController();};
        ILowerLayerFactory lowerFact = () -> {return new BadReactiveController();};
        Vehicle vehicle = vehicleCreator.createAt((float)startingPoint.getX(), (float)startingPoint.getY(), 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, roadMap, uperFact , lowerFact);
        vehicle.setOrientation(0.0f, 0.0f, 0.0f);
        
        vehicle.driveTo((float)target.getX(), (float)target.getY(), roadMap);
    }
    
}
