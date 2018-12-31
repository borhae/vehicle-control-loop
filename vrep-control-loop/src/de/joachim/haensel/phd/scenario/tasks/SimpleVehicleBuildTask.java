package de.joachim.haensel.phd.scenario.tasks;

import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitController;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepLoadModelVehicleFactory;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleConfiguration;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class SimpleVehicleBuildTask implements ITask, IVehicleProvider
{
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;
    private RoadMap _map;
    private Position2D _position;
    private Vector2D _orientation;

    private IVehicle _vehicle;

    public SimpleVehicleBuildTask(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator, RoadMap map, Position2D position, Vector2D orientation)
    {
        _vrep = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
        _map = map;
        _position = position;
        _orientation = orientation;
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
        IVehicleFactory factory = new VRepLoadModelVehicleFactory(_vrep, _clientID, _objectCreator, "./res/simcarmodel/vehicleAllAnglesCleanedUpNoScript.ttm", 1.0f);
        IVehicleConfiguration vehicleConf = createConfiguration(map, vehiclePosition, orientation, 0.3);
        factory.configure(vehicleConf);
        IVehicle vehicle = factory.createVehicleInstance();
        return vehicle;
    }
    
    public IVehicleConfiguration createConfiguration(RoadMap roadMap, Position2D startPosition, Vector2D orientation, double placementHeight)
    {
        IVehicleConfiguration vehicleConf = new VRepVehicleConfiguration();
        IUpperLayerFactory upperFact = () -> {return new DefaultNavigationController(5.0, 60.0);};
        ILowerLayerFactory lowerFact = () -> {
            PurePursuitController ctrl = new PurePursuitController();
            PurePursuitParameters parameters = new PurePursuitParameters(10.0, 0.25);
            parameters.setSpeed(2.5);
            ctrl.setParameters(parameters);
            return ctrl;
        };
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
}
