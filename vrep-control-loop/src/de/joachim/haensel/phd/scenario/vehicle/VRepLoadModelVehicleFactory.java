package de.joachim.haensel.phd.scenario.vehicle;

import java.nio.file.Paths;

import coppelia.IntW;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.CarControlInterface;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VRepLoadModelVehicleFactory implements IVehicleFactory
{
    private static final String PHYSICAL_CAR_BODY_NAME = "physicalCarBody";
    private IVehicleConfiguration _vehicleConf;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;
    private float _scale;

    public VRepLoadModelVehicleFactory(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator, float scale)
    {
        _vrep = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
        _scale = scale;
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
            _vrep.simxLoadModel(_clientID, Paths.get("./res/simcarmodel/vehicle.ttm").toAbsolutePath().toString(), 0, baseHandle, remoteApi.simx_opmode_blocking);
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
            
            handles.setFrontLeftWheel(getHandle("wheelRespondableFrontLeft"));
            handles.setFrontRightWheel(getHandle("wheelRespondableFrontRight"));
            handles.setRearLeftWheel(getHandle("wheelRespondableRearLeft"));
            handles.setRearRightWheel(getHandle("wheelRespondableRearRight"));
            
            handles.setMotorFrontLeft(getHandle("motorFrontLeft"));
            handles.setMotorFrontRight(getHandle("motorFrontRight"));
            
            handles.setPhysicalBody(getHandle(PHYSICAL_CAR_BODY_NAME));
            
            handles.setSteeringFrontLeft(getHandle("steeringFrontLeft"));
            handles.setSteeringFrontRight(getHandle("steeringFrontRight"));
            
            handles.setRearWheelVisualizationDummy(getHandle("lineSegmentRear"));
            handles.setFrontLeftWheelDummy(getHandle("lineSegmentFrontLeft"));
            handles.setFrontRightWheelDummy(getHandle("lineSegmentFrontRight"));
            
            handles.setCamera(getHandle("autoFittingCamera"));
            
            CarControlInterface car = new CarControlInterface(_objectCreator, PHYSICAL_CAR_BODY_NAME, _vrep, _clientID, handles.getPhysicalBody());
            car.initialize();
            
            Vehicle vehicle = new Vehicle(_objectCreator, _vrep, _clientID, handles, car, _vehicleConf.getMap(), _vehicleConf.getUpperCtrlFactory(), _vehicleConf.getLowerCtrlFactory());
            vehicle.setPosition((float)_vehicleConf.getXPos(), (float)_vehicleConf.getYPos(), (float)_vehicleConf.getZPos());

            Vector2D orientationToAlignTo = _vehicleConf.getOrientation();
            Vector2D orientation = vehicle.getOrientation();
            double correctionAngle = Vector2D.computeAngle(orientation, orientationToAlignTo);
            vehicle.setOrientation((float)0.0, (float)correctionAngle, (float)0.0);
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
