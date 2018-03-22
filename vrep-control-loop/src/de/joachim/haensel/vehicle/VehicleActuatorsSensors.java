package de.joachim.haensel.vehicle;

import java.awt.Color;

import coppelia.FloatWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.CarControlInterface;
import de.joachim.haensel.sumo2vrep.Position2D;

public class VehicleActuatorsSensors implements IActuatingSensing
{
    private VehicleHandles _vehicleHandles;
    private Position2D _curPosition;
    private Position2D _rearWheelCenterPosition;
    private double _vehicleLength;
    private CarControlInterface _controlInterface;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    
    public VehicleActuatorsSensors(VehicleHandles vehicleHandles, CarControlInterface controller, VRepRemoteAPI vrep, int clientID)
    {
        _vehicleHandles = vehicleHandles;
        _controlInterface = controller;
        _curPosition = new Position2D(0, 0);
        _rearWheelCenterPosition = new Position2D(0, 0);
        _vehicleLength = -1.0;
        _vrep = vrep;
        _clientID = clientID;
    }

    @Override
    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma)
    {
        FloatWA eulerAngles = new FloatWA(3);
        eulerAngles.getArray()[0] = angleAlpha;
        eulerAngles.getArray()[1] = angleBeta;
        eulerAngles.getArray()[2] = angleGamma;
        try
        {
            _vrep.simxSetObjectOrientation(_clientID, _vehicleHandles.getPhysicalBody(), -1, eulerAngles, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    @Override
    public void computeAndLockSensorData()
    {
        try
        {
            FloatWA physicalBodyPosition = new FloatWA(3);
            _vrep.simxGetObjectPosition(_clientID, _vehicleHandles.getPhysicalBody(), -1, physicalBodyPosition, remoteApi.simx_opmode_blocking);
            _curPosition.setXY(physicalBodyPosition.getArray());
            
            FloatWA rearWheelPosition = new FloatWA(3);
            _vrep.simxGetObjectPosition(_clientID, _vehicleHandles.getRearLeftWheel(), -1, rearWheelPosition, remoteApi.simx_opmode_blocking);
            float xL = rearWheelPosition.getArray()[0];
            float yL = rearWheelPosition.getArray()[1];
            _vrep.simxGetObjectPosition(_clientID, _vehicleHandles.getRearRightWheel(), -1, rearWheelPosition, remoteApi.simx_opmode_blocking);
            float xR = rearWheelPosition.getArray()[0];
            float yR = rearWheelPosition.getArray()[1];
            _rearWheelCenterPosition.setXY((xL + xR) / 2.0, (yL + yR) / 2.0);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }
    
    @Override
    public Position2D getPosition()
    {
        return _curPosition;
    }
    
    @Override
    public Position2D getRearWheelCenterPosition()
    {
        return _rearWheelCenterPosition;
    }

    @Override
    public double getVehicleLength()
    {
        if(_vehicleLength < 0)
        {
            try
            {
                int frontLeftWheelHandle = _vehicleHandles.getFrontLeftWheel();
                int rearLeftWheelHandle = _vehicleHandles.getRearLeftWheel();
                FloatWA frontLeftWheelPos = new FloatWA(3);
                FloatWA rearLeftWheelPos = new FloatWA(3);
                _vrep.simxGetObjectPosition(_clientID, rearLeftWheelHandle, -1, frontLeftWheelPos, remoteApi.simx_opmode_blocking);
                _vrep.simxGetObjectPosition(_clientID, frontLeftWheelHandle, -1, rearLeftWheelPos, remoteApi.simx_opmode_blocking);
                Position2D p1 = new Position2D(rearLeftWheelPos);
                Position2D p2 = new Position2D(frontLeftWheelPos);
                _vehicleLength = Position2D.distance(p1, p2);
                return _vehicleLength;
            }
            catch (VRepException exc)
            {
                exc.printStackTrace();
            }
        }
        else
        {
            return _vehicleLength;
        }
        return 0.0;
    }


    @Override
    public void drive(float targetWheelRotation, float targetSteeringAngle)
    {
        try
        {
            _controlInterface.drive(targetWheelRotation, targetSteeringAngle);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    @Override
    public int drawVector(Vector2D vector, Color color)
    {
        // TODO required in LUA library
        // TODO proceed in LUA library
        // -- number drawingObjectHandle=sim.addDrawingObject(number objectType,number size,number duplicateTolerance,number parentObjectHandle,number maxItemCount,table_3 ambient_diffuse=nil,nil,table_3 specular=nil,table_3 emission=nil)
        // handle = sim.addDrawingObject(sim.drawing_lines, 2, 0.0, -1, 1, nil, nil, color)
        // -- number result=sim.addDrawingObjectItem(number drawingObjectHandle,table itemData)
        // sim.addDrawingObjectItem(handle, {{x1, y1, z1}, {x2, y2, z2}})
        return 0;
    }

    @Override
    public void drawUpdateVector(int handle, Vector2D vector, Color color)
    {
        // TODO required in LUA library
        // TODO proceed in LUA library
        // sim.addDrawingObjectItem(handle, nil)
        // sim.addDrawingObjectItem(handle, "vector")
    }

    @Override
    public void setPosition(float posX, float posY, float posZ)
    {
        FloatWA position = new FloatWA(3);
        position.getArray()[0] = posX;
        position.getArray()[1] = posY;
        position.getArray()[2] = posZ;
        try
        {
            _vrep.simxSetObjectPosition(_clientID, _vehicleHandles.getPhysicalBody(), -1, position, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    @Override
    public Vector2D getOrientation()
    {
        int frontLeftWheelHandle = _vehicleHandles.getFrontLeftWheel();
        int rearLeftWheelHandle = _vehicleHandles.getRearLeftWheel();
        FloatWA frontLeftWheelPos = new FloatWA(3);
        FloatWA rearLeftWheelPos = new FloatWA(3);
        try
        {
            _vrep.simxGetObjectPosition(_clientID, rearLeftWheelHandle, -1, frontLeftWheelPos, remoteApi.simx_opmode_blocking);
            _vrep.simxGetObjectPosition(_clientID, frontLeftWheelHandle, -1, rearLeftWheelPos, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        Position2D p1 = new Position2D(rearLeftWheelPos);
        Position2D p2 = new Position2D(frontLeftWheelPos);
        return new Vector2D(p1, p2);
    }
}
