package de.joachim.haensel.vehicle;

import java.awt.Color;
import java.util.Timer;

import coppelia.FloatWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.CarControlInterface;
import de.joachim.haensel.sumo2vrep.OrientedPosition;
import de.joachim.haensel.sumo2vrep.Position2D;
import de.joachim.haensel.sumo2vrep.RoadMap;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class Vehicle implements IActuatingSensing
{
    private static final int CONTROL_LOOP_EXECUTION_DENSITY = 100; //milliseconds
    private VehicleHandles _vehicleHandles;
    private CarControlInterface _controlInterface;
    private VRepRemoteAPI _vrep;
    private int _clientID;

    private ITopLayerControl _upperControlLayer;
    private ILowLevelController _lowerControlLayer;
    private LowLevelEventGenerator _controlEventGenerator;
    private Timer _timer;
    private Position2D _curPosition;
    private Position2D _rearWheelCenterPosition;
    private RoadMap _roadMap;
    private double _vehicleLength;
    
//        _upperControlLayer = new NavigationController(this, roadMap);
//        _lowerControlLayer = new BadReactiveController(this, _upperControlLayer);
    public Vehicle(VRepObjectCreation creator, VRepRemoteAPI vrep, int clientID, VehicleHandles vehicleHandles, CarControlInterface controller, RoadMap roadMap, IUpperLayerFactory upperLayerFactory, ILowerLayerFactory lowerLayerFactory)
    {
        _vrep = vrep;
        _clientID = clientID;
        _vehicleHandles = vehicleHandles;
        _controlInterface = controller;
        _upperControlLayer = upperLayerFactory.create();
        _upperControlLayer.initController(this, roadMap);
        _lowerControlLayer = lowerLayerFactory.create();
        _lowerControlLayer.initController(this, _upperControlLayer);
        
        _controlEventGenerator = new LowLevelEventGenerator();
        _controlEventGenerator.addEventListener(_lowerControlLayer);
        _timer = new Timer();
        _curPosition = new Position2D(0, 0);
        _rearWheelCenterPosition = new Position2D(0, 0);
        _roadMap = roadMap;
        _vehicleLength = -1.0;
    }

    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma) throws VRepException
    {
        FloatWA eulerAngles = new FloatWA(3);
        eulerAngles.getArray()[0] = angleAlpha;
        eulerAngles.getArray()[1] = angleBeta;
        eulerAngles.getArray()[2] = angleGamma;
        _vrep.simxSetObjectOrientation(_clientID, _vehicleHandles.getPhysicalBody(), -1, eulerAngles, remoteApi.simx_opmode_blocking);
    }

    public void setPosition(float posX, float posY, float posZ) throws VRepException
    {
        FloatWA position = new FloatWA(3);
        position.getArray()[0] = posX;
        position.getArray()[1] = posY;
        position.getArray()[2] = posZ;
        _vrep.simxSetObjectPosition(_clientID, _vehicleHandles.getPhysicalBody(), -1, position, remoteApi.simx_opmode_blocking);
    }

    public void start()
    {
        _timer.scheduleAtFixedRate(_controlEventGenerator, 0, CONTROL_LOOP_EXECUTION_DENSITY);
    }
    
    public void driveTo(float x, float y, RoadMap roadMap)
    {
        _roadMap = roadMap;
        Position2D targetPosition = new Position2D(x, y);
        _upperControlLayer.buildSegmentBuffer(targetPosition, roadMap);
        _lowerControlLayer.driveTo(targetPosition);
    }

    public void driveToBlocking(float x, float y, RoadMap roadMap)
    {
        //TODO nothings blocking here yet. Take care if the rest is done
        driveTo(x, y, roadMap);
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

    public void stop()
    {
        _timer.cancel();
    }

    public void putOnJunctionHeadingTo(JunctionType junction, LaneType laneToHeadFor) throws VRepException
    {
        OrientedPosition posAndHeading = _roadMap.computeLaneEntryAtJunction(junction, laneToHeadFor);
        setOrientation(0.0f, 0.0f, (float)posAndHeading.getAngle());
        setPosition((float)posAndHeading.getPos().getX(), (float)posAndHeading.getPos().getY(), 0.3f);
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

    public Vector2D getOrientation() throws VRepException
    {
        int frontLeftWheelHandle = _vehicleHandles.getFrontLeftWheel();
        int rearLeftWheelHandle = _vehicleHandles.getRearLeftWheel();
        FloatWA frontLeftWheelPos = new FloatWA(3);
        FloatWA rearLeftWheelPos = new FloatWA(3);
        _vrep.simxGetObjectPosition(_clientID, rearLeftWheelHandle, -1, frontLeftWheelPos, remoteApi.simx_opmode_blocking);
        _vrep.simxGetObjectPosition(_clientID, frontLeftWheelHandle, -1, rearLeftWheelPos, remoteApi.simx_opmode_blocking);
        Position2D p1 = new Position2D(rearLeftWheelPos);
        Position2D p2 = new Position2D(frontLeftWheelPos);
        return new Vector2D(p1, p2);
    }

    @Override
    public int drawVector(Vector2D vector, Color color)
    {
        
        return 0;
    }

    @Override
    public void drawUpdateVector(int handle, Vector2D vector, Color color)
    {
        
    }
}
