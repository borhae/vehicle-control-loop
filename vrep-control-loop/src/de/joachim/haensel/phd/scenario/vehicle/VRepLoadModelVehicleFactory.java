package de.joachim.haensel.phd.scenario.vehicle;

import java.nio.file.Paths;

import coppelia.IntW;
import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VRepLoadModelVehicleFactory implements IVehicleFactory
{
    private IVehicleConfiguration _vehicleConf;
    private VRepRemoteAPI _vrep;
    private int _clientID;
    private VRepObjectCreation _objectCreator;
    private float _scale;

    public VRepLoadModelVehicleFactory(VRepRemoteAPI vrep, int clientID, VRepObjectCreation objectCreator, float scale)
    {
        _vrep = vrep;
        _clientID = clientID;
        _objectCreator = objectCreator;
        _scale = scale;
    }

    @Override
    public void configure(IVehicleConfiguration vehicleConf)
    {
        _vehicleConf = vehicleConf;
    }

    @Override
    public IVehicle createVehicleInstance()
    {
        IVehicle vehicle = null;
        IntW baseHandle = new IntW(0);
        try
        {
            _vrep.simxLoadModel(_clientID, Paths.get("./res/simcarmodel/vehicle.ttm").toAbsolutePath().toString(), 0, baseHandle, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        return vehicle;
    }
}
