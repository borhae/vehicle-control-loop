package de.joachim.haensel.phd.scenario.experiment.runner;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;
import de.joachim.haensel.vrepshapecreation.shapes.EVRepShapes;
import de.joachim.haensel.vrepshapecreation.shapes.ShapeParameters;

public class SimulatorConsolePrint
{
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;
    
    private void run() throws VRepException
    {
        System.out.println("creating vrep instance");
        _vrep = VRepRemoteAPI.INSTANCE;
        System.out.println("vrep instance created");
        _clientID = _vrep.simxStart("127.0.0.1", 19997, true, true, 5000, 5);
        System.out.println("simxstart called");
        _objectCreator = new VRepObjectCreation(_vrep, _clientID);
        System.out.println("object creator created");
        ShapeParameters params = new ShapeParameters();
        params.setMass(10);
        params.setName("Hugo");
        params.setOrientation(0.0f, 0.0f, 0.0f);
        params.setPosition(0.0f, 0.0f, 0.0f);
        params.setType(EVRepShapes.CUBOID);
        params.setVisibility(true);
        params.setSize(10.0f, 10.0f, 10.0f);
        _objectCreator.createPrimitive(params );
        System.out.println("cube created");
        _objectCreator.deleteAll();
        System.out.println("objects deleted");
        _objectCreator.removeScriptloader();
        System.out.println();
    }
    
    public static void main(String[] args)
    {
        SimulatorConsolePrint obj = new SimulatorConsolePrint();
        try
        {
            obj.run();
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }
}
