package de.joachim.haensel.phd.scenario.vehicle.test;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.phd.scenario.vehicle.test.TestVehicleSteering.IPathCondition;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingType;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vehicle.IActuatingSensing;
import de.joachim.haensel.vehicle.ILowLevelController;
import de.joachim.haensel.vehicle.ILowerLayerFactory;
import de.joachim.haensel.vehicle.ITopLayerControl;
import de.joachim.haensel.vehicle.ITrajectoryProvider;
import de.joachim.haensel.vehicle.IUpperLayerFactory;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vehicle.VehicleCreator;
import de.joachim.haensel.vehicle.BadReactiveController.DefaultReactiveControllerStateMachine;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TestVehicleSteering
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
    public void testSteerLeftRight() throws VRepException
    {
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator, 1.0f);

        IUpperLayerFactory uperFact = () ->
        {
            return new ITopLayerControl() {

                @Override
                public List<Trajectory> getNewSegments(int segmentRequestSize)
                {
                    return null;
                }

                @Override
                public void initController(IActuatingSensing sensorsActuators, RoadMap roadMap)
                {
                }

                @Override
                public void buildSegmentBuffer(Position2D position2d, RoadMap roadMap)
                {
                }
            };
        };
        TestJustSteeringController llControl = new TestJustSteeringController();
        ILowerLayerFactory lowerFact = () ->
        {
            return llControl;
        };

        Vehicle vehicle = vehicleCreator.createAt(0.0f, 0.0f, 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, null, uperFact, lowerFact);

        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }

        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        vehicle.activateDebugging();
        vehicle.start();
        
        System.out.println("wait here");
        llControl.steer(Math.toRadians(15));
        llControl.steer(Math.toRadians(-15));
        vehicle.stop();
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        vehicle.deacvtivateDebugging();
    }
    
    @Test
    public void testDriveLeft() throws VRepException
    {
        VehicleCreator vehicleCreator = new VehicleCreator(_vrep, _clientID, _objectCreator, 1.0f);

        IUpperLayerFactory uperFact = () ->
        {
            return new ITopLayerControl() {

                @Override
                public List<Trajectory> getNewSegments(int segmentRequestSize)
                {
                    return null;
                }

                @Override
                public void initController(IActuatingSensing sensorsActuators, RoadMap roadMap)
                {
                }

                @Override
                public void buildSegmentBuffer(Position2D position2d, RoadMap roadMap)
                {
                }
            };
        };
        Position2D requiredCenter = new Position2D(0.0f, 0.0f);
        double requiredRadius = 5.0;
        TestCheckingSteeringController llControl = new TestCheckingSteeringController(pos -> {return Math.abs(Position2D.distance(requiredCenter, pos) - requiredRadius) < 0.000000000000001;});
        ILowerLayerFactory lowerFact = () ->
        {
            return llControl;
        };

        Vehicle vehicle = vehicleCreator.createAt(5.0f, 0.0f, 0.0f + vehicleCreator.getVehicleHeight() + 0.2f, null, uperFact, lowerFact);

        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }

        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        vehicle.activateDebugging();
        vehicle.start();
        
        System.out.println("wait here");
        double steeringAngle = Math.tan(vehicle.getBetweenFrontRearWheelsLength() / requiredRadius);
        System.out.println("Formula says we should steer this angle: " + Math.toDegrees(steeringAngle));
        llControl.steer(steeringAngle);
        llControl.setSpeed(-0.5f);
        System.out.println("events should appear");
        
        
        vehicle.stop();
        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
        vehicle.deacvtivateDebugging();
    }

    public interface IPathCondition
    {
        public boolean isOnPath(Position2D pos);
    }

    public class TestCheckingSteeringController implements ILowLevelController
    {
        private IActuatingSensing _actuatorsSensors;
        private float _targetAngle;
        private IPathCondition _pathCondition;
        private float _targetSpeed;

        public TestCheckingSteeringController(IPathCondition pathCondition)
        {
            _pathCondition = pathCondition;
        }
        
        public void setSpeed(float speed)
        {
            _targetSpeed = speed;
        }

        @Override
        public void controlEvent()
        {
            System.out.print(".");
            _actuatorsSensors.computeAndLockSensorData();
            _actuatorsSensors.drive(_targetSpeed, _targetAngle);
            if(!_pathCondition.isOnPath(_actuatorsSensors.getRearWheelCenterPosition()))
            {
//                fail("not on desired path!");
                double radius = 5.0;
                Position2D center = new Position2D(0.0, 0.0);
                System.out.println("Distance between required and actual position: " + Math.abs(Position2D.distance(center, _actuatorsSensors.getRearWheelCenterPosition()) - radius));
            }
        }

        public void steer(double steeringAngle)
        {
            System.out.println("steering: " + steeringAngle + " (in degrees: " + Math.toDegrees(steeringAngle) + ")");
            _targetAngle = (float) steeringAngle;
        }

        @Override
        public void driveTo(Position2D position)
        {
        }

        @Override
        public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
        {
            _actuatorsSensors = actuatorsSensors;
            IVrepDrawing vrepDrawing = (IVrepDrawing)_actuatorsSensors;
            String drawingObjectKey = "TargetCircle";
            vrepDrawing.registerDrawingObject(drawingObjectKey, DrawingType.CIRCLE, Color.BLUE);
            vrepDrawing.updateCircle(drawingObjectKey, new Position2D(0.0, 0.0), 5.0, Color.BLUE);
        }

        @Override
        public void activateDebugging(IVrepDrawing actuatingSensing)
        {
        }

        @Override
        public void deactivateDebugging()
        {
        }
    }
    
    public class TestJustSteeringController implements ILowLevelController
    {
        private IActuatingSensing _actuatorsSensors;
        private float _currentAngle;

        @Override
        public void controlEvent()
        {
            _actuatorsSensors.drive(0.0f, _currentAngle);
            System.out.println("steering in degrees:" + Math.toDegrees(_currentAngle));
        }

        public void steer(double steeringAngle)
        {
            _currentAngle = (float) steeringAngle;
        }

        @Override
        public void driveTo(Position2D position)
        {
        }

        @Override
        public void initController(IActuatingSensing actuatorsSensors, ITrajectoryProvider trajectoryProvider)
        {
            _actuatorsSensors = actuatorsSensors;
        }

        @Override
        public void activateDebugging(IVrepDrawing actuatingSensing)
        {
        }

        @Override
        public void deactivateDebugging()
        {
        }
    }
}
