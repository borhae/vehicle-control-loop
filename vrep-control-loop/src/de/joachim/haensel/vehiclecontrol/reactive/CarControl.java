package de.joachim.haensel.vehiclecontrol.reactive;

import coppelia.FloatWA;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class CarControl
{

    private VRepObjectCreation _creator;
    private int _physicalBodyHandle;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private String _scriptParentName;

    public CarControl(VRepObjectCreation creator, String scriptParentName, VRepRemoteAPI vrep, int clientID, int physicalBodyHandle)
    {
        _creator = creator;
        _physicalBodyHandle = physicalBodyHandle;
        _scriptParentName = scriptParentName;
        _vrep = vrep;
        _clientID = clientID;
    }

    public void initialize() throws VRepException
    {
        createBasicCtrlInterface(_creator, _physicalBodyHandle);
    }

    private void createBasicCtrlInterface(VRepObjectCreation creator, int physicalBodyHandle) throws VRepException
    {
        creator.attachControlScript(physicalBodyHandle);
    }

    public void drive(double targetWheelRotationSpeed, double targetSteeringAngle) throws VRepException
    {        
        FloatWA inFloats = new FloatWA(2);
        inFloats.getArray()[0] = (float)targetSteeringAngle;
        inFloats.getArray()[1] = (float)targetWheelRotationSpeed;
        _vrep.simxCallScriptFunction(_clientID, _scriptParentName, remoteApi.sim_scripttype_childscript, "control", null, inFloats, null, null, null, null, null, null, remoteApi.simx_opmode_blocking);
    }
}
