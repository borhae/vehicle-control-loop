package de.joachim.haensel.phd.scenario.vehicle;

import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;
import de.joachim.haensel.vehicle.Vehicle;
import de.joachim.haensel.vehicle.VehicleCreator;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VRepPartwiseVehicleFactory implements IVehicleFactory
{
    private VRepRemoteAPI _vrepApi;
    private int _vrepClientID;
    private VRepObjectCreation _vrepObjectCreator;
    private float _scaleFactor;
    private IVehicleConfiguration _configuration;

    public VRepPartwiseVehicleFactory(VRepRemoteAPI vrepApi, int vrepClientID, VRepObjectCreation vrepObjectCreator, float scaleFactor)
    {
        _vrepApi = vrepApi;
        _vrepClientID = vrepClientID;
        _vrepObjectCreator = vrepObjectCreator;
        _scaleFactor = scaleFactor;
    }

    @Override
    public void configure(IVehicleConfiguration vehicleConf)
    {
        _configuration = vehicleConf;
    }

    @Override
    public IVehicle createVehicleInstance()
    {
        VehicleCreator creator = new VehicleCreator(_vrepApi, _vrepClientID, _vrepObjectCreator, _scaleFactor);
        float x = (float) _configuration.getXPos();
        float y = (float) _configuration.getYPos();
        float z = (float) _configuration.getZPos();
        Vehicle vehicle = creator.createAt(x, y, z, _configuration.getMap(), _configuration.getUpperCtrlFactory(), _configuration.getLowerCtrlFactory());
        return vehicle;
    }
}
