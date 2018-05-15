package de.joachim.haensel.phd.scenario.vehicle.vrep;

import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.IVehicleConfiguration;
import de.joachim.haensel.vehicle.ILowerLayerFactory;
import de.joachim.haensel.vehicle.IUpperLayerFactory;

public class VRepVehicleConfiguration implements IVehicleConfiguration
{
    private IUpperLayerFactory _upperFact;
    private ILowerLayerFactory _lowerFact;
    private double _posX;
    private double _posY;
    private double _posZ;

    @Override
    public IVehicleConfiguration upperCtrlFactory(IUpperLayerFactory upperFact)
    {
        _upperFact = upperFact;
        return this;
    }

    @Override
    public IVehicleConfiguration lowerCtrlFactory(ILowerLayerFactory lowerFact)
    {
        _lowerFact = lowerFact;
        return this;
    }

    @Override
    public IVehicleConfiguration setPosition(double x, double y, double z)
    {
        _posX = x;
        _posY = y;
        _posZ = z;
        return null;
    }

    @Override
    public double getXPos()
    {
        return _posX;
    }

    @Override
    public double getYPos()
    {
        return _posY;
    }

    @Override
    public double getZPos()
    {
        return _posZ;
    }

    @Override
    public RoadMap getMap()
    {
        return null;
    }

    @Override
    public ILowerLayerFactory getLowerCtrlFactory()
    {
        return _lowerFact;
    }

    @Override
    public IUpperLayerFactory getUpperCtrlFactory()
    {
        return _upperFact;
    }

}
