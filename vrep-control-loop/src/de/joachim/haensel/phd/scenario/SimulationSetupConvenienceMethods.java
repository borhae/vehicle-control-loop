package de.joachim.haensel.phd.scenario;

import java.util.ArrayList;
import java.util.List;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitController;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.PurePursuitParameters;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Navigator;
import de.joachim.haensel.phd.scenario.vehicle.vrep.VRepVehicleConfiguration;
import de.joachim.haensel.phd.scenario.vrep.modelvisuals.MercedesVisualsNames;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class SimulationSetupConvenienceMethods
{
    public static IVehicleConfiguration createMercedesLikeConfiguration(RoadMap roadMap, Position2D startPosition, Position2D targetPosition, double placementHeight)
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
        Navigator nav = new Navigator(roadMap);
        List<Line2D> route = nav.getRoute(startPosition, targetPosition);
        
        Vector2D firstRoadSection = new Vector2D(route.get(0));
        vehicleConf.setOrientation(firstRoadSection);
        vehicleConf.setRoadMap(roadMap);
        
        List<String> autoBodyNames = new ArrayList<>();
        autoBodyNames.add(MercedesVisualsNames.AUTO_BODY_NAME);
        autoBodyNames.add(MercedesVisualsNames.REAR_LEFT_VISUAL);
        autoBodyNames.add(MercedesVisualsNames.REAR_RIGHT_VISUAL);
        autoBodyNames.add(MercedesVisualsNames.FRONT_LEFT_VISUAL);
        autoBodyNames.add(MercedesVisualsNames.FRONT_RIGHT_VISUAL);
        
        vehicleConf.setAutoBodyNames(autoBodyNames );

        return vehicleConf;
    }

    
    public static IVehicleConfiguration createVehicleConfiguration(RoadMap roadMap, Position2D startPosition, double height)
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
        vehicleConf.setPosition(startingPoint.getX(), startingPoint.getY(), height);

        Vector2D firstRoadSection = new Vector2D(roadMap.getClosestLineFor(startPosition));
        vehicleConf.setOrientation(firstRoadSection);
        vehicleConf.setRoadMap(roadMap);
        return vehicleConf;
    }
    
    public static RoadMap createMap(int clientID, VRepRemoteAPI vrep, VRepObjectCreation objectCreator, String mapFileName) throws VRepException
    {
        RoadMap roadMap = new RoadMap(mapFileName);
        roadMap.center(0.0, 0.0);
        
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;
        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, vrep, clientID, objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
        
        return roadMap;
    }

    
    public static RoadMapAndCenterMatrix createCenteredMap(int clientID, VRepRemoteAPI vrep, VRepObjectCreation objectCreator, String mapFileName) throws VRepException
    {
        RoadMap roadMap = new RoadMap(mapFileName);
        TMatrix centerMatrix = roadMap.center(0.0, 0.0);
        
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;
        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, vrep, clientID, objectCreator);
        mapCreator.createMeshBasedMap(roadMap);
        mapCreator.createMapSizedRectangle(roadMap, false);
        
        return new RoadMapAndCenterMatrix(roadMap, centerMatrix);
    }
}
