package de.joachim.haensel.phd.scenario.vehicle.test;

import java.awt.Color;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.ITrajectoryProvider;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.Vehicle;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryReportListener;
import de.joachim.haensel.phd.scenario.vehicle.control.interfacing.ITrajectoryRequestListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepPartwiseVehicleCreator;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingType;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class TestVehicleSteering
{
    private static VRepRemoteAPI _vrep;
    private static int _clientID;
    private static VRepObjectCreation _objectCreator;

    @BeforeAll
    public static void setupVrep() throws VRepException
    {
        _vrep = VRepRemoteAPI.INSTANCE;
        _clientID = _vrep.simxStart("127.0.0.1", 19999, true, true, 5000, 5);
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
    }

    @AfterAll
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

    @AfterEach
    public void cleanUpObjects() throws VRepException
    {
        _objectCreator.deleteAll();
    }
    
    @Test
    public void testSteerLeftRight() throws VRepException
    {
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, 1.0f);

        IUpperLayerFactory uperFact = () ->
        {
            return new IUpperLayerControl() {

                @Override
                public List<TrajectoryElement> getNewSegments(int segmentRequestSize)
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

                @Override
                public void activateDebugging(DebugParams params)
                {
                }

                @Override
                public void deactivateDebugging()
                {
                }

                @Override
                public void addRouteBuilderListener(IRouteBuildingListener listener)
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
        DebugParams debParam = new DebugParams();
        debParam.setSimulationDebugMarkerHeight(1.0);
        vehicle.activateDebugging(debParam);

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
        VRepPartwiseVehicleCreator vehicleCreator = new VRepPartwiseVehicleCreator(_vrep, _clientID, _objectCreator, 1.0f);

        IUpperLayerFactory uperFact = () ->
        {
            return new IUpperLayerControl() {

                @Override
                public List<TrajectoryElement> getNewSegments(int segmentRequestSize)
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

                @Override
                public void activateDebugging(DebugParams params)
                {
                }

                @Override
                public void deactivateDebugging()
                {
                }

                @Override
                public void addRouteBuilderListener(IRouteBuildingListener listener)
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
        DebugParams debParam = new DebugParams();
        debParam.setSimulationDebugMarkerHeight(1.0);
        vehicle.activateDebugging(debParam);

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

    public class TestCheckingSteeringController implements ILowerLayerControl<Object>
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
            vrepDrawing.updateCircle(drawingObjectKey, new Position2D(0.0, 0.0), 1.0, 5.0, Color.BLUE);
        }

        @Override
        public void activateDebugging(IVrepDrawing actuatingSensing, DebugParams debugParams)
        {
        }

        @Override
        public void deactivateDebugging()
        {
        }

        @Override
        public void setParameters(Object parameters)
        {
        }

        @Override
        public void stop()
        {
        }

        @Override
        public void addTrajectoryRequestListener(ITrajectoryRequestListener requestListener)
        {
        }

        @Override
        public void addTrajectoryReportListener(ITrajectoryReportListener reportListener)
        {
        }

        @Override
        public void addArrivedListener(IArrivedListener arrivedListener)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void clearSegmentBuffer()
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void clearArrivedListeners()
        {
            // TODO Auto-generated method stub
            
        }
    }
    
    public class TestJustSteeringController implements ILowerLayerControl<Object>
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
        public void activateDebugging(IVrepDrawing drawing, DebugParams debugParams)
        {
        }

        @Override
        public void deactivateDebugging()
        {
        }

        @Override
        public void setParameters(Object parameters)
        {
        }

        @Override
        public void stop()
        {
        }

        @Override
        public void addTrajectoryRequestListener(ITrajectoryRequestListener requestListener)
        {
        }

        @Override
        public void addTrajectoryReportListener(ITrajectoryReportListener reportListener)
        {
        }

        @Override
        public void addArrivedListener(IArrivedListener arrivedListener)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void clearSegmentBuffer()
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void clearArrivedListeners()
        {
            // TODO Auto-generated method stub
            
        }
    }
}
