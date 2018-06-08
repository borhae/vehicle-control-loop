package de.joachim.haensel.phd.scenario.vehicle.vrep;

import java.nio.file.Paths;

import coppelia.IntW;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.simulator.vrep.VRepSimulatorData;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.Vehicle;
import de.joachim.haensel.phd.scenario.vehicle.VehicleWithCameraHandles;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VRepLoadModelVehicleFactory implements IVehicleFactory
{
    private static final String PHYSICAL_CAR_BODY_NAME = "physicalCarBody";
    private IVehicleConfiguration _vehicleConf;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;

    public VRepLoadModelVehicleFactory(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator, double scale)
    {
        _vrep = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
    }

    @Override
    public void configure(IVehicleConfiguration vehicleConf)
    {
        _vehicleConf = vehicleConf;
    }

    @Override
    public IVehicle createVehicleInstance()
    {
        try
        {
            IntW baseHandle = new IntW(0);
            _vrep.simxLoadModel(_clientID, Paths.get("./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm").toAbsolutePath().toString(), 0, baseHandle, remoteApi.simx_opmode_blocking);
            VehicleWithCameraHandles handles = new VehicleWithCameraHandles();
            
            handles.setAxisRearLeft(getHandle("axisRearLeft"));
            handles.setAxisRearRight(getHandle("axisRearRight"));
            
            handles.setConnectorDflSfl(getHandle("connectorDflSfl"));
            handles.setConnectorDfrSfr(getHandle("connectorDfrSfr"));
            handles.setConnectorDrlArl(getHandle("connectorDrlArl"));
            handles.setConnectorDrrArr(getHandle("connectorDrrArr"));
            handles.setConnectorSflMfl(getHandle("connectorSflMfl"));
            handles.setConnectorSfrMfr(getHandle("connectorSfrMfr"));
            
            handles.setDamperFrontLeft(getHandle("damperFrontLeft"));
            handles.setDamperFrontRight(getHandle("damperFrontRight"));
            handles.setDamperRearLeft(getHandle("damperRearLeft"));
            handles.setDamperRearRight(getHandle("damperRearRight"));
            
            handles.setFrontLeftWheel(getHandle("frontLeftWheel"));
            handles.setFrontRightWheel(getHandle("frontRightWheel"));
            handles.setRearLeftWheel(getHandle("rearLeftWheel"));
            handles.setRearRightWheel(getHandle("rearRightWheel"));
            
            handles.setMotorFrontLeft(getHandle("motorFrontLeft"));
            handles.setMotorFrontRight(getHandle("motorFrontRight"));
            
            handles.setPhysicalBody(getHandle(PHYSICAL_CAR_BODY_NAME));
            
            handles.setSteeringFrontLeft(getHandle("steeringFrontLeft"));
            handles.setSteeringFrontRight(getHandle("steeringFrontRight"));
            
            handles.setRearWheelVisualizationDummy(getHandle("lineSegmentRear"));
            handles.setFrontLeftWheelDummy(getHandle("lineSegmentFrontLeft"));
            handles.setFrontRightWheelDummy(getHandle("lineSegmentFrontRight"));
            
            handles.setCamera(getHandle("autoFittingCamera"));
            
            handles.setCtrlScript(_objectCreator.getScriptAssociatedWithObject(handles.getPhysicalBody()));
            
            
            VRepSimulatorData simulatorData = new VRepSimulatorData(_objectCreator, _vrep, _clientID, PHYSICAL_CAR_BODY_NAME);
            Vehicle vehicle = new Vehicle(simulatorData, handles, _vehicleConf.getMap(), _vehicleConf.getUpperCtrlFactory(), _vehicleConf.getLowerCtrlFactory());

            vehicle.setPosition((float)_vehicleConf.getXPos(), (float)_vehicleConf.getYPos(), (float)_vehicleConf.getZPos());
            Vector2D orientationToAlignTo = _vehicleConf.getOrientation();
            if(orientationToAlignTo != null)
            {
                Vector2D orientation = vehicle.getOrientation();
                double correctionAngle = Vector2D.computeAngle(orientation, orientationToAlignTo);
                vehicle.setOrientation((float)0.0, (float)0.0, (float)correctionAngle);
            }
            
            _objectCreator.addToDeletionList(handles.getAllObjectHandles());
            return vehicle;
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        return null;
    }

    private int getHandle(String objectName) throws VRepException
    {
        IntW handle = new IntW(1);
        _vrep.simxGetObjectHandle(_clientID, objectName, handle, remoteApi.simx_opmode_blocking);
        return handle.getValue();
    }
}
