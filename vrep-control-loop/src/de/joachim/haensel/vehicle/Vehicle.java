package de.joachim.haensel.vehicle;

import java.util.Timer;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.OrientedPosition;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.CarControlInterface;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class Vehicle 
{
    private static final int CONTROL_LOOP_EXECUTION_DENSITY = 100; //milliseconds

    private IUpperLayerControl _upperControlLayer;
    private ILowerLayerControl _lowerControlLayer;
    private LowLevelEventGenerator _controlEventGenerator;
    private Timer _timer;
    private RoadMap _roadMap;
    private IActuatingSensing _actuatingSensing;

    private VehicleHandles _vehicleHandles;
    private CarControlInterface _controller;

    public Vehicle(VRepObjectCreation creator, VRepRemoteAPI vrep, int clientID, VehicleHandles vehicleHandles, CarControlInterface controller, RoadMap roadMap, IUpperLayerFactory upperLayerFactory, ILowerLayerFactory lowerLayerFactory)
    {
        _vehicleHandles = vehicleHandles;
        _controller = controller;
        _upperControlLayer = upperLayerFactory.create();
        _actuatingSensing = new VehicleActuatorsSensors(vehicleHandles, controller, vrep, clientID);
        _upperControlLayer.initController(_actuatingSensing, roadMap);
        _lowerControlLayer = lowerLayerFactory.create();
        _lowerControlLayer.initController(_actuatingSensing, _upperControlLayer);
        
        _controlEventGenerator = new LowLevelEventGenerator();
        _controlEventGenerator.addEventListener(_lowerControlLayer);
        _timer = new Timer();
        _roadMap = roadMap;
    }

    public void activateDebugging(DebugParams params)
    {
        _lowerControlLayer.activateDebugging((IVrepDrawing)_actuatingSensing, params);
    }
    
    public void deacvtivateDebugging()
    {
        _lowerControlLayer.deactivateDebugging();
    }

    public CarControlInterface getController()
    {
        return _controller;
    }

    public VehicleHandles getVehicleHandles()
    {
        return _vehicleHandles;
    }

    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma) throws VRepException
    {
        _actuatingSensing.setOrientation(angleAlpha, angleBeta, angleGamma);
    }

    public void setPosition(float posX, float posY, float posZ) throws VRepException
    {
        _actuatingSensing.setPosition(posX, posY, posZ);
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

    public void stop()
    {
        _timer.cancel();
        _lowerControlLayer.stop();
    }

    public void putOnJunctionHeadingTo(JunctionType junction, LaneType laneToHeadFor) throws VRepException
    {
        OrientedPosition posAndHeading = _roadMap.computeLaneEntryAtJunction(junction, laneToHeadFor);
        setOrientation(0.0f, 0.0f, (float)posAndHeading.getAngle());
        setPosition((float)posAndHeading.getPos().getX(), (float)posAndHeading.getPos().getY(), 0.3f);
    }

    public Vector2D getOrientation() throws VRepException
    {
        return _actuatingSensing.getOrientation();
    }

    public double getBetweenFrontRearWheelsLength()
    {
        return _actuatingSensing.getVehicleLength();
    }
}
