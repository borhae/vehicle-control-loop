package de.joachim.haensel.phd.scenario.experiment.setup;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.RoadMapAndCenterMatrix;
import de.joachim.haensel.phd.scenario.SimulationSetupConvenienceMethods;
import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.TMatrix;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

/**
 * Generates points on given map. Does not check whether they are connected
 * @author dummy
 *
 */
public class PointGenerator
{
    private static final String RES_ROADNETWORKS_DIRECTORY = "./res/roadnetworks/";

    public static void main(String[] args) throws VRepException
    {
        VRepRemoteAPI vrep = VRepRemoteAPI.INSTANCE;
        int clientID = vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        VRepObjectCreation objectCreator = new VRepObjectCreation(vrep, clientID);
        String mapFilenName = "chandigarh-roads-lefthand.removed.net.xml";

        RoadMapAndCenterMatrix centeredMap = 
                SimulationSetupConvenienceMethods.createCenteredMap(clientID, vrep, objectCreator, RES_ROADNETWORKS_DIRECTORY + mapFilenName);
        RoadMap roadMap = centeredMap.getRoadMap();
        TMatrix centerMatrix = centeredMap.getCenterMatrix();
    }
}
