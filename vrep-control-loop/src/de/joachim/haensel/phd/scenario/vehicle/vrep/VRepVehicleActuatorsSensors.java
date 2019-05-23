package de.joachim.haensel.phd.scenario.vehicle.vrep;


import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Color;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.map.IStreetSection;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.simulator.ISimulatorData;
import de.joachim.haensel.phd.scenario.simulator.RoadMapTracker;
import de.joachim.haensel.phd.scenario.simulator.vrep.VRepSimulatorData;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleHandles;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingObject;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingType;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VRepVehicleActuatorsSensors implements IActuatingSensing, IVrepDrawing
{
    private IVehicleHandles _vehicleHandles;
    private Position2D _curPosition;
    private Position2D _rearWheelCenterPosition;
    private Position2D _frontWheelCenterPosition;
    private double _vehicleLength;
    private String _vehicleScriptParentName;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private Map<String, DrawingObject> _drawingObjectsStore;
    private double[] _vechicleVelocity;
    private VRepObjectCreation _vRepObjectCreator;
    private double _wheelDiameter;
    private float _timeStamp; //current Simulation times
    private RoadMapTracker _mapTracker;
    private RoadMap _roadMap;
    
    public VRepVehicleActuatorsSensors(IVehicleHandles vehicleHandles, ISimulatorData simulatorData, RoadMap roadMap)
    {
        _vehicleHandles = vehicleHandles;
        _curPosition = new Position2D(0, 0);
        _rearWheelCenterPosition = new Position2D(0, 0);
        _frontWheelCenterPosition = new Position2D(0.0, 0.0);
        _vehicleLength = -1.0;
        _drawingObjectsStore = new HashMap<String, DrawingObject>();
        _vechicleVelocity = new double[3];

        VRepSimulatorData vrepData = (VRepSimulatorData)simulatorData;
        _vehicleScriptParentName = vrepData.getVehicleScriptParentName();
        _vrep = vrepData.getVRepRemoteAPI();
        _clientID = vrepData.getClientID();
        _vRepObjectCreator = vrepData.getVRepObjectCreator();
        _wheelDiameter = INVALID_WHEEL_DIAMETER;
        _roadMap = roadMap;
    }

    @Override
    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma)
    {
        internalSetOrientation(angleAlpha, angleBeta, angleGamma);
    }

    private synchronized void internalSetOrientation(float angleAlpha, float angleBeta, float angleGamma)
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
        internalComputeAndLockSensorData();
    }

    private synchronized void internalComputeAndLockSensorData()
    {
        try
        {
            FloatWA returnValF = new FloatWA(10);
            _vrep.simxCallScriptFunction(_clientID, _vehicleScriptParentName, remoteApi.sim_scripttype_childscript, "sense",  null, null, null, null, null, returnValF, null, null, remoteApi.simx_opmode_blocking);
            float[] vals = returnValF.getArray();
            if(vals == null || vals.length == 0 || (Double.isNaN(vals[0]) || Double.isNaN(vals[1])))
            {
                //simulation probably not running
                return;
            }
            _curPosition.setXY(vals[0], vals[1]);
            _frontWheelCenterPosition.setXY(vals[2], vals[3]);
            _rearWheelCenterPosition.setXY(vals[4], vals[5]);
            _vechicleVelocity[0] = vals[6];
            _vechicleVelocity[1] = vals[7];
            _vechicleVelocity[2] = vals[8];
            _timeStamp = vals[9];
            _wheelDiameter = vals[10];
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
        return internalGetVehicleLength();
    }

    private synchronized double internalGetVehicleLength()
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
        System.out.format("v: %.2f, angle: %.2f\n", targetWheelRotation, Math.toDegrees(targetSteeringAngle));
        internalDrive(targetWheelRotation, targetSteeringAngle);
    }

    private synchronized void internalDrive(float targetWheelRotation, float targetSteeringAngle)
    {
        try
        {
            FloatWA inFloats = new FloatWA(2);
            inFloats.getArray()[0] = (float)(double) targetSteeringAngle;
            inFloats.getArray()[1] = (float)(double) targetWheelRotation;
            _vrep.simxCallScriptFunction(_clientID, _vehicleScriptParentName, remoteApi.sim_scripttype_childscript, "control", null, inFloats, null, null, null, null, null, null, remoteApi.simx_opmode_oneshot);
        }
        catch (VRepException exc)
        {
            if(exc.getRetVal() == 1)
            {
                System.out.println("Warning: Input doesn't contain the specified command. Ok when this happens only once (during startup)");
            }
            exc.printStackTrace();
        }
    }

    @Override
    public void registerDrawingObject(String key, DrawingType type, Color lineColor)
    {
        internalRegisterDrawingObject(key, type, lineColor);
    }

    private synchronized void internalRegisterDrawingObject(String key, DrawingType type, Color lineColor)
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
        int[] luaResultArray = luaIntCallResult.getArray();
        if(luaResultArray.length > 0)
        {
            int handle = luaResultArray[0];
            System.out.println("Added drawing object with handle" + handle);
            _drawingObjectsStore.put(key, new DrawingObject(type, handle));
        }
        else
        {
            System.out.println("Seems like we couldn't add a drawing object");
        }
    }
    
    @Override
    public void attachDebugCircle(double lookahead)
    {
        internalAttachDebugCircle(lookahead);
    }

    private synchronized void internalAttachDebugCircle(double lookahead)
    {
        String parentObj = VRepPartwiseVehicleCreator.PHYSICAL_CAR_BODY_NAME;
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

    private synchronized void removeDrawingObject(int handle)
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
        internalUpdateLine(key, vector, zValue);
    }

    private synchronized void internalUpdateLine(String key, Vector2D vector, double zValue)
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
        internalUpdateCircle(key, center, zValue, radius);
    }

    private synchronized void internalUpdateCircle(String key, Position2D center, double zValue, double radius)
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
            fail(exc);
            exc.printStackTrace();
        }
    }
    
    @Override
    public void blowTire(boolean[] tiresToBlow, float tireScale)
    {
        internalBlowTire(tiresToBlow, tireScale);
    }

    private synchronized void internalBlowTire(boolean[] tiresToBlow, float tireScale)
    {
        String parentObj = VRepObjectCreation.VREP_LOADING_SCRIPT_PARENT_OBJECT;
        FloatWA inFloats = new FloatWA(3);
        IntWA inInts = new IntWA(1);
        for(int idx = 0; idx < tiresToBlow.length; idx++)
        {
            if(tiresToBlow[idx])
            {
                switch (idx)
                {
                    case 0:
                        inInts.getArray()[0] = _vehicleHandles.getFrontLeftWheel();
                        break;
                    case 1:
                        inInts.getArray()[0] = _vehicleHandles.getFrontRightWheel();
                        break;
                    case 2:
                        inInts.getArray()[0] = _vehicleHandles.getRearRightWheel();
                        break;
                    case 3:
                        inInts.getArray()[0] = _vehicleHandles.getRearLeftWheel();
                        break;
                    default:
                        break;
                }
            }
            inFloats.getArray()[0] = tireScale;
            inFloats.getArray()[1] = 1.0f;
            inFloats.getArray()[2] = 1.0f;
            try
            {
                _vrep.simxCallScriptFunction(_clientID, parentObj, remoteApi.sim_scripttype_customizationscript, "scaleObject", inInts, inFloats, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
            }
            catch (VRepException exc)
            {
                exc.printStackTrace();
            }
        }
    }

    @Override
    public void setPosition(float posX, float posY, float posZ)
    {
        internalSetPosition(posX, posY, posZ);
    }

    private synchronized void internalSetPosition(float posX, float posY, float posZ)
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
        return internalGetOrientation();
    }

    private synchronized Vector2D internalGetOrientation()
    {
        int frontLeftWheelHandle = _vehicleHandles.getFrontLeftWheel();
        int rearLeftWheelHandle = _vehicleHandles.getRearLeftWheel();
        FloatWA frontLeftWheelPos = new FloatWA(3);
        FloatWA rearLeftWheelPos = new FloatWA(3);
        try
        {
            _vrep.simxGetObjectPosition(_clientID, rearLeftWheelHandle, -1, rearLeftWheelPos, remoteApi.simx_opmode_blocking);
            _vrep.simxGetObjectPosition(_clientID, frontLeftWheelHandle, -1, frontLeftWheelPos, remoteApi.simx_opmode_blocking);
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
    public Vector2D getLockedOrientation()
    {
        return new Vector2D(_rearWheelCenterPosition, _frontWheelCenterPosition);
    }

    @Override
    public Position2D getNonDynamicPosition()
    {
        return internalGetNonDynamicPosition();
    }

    private synchronized Position2D internalGetNonDynamicPosition()
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

    @Override
    public void initialize()
    {
        internalInitialize();
    }

    private synchronized void internalInitialize()
    {
        try
        {
            _vRepObjectCreator.attachControlScript(_vehicleHandles.getPhysicalBody());
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    @Override
    public double getWheelDiameter()
    {
        return _wheelDiameter;
    }

    @Override
    public long getTimeStamp()
    {
        // Vrep gives us seconds with up to 2 digits behind the decimal point. 
        return (long)(_timeStamp * 1000.0f);
    }

    @Override
    public List<IStreetSection> getViewAhead()
    {
        if(_mapTracker == null)
        {
            _mapTracker = _roadMap.createTracker(new Vector2D(_rearWheelCenterPosition, _frontWheelCenterPosition));
        }
        return _mapTracker.getViewAhead(50.0, _curPosition, getLockedOrientation());
    }

    @Override
    public void notifyNewRoute(List<Line2D> route)
    {
    }

    @Override
    public void notifyStartOriginalTrajectory(LinkedList<Vector2D> emptyRoute)
    {
    }

    @Override
    public void notifyStartOverlayTrajectory(Deque<Vector2D> emptyOverlay)
    {
    }

    @Override
    public void updateTrajectory(Vector2D newVector, Deque<Vector2D> updatedList)
    {
    }

    @Override
    public void notifyNewRouteStreetSections(List<IStreetSection> path)
    {
        if(_mapTracker == null)
        {
            IStreetSection firstStreetSection = path.get(0);
            Vector2D startPosition = firstStreetSection.getAPosition();
            _mapTracker = _roadMap.createTracker(startPosition);
        }
        _mapTracker.notifyNewRoute(path);
    }
}
