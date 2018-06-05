package de.joachim.haensel.phd.scenario.scripts.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vehicle.PurePursuitController;
import de.joachim.haensel.vehicle.PurePursuitParameters;
import de.joachim.haensel.vehicle.ILowerLayerFactory;
import de.joachim.haensel.vehicle.IUpperLayerControl;
import de.joachim.haensel.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.debug.Speedometer;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.VRepLoadModelVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleConfiguration;
import de.joachim.haensel.vehicle.DefaultNavigationController;
import de.joachim.haensel.vehicle.ILowerLayerControl;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vehicle.VehicleCreator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;

public class ScriptFunctionsTest
{
    private static final String PHYSICAL_CAR_BODY_NAME = "physicalCarBody";

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
    public void testDriveForward() throws VRepException
    {
        IVehicleFactory vehicleFactory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, 1.0); 
        VRepVehicleConfiguration vehicleConf = new VRepVehicleConfiguration();
        vehicleConf.setLowerCtrlFactory(new ILowerLayerFactory() {
            @Override
            public ILowerLayerControl<Object> create()
            {
                ILowerLayerControl<Object> ctrl = new MonitoringController();
                ctrl.setParameters(new PurePursuitParameters(5.0, 0.25));
                return ctrl;
            }
        });
        vehicleConf.setUpperCtrlFactory(new IUpperLayerFactory() {
            @Override
            public IUpperLayerControl create()
            {
                IUpperLayerControl ctrl = new DefaultNavigationController(5.0, 75);
                return ctrl;
            }
        });
        vehicleConf.setPosition(0.0, 90.0, 0.5);
        vehicleFactory.configure(vehicleConf);
        IVehicle vehicle = vehicleFactory.createVehicleInstance();
        DebugParams debParam = new DebugParams();
        Speedometer speedometer = Speedometer.createWindow();
        debParam.setSpeedometer(speedometer);

        vehicle.activateDebugging(debParam);
        ShapeParameters params = new ShapeParameters();
        params.setIsDynamic(false);
        params.setIsRespondable(true);
        params.setMass(50);
        params.setName("ground");
        params.setOrientation(0.0f, 0.0f, 0.0f);
        params.setPosition(0.0f, 0.0f, 0.0f);
        params.setRespondableMask(ShapeParameters.GLOBAL_AND_LOCAL_RESPONDABLE_MASK);
        params.setSize(20.0f, 200.0f, 0.1f);
        params.setType(EVRepShapes.CUBOID);
        params.setVisibility(true);
        _objectCreator.createPrimitive(params);
        
        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        
        vehicle.start();

        FloatWA inFloats = new FloatWA(2);
        inFloats.getArray()[0] = (float)0;
        inFloats.getArray()[1] = (float)4.0;
        _vrep.simxCallScriptFunction(_clientID, PHYSICAL_CAR_BODY_NAME, remoteApi.sim_scripttype_childscript, "control", null, inFloats, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);

        vehicle.stop();
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
    }
}
