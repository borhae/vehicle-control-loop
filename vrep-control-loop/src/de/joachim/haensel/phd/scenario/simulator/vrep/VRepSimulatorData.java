package de.joachim.haensel.phd.scenario.simulator.vrep;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.phd.scenario.simulator.ISimulatorData;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleHandles;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VRepSimulatorData implements ISimulatorData
{
    private String _vehicleScriptParentName;
    private VRepRemoteAPI _vrepAPI;
    private int _clientID;
    private VRepObjectCreation _objectCreator;

    public VRepSimulatorData(VRepObjectCreation objectCreator, VRepRemoteAPI vrep, int clientID, String vehicleScriptParentName)
    {
        _vrepAPI = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
        _vehicleScriptParentName = vehicleScriptParentName;
    }

    public String getVehicleScriptParentName()
    {
        return _vehicleScriptParentName;
    }

    public VRepRemoteAPI getVRepRemoteAPI()
    {
        return _vrepAPI;
    }

    public int getClientID()
    {
        return _clientID;
    }

    @Override
    public void removeItemsFromSimulation(IVehicleHandles vehicleHandles)
    {
        try
        {
            _objectCreator.deleteScripts(vehicleHandles.getAllScriptHandles());
            _objectCreator.deleteAutomaticObjects(vehicleHandles.getAllObjectHandles());
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
    }

    public VRepObjectCreation getVRepObjectCreator()
    {
        return _objectCreator;
    }
}
