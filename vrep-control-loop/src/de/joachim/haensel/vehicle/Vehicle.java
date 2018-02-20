package de.joachim.haensel.vehicle;

import java.awt.geom.Point2D;
import java.util.Timer;

import coppelia.FloatWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vehiclecontrol.base.Position2D;
import de.joachim.haensel.vehiclecontrol.reactive.CarControlInterface;
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
    
    public Vehicle(VRepObjectCreation creator, VRepRemoteAPI vrep, int clientID, VehicleHandles vehicleHandles, CarControlInterface controller)
    {
        _vrep = vrep;
        _clientID = clientID;
        _vehicleHandles = vehicleHandles;
        _controlInterface = controller;
        
        
        _lowerControlLayer = new BadReactiveController(this);
        _upperControlLayer = new NavigationController(_lowerControlLayer);

        _controlEventGenerator = new LowLevelEventGenerator();
        _controlEventGenerator.addEventListener(_lowerControlLayer);
        _timer = new Timer();
        _curPosition = new Position2D(0, 0);
        _rearWheelCenterPosition = new Position2D(0, 0);
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

    public void driveTo(float x, float y)
    {
        _upperControlLayer.driveTo(new Position2D(x, y));
    }
    
    public void start()
    {
        _timer.scheduleAtFixedRate(_controlEventGenerator, 0, CONTROL_LOOP_EXECUTION_DENSITY);
    }

    public void driveToBlocking(float x, float y)
    {
        _upperControlLayer.driveToBlocking(new Position2D(x, y));
    }

    @Override
    public Position2D getPosition()
    {
        try
        {
            FloatWA position3D = new FloatWA(3);
            _vrep.simxGetObjectPosition(_clientID, _vehicleHandles.getPhysicalBody(), -1, position3D, remoteApi.simx_opmode_blocking);
            _curPosition.setXY(position3D.getArray());
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        return _curPosition;
    }
    
    @Override
    public Position2D getRearWheelCenterPosition()
    {
        try
        {
            FloatWA position3D = new FloatWA(3);
            _vrep.simxGetObjectPosition(_clientID, _vehicleHandles.getRearLeftWheel(), -1, position3D, remoteApi.simx_opmode_blocking);
            float xL = position3D.getArray()[0];
            float yL = position3D.getArray()[1];
            _vrep.simxGetObjectPosition(_clientID, _vehicleHandles.getRearLeftWheel(), -1, position3D, remoteApi.simx_opmode_blocking);
            float xR= position3D.getArray()[0];
            float yR = position3D.getArray()[1];
            _rearWheelCenterPosition.setXY(xL - xR, yL -yR);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
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
        float junctionX = junction.getX();
        float junctionY = junction.getY();
        
        Position2D[] lineCoordinates = Position2D.valueOf(laneToHeadFor.getShape().split(" "));
        double minDist = Float.MAX_VALUE;
        int minIdx = -1;
        for (int idx = 0; idx < lineCoordinates.length; idx++)
        {
            Position2D curCoordinate = lineCoordinates[idx];
            double curDist = Point2D.distance(curCoordinate.getX(), curCoordinate.getY(), junctionX, junctionY);
            if(curDist < minDist)
            {
               minDist = curDist;
               minIdx = idx;
            }
        }
        Position2D startPos = lineCoordinates[minIdx];
        setPosition(startPos.getX(), startPos.getY(), 0.3f);
        // defined by two points
        if(lineCoordinates.length == 2)
        {
            int otherIdx = minIdx == 0 ? 1 : 0;
            Position2D destPos = lineCoordinates[otherIdx];
            double angle = Math.atan2(startPos.getY() - destPos.getY(), startPos.getX() - destPos.getX()) + Math.PI / 2;
            setOrientation(0.0f, 0.0f, (float)angle);
        }
    }
}
