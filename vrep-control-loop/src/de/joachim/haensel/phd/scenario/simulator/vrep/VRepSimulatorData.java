package de.joachim.haensel.phd.scenario.simulator.vrep;

import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VRepSimulatorData
{
    private VRepRemoteAPI _vrepAPI;
    private int _clientID;
    private VRepObjectCreation _objectCreator;

    public VRepSimulatorData(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator)
    {
        _vrepAPI = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
    }

    public VRepRemoteAPI getVrepAPI()
    {
        return _vrepAPI;
    }

    public int getClientID()
    {
        return _clientID;
    }

    public VRepObjectCreation getObjectCreator()
    {
        return _objectCreator;
    }
}
