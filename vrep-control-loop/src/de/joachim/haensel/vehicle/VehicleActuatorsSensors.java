package de.joachim.haensel.vehicle;

import static org.junit.Assert.fail;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleHandles;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.CarControlInterface;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingObject;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingType;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VehicleActuatorsSensors implements IActuatingSensing, IVrepDrawing
{
    private IVehicleHandles _vehicleHandles;
    private Position2D _curPosition;
    private Position2D _rearWheelCenterPosition;
    private Position2D _frontWheelCenterPosition;
    private double _vehicleLength;
    private CarControlInterface _controlInterface;
    private String _vehicleScriptParentName;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private Map<String, DrawingObject> _drawingObjectsStore;
    private double[] _vechicleVelocity;
    
    public VehicleActuatorsSensors(IVehicleHandles vehicleHandles, CarControlInterface controller, VRepRemoteAPI vrep, int clientID)
    {
        _vehicleHandles = vehicleHandles;
        _controlInterface = controller;
        _vehicleScriptParentName = controller.getVehicleScriptParentName();
        _curPosition = new Position2D(0, 0);
        _rearWheelCenterPosition = new Position2D(0, 0);
        _frontWheelCenterPosition = new Position2D(0.0, 0.0);
        _vehicleLength = -1.0;
        _vrep = vrep;
        _clientID = clientID;
        _drawingObjectsStore = new HashMap<String, DrawingObject>();
        _vechicleVelocity = new double[3];
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
            FloatWA returnValF = new FloatWA(9);
            _vrep.simxCallScriptFunction(_clientID, _vehicleScriptParentName, remoteApi.sim_scripttype_childscript, "sense", 
                    null, null, null, null, null, returnValF, null, null, remoteApi.simx_opmode_blocking);
            float[] vals = returnValF.getArray();
            _curPosition.setXY(vals[0], vals[1]);
            _frontWheelCenterPosition.setXY(vals[2], vals[3]);
            _rearWheelCenterPosition.setXY(vals[4], vals[5]);
            _vechicleVelocity[0] = vals[6];
            _vechicleVelocity[1] = vals[7];
            _vechicleVelocity[2] = vals[8];
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
    public Position2D getFrontWheelCenterPosition()
    {
        return _frontWheelCenterPosition;
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
    public void registerDrawingObject(String key, DrawingType type, Color lineColor)
    {
        String parentObj = VRepObjectCreation.VREP_LOADING_SCRIPT_PARENT_OBJECT;
        IntWA luaIntCallResult = new IntWA(1);
        FloatWA colorParamsLua = new FloatWA(3);
        float[] colorParams = colorParamsLua.getArray();
        colorParams[0] = lineColor.getRed() / 255.0f;
        colorParams[1] = lineColor.getGreen() / 255.0f;
        colorParams[2] = lineColor.getBlue() / 255.0f;
        try
        {
            switch (type)
            {
                case LINE:
                    _vrep.simxCallScriptFunction(_clientID, parentObj, remoteApi.sim_scripttype_customizationscript, "createDrawingObjectLine", null, colorParamsLua, null, null, luaIntCallResult, null, null, null, remoteApi.simx_opmode_blocking);
                    break;
                case CIRCLE:
                    _vrep.simxCallScriptFunction(_clientID, parentObj, remoteApi.sim_scripttype_customizationscript, "createDrawingObjectCircle", null, colorParamsLua, null, null, luaIntCallResult, null, null, null, remoteApi.simx_opmode_blocking);
                    break;
                default:
                    break;
            }
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        int handle = luaIntCallResult.getArray()[0];
        System.out.println("Added drawing object with handle" + handle);
        _drawingObjectsStore.put(key, new DrawingObject(type, handle));
    }
    
    @Override
    public void attachDebugCircle(double lookahead)
    {
        String parentObj = VehicleCreator.PHYSICAL_CAR_BODY_NAME;
        Color lineColor = Color.ORANGE;
        FloatWA floatParamsIn = new FloatWA(4);
        float[] floatsIn = floatParamsIn.getArray();
        floatsIn[0] = (float) lookahead;
        floatsIn[1] = lineColor.getRed() / 255.0f;
        floatsIn[2] = lineColor.getGreen() / 255.0f;
        floatsIn[3] = lineColor.getBlue() / 255.0f;

        IntWA intParamsIn = new IntWA(1);
        intParamsIn.getArray()[0] = _vehicleHandles.getRearWheelDummy();
        try
        {
            _vrep.simxCallScriptFunction(_clientID, parentObj, remoteApi.sim_scripttype_childscript, "debugCircle", 
                    intParamsIn, floatParamsIn, null, null, 
                    null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    @Override
    public void removeAllDrawigObjects()
    {
        _drawingObjectsStore.forEach((k, v) -> removeDrawingObject(v.getHandle()));
    }

    private void removeDrawingObject(int handle)
    {
        String parentObj = VRepObjectCreation.VREP_LOADING_SCRIPT_PARENT_OBJECT;
        IntWA inHandle = new IntWA(1);
        inHandle.getArray()[0] = handle;
        try
        {
            _vrep.simxCallScriptFunction(_clientID, parentObj, remoteApi.sim_scripttype_customizationscript, "removeDrawingObject", inHandle, null, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    @Override
    public void updateLine(String key, Vector2D vector, double zValue, Color color)
    {
        int handle = _drawingObjectsStore.get(key).getHandle();
        FloatWA callParamsF = new FloatWA(6);
        float[] floatParamsArray = callParamsF.getArray();
        floatParamsArray[0] = (float) vector.getBase().getX();
        floatParamsArray[1] = (float) vector.getBase().getY();
        floatParamsArray[2] = (float) zValue;
        floatParamsArray[3] = (float) vector.getTip().getX();
        floatParamsArray[4] = (float) vector.getTip().getY();
        floatParamsArray[5] = (float) zValue;
        String parentObj = VRepObjectCreation.VREP_LOADING_SCRIPT_PARENT_OBJECT;
        IntWA inHandle = new IntWA(1);
        inHandle.getArray()[0] = handle;
        try
        {
            _vrep.simxCallScriptFunction(_clientID, parentObj, remoteApi.sim_scripttype_customizationscript, "redrawLine", inHandle, callParamsF, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    @Override
    public void updateCircle(String key, Position2D center, double zValue, double radius, Color color)
    {
        int handle = _drawingObjectsStore.get(key).getHandle();
        FloatWA callParamsF = new FloatWA(6);
        float[] floatParamsArray = callParamsF.getArray();
        floatParamsArray[0] = (float) center.getX();
        floatParamsArray[1] = (float) center.getY();
        floatParamsArray[2] = (float) zValue;
        floatParamsArray[3] = (float) radius;
        try
        {
            String parentObj = VRepObjectCreation.VREP_LOADING_SCRIPT_PARENT_OBJECT;
            IntWA inHandle = new IntWA(1);
            inHandle.getArray()[0] = handle;
            _vrep.simxCallScriptFunction(_clientID, parentObj, remoteApi.sim_scripttype_customizationscript, "redrawCircle", inHandle, callParamsF, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            fail();
            exc.printStackTrace();
        }
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

    @Override
    public Position2D getNonDynamicPosition()
    {
        try
        {
            FloatWA pos3d = new FloatWA(3);
            _vrep.simxGetObjectPosition(_clientID, _vehicleHandles.getPhysicalBody(), -1, pos3d, remoteApi.simx_opmode_blocking);
            Position2D result = new Position2D(pos3d);
            return result;
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        return new Position2D(0.0, 0.0);
    }

    @Override
    public double[] getVehicleVelocity()
    {
        return _vechicleVelocity;
    }
}
