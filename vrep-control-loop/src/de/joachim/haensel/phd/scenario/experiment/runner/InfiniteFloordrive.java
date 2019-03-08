package de.joachim.haensel.phd.scenario.experimentrunner;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.map.sumo2vrep.VRepMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;

public class InfiniteFloordrive
{
    public static void main(String[] args)
    {
        InfiniteFloordrive obj = new InfiniteFloordrive();
        try
        {
            obj.run();
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    private void run() throws VRepException
    {
        VRepRemoteAPI vrep = VRepRemoteAPI.INSTANCE;
        int clientID = vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        VRepObjectCreation objectCreator = new VRepObjectCreation(vrep, clientID);
        ShapeParameters params = new ShapeParameters();
        params.setMass(10);
        params.setName("Hugo");
        params.setOrientation(0.0f, 0.0f, 0.0f);
        params.setPosition(0.0f, 0.0f, 0.0f);
        params.setType(EVRepShapes.CUBOID);
        params.setVisibility(true);
        params.setSize(10.0f, 10.0f, 10.0f);
        objectCreator.createPrimitive(params);
  
        RoadMap roadMap = new RoadMap("neumarkRealWorldJustCars.net.xml");
        TMatrix centerMatrix = roadMap.center(0.0, 0.0);
        
        float streetWidth = (float)1.5;
        float streetHeight = (float)0.4;
//        VRepMap mapCreator = new VRepMap(streetWidth, streetHeight, vrep, clientID, objectCreator);
//        mapCreator.createMeshBasedMap(roadMap);
//        mapCreator.createMapSizedRectangle(roadMap, false);
        
        RoadMapAndCenterMatrix roadMapAndCenterMatrix = new RoadMapAndCenterMatrix(roadMap, centerMatrix);
        Position2D start = new Position2D(3028.45,4934.51).transform(roadMapAndCenterMatrix.getCenterMatrix());
        Position2D target = new Position2D(5688.63,2963.04).transform(roadMapAndCenterMatrix.getCenterMatrix());
        
        
        objectCreator.deleteAll();
        objectCreator.removeScriptloader();
    }
}
