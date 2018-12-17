package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;
import java.util.Timer;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.simulator.ISimulatorData;
import de.joachim.haensel.phd.scenario.sumo2vrep.OrientedPosition;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleActuatorsSensors;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;
import sumobindings.JunctionType;
import sumobindings.LaneType;

public class Vehicle implements IVehicle 
{
    private static final int CONTROL_LOOP_EXECUTION_FREQUENCY = 100; //milliseconds

    private IUpperLayerControl _upperControlLayer;
    private ILowerLayerControl _lowerControlLayer;
    private LowLevelEventGenerator _controlEventGenerator;
    private Timer _timer;
    private RoadMap _roadMap;
    private IActuatingSensing _actuatingSensing;

    private IVehicleHandles _vehicleHandles;

    private ISimulatorData _simulatorData;

    private boolean _stopped;


    public Vehicle(ISimulatorData simulatorData, IVehicleHandles vehicleHandles, RoadMap roadMap, IUpperLayerFactory upperLayerFactory, ILowerLayerFactory lowerLayerFactory)
    {
        _simulatorData = simulatorData;
        _vehicleHandles = vehicleHandles;
        _upperControlLayer = upperLayerFactory.create();
        _actuatingSensing = new VRepVehicleActuatorsSensors(vehicleHandles, simulatorData);
        _actuatingSensing.initialize();
        _upperControlLayer.initController(_actuatingSensing, roadMap);
        _lowerControlLayer = lowerLayerFactory.create();
        _lowerControlLayer.initController(_actuatingSensing, _upperControlLayer);
        
        _controlEventGenerator = new LowLevelEventGenerator();
        _controlEventGenerator.addEventListener(_lowerControlLayer);
        _timer = new Timer();
        _roadMap = roadMap;
        _stopped = true;
    }

    @Override
    public void addLowLevelEventGeneratorListener(ILowerLayerControl listener)
    {
        listener.initController(_actuatingSensing, _upperControlLayer);
        _controlEventGenerator.addEventListener(listener);
    }
    
    public void activateDebugging(DebugParams params)
    {
        _lowerControlLayer.activateDebugging((IVrepDrawing)_actuatingSensing, params);
        _upperControlLayer.activateDebugging(params);
    }
    
    public void deacvtivateDebugging()
    {
        _lowerControlLayer.deactivateDebugging();
        _upperControlLayer.deactivateDebugging();
    }

    public IVehicleHandles getVehicleHandles()
    {
        return _vehicleHandles;
    }

    public void setOrientation(float angleAlpha, float angleBeta, float angleGamma) throws VRepException
    {
        _actuatingSensing.setOrientation(angleAlpha, angleBeta, angleGamma);
    }

    @Override
    public void setPosition(double posX, double posY, double posZ) throws VRepException
    {
        _actuatingSensing.setPosition((float)posX, (float)posY, (float)posZ);
    }
    
    public void start()
    {
        if(_stopped)
        {
            _timer.scheduleAtFixedRate(_controlEventGenerator, 0, CONTROL_LOOP_EXECUTION_FREQUENCY);
            _stopped = false;
        }
    }
    
    public void driveTo(double x, double y, RoadMap roadMap)
    {
        _lowerControlLayer.stop();
        try
        {
            Thread.sleep(500); // give the lower layer time to actually stop
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        } 
        _roadMap = roadMap;
        Position2D targetPosition = new Position2D(x, y);
        _upperControlLayer.buildSegmentBuffer(targetPosition, roadMap);
        _lowerControlLayer.clearSegmentBuffer();
        List<ILowerLayerControl> listeners = _controlEventGenerator.getListeners();
        listeners.stream().forEach(l -> l.driveTo(targetPosition));
    }

    @Override
    public void driveTo(double x, double y, RoadMap map, IArrivedListener arrivedListener)
    {
        _lowerControlLayer.clearArrivedListeners();
        _lowerControlLayer.addArrivedListener(arrivedListener);
        driveTo(x, y, map);
    }

    public void stop()
    {
        if(!_stopped)
        {
            _timer.cancel();
            _lowerControlLayer.stop();
        }
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

    @Override
    public void removeFromSimulation()
    {
        _simulatorData.removeItemsFromSimulation(_vehicleHandles);
    }
}