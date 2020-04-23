package de.joachim.haensel.phd.scenario.tasks;

import java.util.ArrayList;
import java.util.List;

import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitVariableLookaheadController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepLoadModelVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vrep.modelvisuals.MercedesVisualsNames;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VehicleBuildTask implements ITask, IVehicleProvider
{
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;
    private RoadMap _map;
    private Position2D _position;
    private Vector2D _orientation;

    private IVehicle _vehicle;
    private double _maxVelocity;
    private double _maxLongitudinalAcceleration;
    private double _maxLongitudinalDecceleration;
    private double _maxLateralAcceleration;

    private ILowerLayerFactory _lowerLayerFactory;

    private String _carmodel;

    private IUpperLayerFactory _upperLayerFactory;
    private int _controlLoopRate; //in milliseconds

    public VehicleBuildTask(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator, RoadMap map, Position2D position, Vector2D orientation, String carmodel)
    {
        _vrep = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
        _map = map;
        _position = position;
        _orientation = orientation;
        _carmodel = carmodel;
        _controlLoopRate = 200; //milliseconds. So 200 means 5 times per second
    }

    public void setControlParams(double maxVelocity, double maxLongitudinalAcceleration, double maxLongitudinalDecceleration, double maxLateralAcceleration)
    {
        _maxVelocity = maxVelocity;
        _maxLongitudinalAcceleration = maxLongitudinalAcceleration;
        _maxLongitudinalDecceleration = maxLongitudinalDecceleration;
        _maxLateralAcceleration = maxLateralAcceleration;
    }
    
    public void setControlLoopRate(int controlLoopRate)
    {
        _controlLoopRate = controlLoopRate;
    }

    @Override
    public void execute()
    {
        _vehicle = createVehicle(_map, _position, _orientation);
    }

    @Override
    public int getTimeout()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    private IVehicle createVehicle(RoadMap map, Position2D vehiclePosition, Vector2D orientation)
    {
        IVehicleConfiguration vehicleConf = null;
        if(_carmodel == null)
        {
            _carmodel = "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm";
            vehicleConf = createVehicleConfiguration(map, vehiclePosition, orientation, 1.5);
        }
        else if (_carmodel.contains("isuals"))
        {
            vehicleConf = createMercedesLikeConfiguration(map, vehiclePosition, orientation, 1.5);
        }
        else
        {
            vehicleConf = createVehicleConfiguration(map, vehiclePosition, orientation, 1.5);
        }
        vehicleConf.setControlLoopRate(_controlLoopRate);
        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, _carmodel);
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();
        return vehicle;
    }
    
    private IVehicleConfiguration createMercedesLikeConfiguration(RoadMap roadMap, Position2D startPosition, Vector2D orientation, double placementHeight)
    {
        IVehicleConfiguration vehicleConf = createVehicleConfiguration(roadMap, startPosition, orientation, placementHeight);
        
        List<String> autoBodyNames = new ArrayList<>();
        autoBodyNames.add(MercedesVisualsNames.AUTO_BODY_NAME);
        autoBodyNames.add(MercedesVisualsNames.REAR_LEFT_VISUAL);
        autoBodyNames.add(MercedesVisualsNames.REAR_RIGHT_VISUAL);
        autoBodyNames.add(MercedesVisualsNames.FRONT_LEFT_VISUAL);
        autoBodyNames.add(MercedesVisualsNames.FRONT_RIGHT_VISUAL);
        
        vehicleConf.setAutoBodyNames(autoBodyNames);

        return vehicleConf;
    }

    private IVehicleConfiguration createVehicleConfiguration(RoadMap roadMap, Position2D startPosition, Vector2D orientation, double placementHeight)
    {
        IVehicleConfiguration vehicleConf = new VRepVehicleConfiguration();
        IUpperLayerFactory upperFact;
        if(_upperLayerFactory != null)
        {
            upperFact = _upperLayerFactory;
        }
        else
        {
            upperFact = () -> 
            {
                return new DefaultNavigationController(5.0, UnitConverter.kilometersPerHourToMetersPerSecond(_maxVelocity), _maxLongitudinalAcceleration, _maxLongitudinalDecceleration, _maxLateralAcceleration);
            };
        }
        ILowerLayerFactory lowerFact = null;
        if(_lowerLayerFactory != null)
        {
            lowerFact = _lowerLayerFactory;
        }
        else
        {
            lowerFact = () -> new PurePursuitVariableLookaheadController();
        }
        vehicleConf.setUpperCtrlFactory(upperFact);
        vehicleConf.setLowerCtrlFactory(lowerFact);
        
        Position2D startingPoint = roadMap.getClosestPointOnMap(startPosition);
        vehicleConf.setPosition(startingPoint.getX(), startingPoint.getY(), placementHeight);

        vehicleConf.setOrientation(orientation);
        vehicleConf.setRoadMap(roadMap);
        return vehicleConf;
    }

    @Override
    public IVehicle getVehicle()
    {
        return _vehicle;
    }

    public void setLowerLayerFactory(ILowerLayerFactory lowerLayerFactory)
    {
        _lowerLayerFactory = lowerLayerFactory;
    }

    public void setUpperLayerFactory(IUpperLayerFactory upperLayerFactory)
    {
        _upperLayerFactory = upperLayerFactory;
    }
}
