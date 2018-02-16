package de.joachim.haensel.vehicle;

import java.util.Timer;

import coppelia.FloatWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vehiclecontrol.reactive.CarControlInterface;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class Vehicle implements IActuatingSensing
{
    private static final int CONTROL_LOOP_EXECUTION_DENSITY = 10; //milliseconds
    private int _physicalBodyHandle;
    private CarControlInterface _controlInterface;
    private VRepRemoteAPI _vrep;
    private int _clientID;

    private ITopLayerControl _upperControlLayer;
    private ILowLevelController _lowerControlLayer;
    private LowLevelEventGenerator _controlEventGenerator;
    private Timer _timer;
    
    public Vehicle(VRepObjectCreation creator, VRepRemoteAPI vrep, int clientID, int physicalBodyHandle, CarControlInterface controller)
    {
        _vrep = vrep;
        _clientID = clientID;
        _physicalBodyHandle = physicalBodyHandle;
        _controlInterface = controller;
        
        _upperControlLayer = new NavigationController();
        
        _lowerControlLayer = new DefaultReactiveController();

        _controlEventGenerator = new LowLevelEventGenerator(this);
        _controlEventGenerator.addEventListener(_lowerControlLayer);
        _timer = new Timer();
    }

    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma) throws VRepException
    {
        FloatWA eulerAngles = new FloatWA(3);
        eulerAngles.getArray()[0] = angleAlpha;
        eulerAngles.getArray()[1] = angleBeta;
        eulerAngles.getArray()[2] = angleGamma;
        _vrep.simxSetObjectOrientation(_clientID, _physicalBodyHandle, -1, eulerAngles, remoteApi.simx_opmode_blocking);
    }

    public void setPosition(float posX, float posY, float posZ) throws VRepException
    {
        FloatWA position = new FloatWA(3);
        position.getArray()[0] = posX;
        position.getArray()[1] = posY;
        position.getArray()[2] = posZ;
        _vrep.simxSetObjectPosition(_clientID, _physicalBodyHandle, -1, position, remoteApi.simx_opmode_blocking);
    }

    public void driveTo(float x, float y)
    {
        _lowerControlLayer.driveTo(x, y);
    }
    
    public void start()
    {
        _timer.scheduleAtFixedRate(_controlEventGenerator, 0, CONTROL_LOOP_EXECUTION_DENSITY);
    }

    public void driveToBlocking(float x, float y)
    {
        _lowerControlLayer.driveToBlocking(x, y);
    }
}
