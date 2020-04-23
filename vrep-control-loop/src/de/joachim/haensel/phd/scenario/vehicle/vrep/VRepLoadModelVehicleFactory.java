package de.joachim.haensel.phd.scenario.vehicle.vrep;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import coppelia.IntW;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.simulator.vrep.VRepSimulatorAndVehicleData;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensingFactory;
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
    private String _vrepModelPath;

    public VRepLoadModelVehicleFactory(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator, String vrepModelFilePath)
    {
        _vrep = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
        _vrepModelPath = vrepModelFilePath;
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
            IntW handle = new IntW(-1);
            int errVal = -1;
            VRepException exc = null;
            try
            {
                _vrep.simxGetObjectHandle(_clientID, PHYSICAL_CAR_BODY_NAME, handle, remoteApi.simx_opmode_blocking);
            }
            catch (VRepException e) 
            {
                errVal = e.getRetVal();
                exc = e;
            }
            boolean carAlreadyLoaded = false;
            if(handle.getValue() != errVal)
            {
                carAlreadyLoaded  = true;
            }
            else
            {
                if(errVal != 1 && errVal != 8)
                {
                    throw exc;
                }
            }
            IntW baseHandle = new IntW(0);
            if(!carAlreadyLoaded)
            {
                _vrep.simxLoadModel(_clientID, Paths.get(_vrepModelPath).toAbsolutePath().toString(), 0, baseHandle, remoteApi.simx_opmode_blocking);
            }
            
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
            handles.setAdditionalObjectHandles(_objectCreator.getHandlesForNames(_vehicleConf.getAutoBodyNames()));
            
            List<Integer> handlesToBeRemoved = new ArrayList<>();
            handles.setAdditionalObjectHandles(handlesToBeRemoved );
            
            VRepSimulatorAndVehicleData simulatorData = new VRepSimulatorAndVehicleData(_objectCreator, _vrep, _clientID, PHYSICAL_CAR_BODY_NAME);
            IActuatingSensingFactory actuatingSensingFactory = 
                    () -> {return new VRepVehicleActuatorsSensors(handles, new VRepSimulatorAndVehicleData(_objectCreator, _vrep, _clientID, PHYSICAL_CAR_BODY_NAME), _vehicleConf.getMap());};
            Vehicle vehicle = new Vehicle(simulatorData, handles, actuatingSensingFactory, _vehicleConf);

            vehicle.setPosition((float)_vehicleConf.getXPos(), (float)_vehicleConf.getYPos(), (float)_vehicleConf.getZPos());
            Vector2D orientationToAlignTo = _vehicleConf.getOrientation();
            if(orientationToAlignTo != null)
            {
                Vector2D orientation = vehicle.getOrientation();
                double correctionAngle = Vector2D.computeAngleSpecial(orientation, orientationToAlignTo);
                vehicle.setOrientation((float)0.0, (float)0.0, (float)(correctionAngle));
                orientation = vehicle.getOrientation();
                System.out.println("orientation");
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
