package de.joachim.haensel.phd.scenario.vehicle.vrep;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
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
    private Vector2D _orientation;
    private RoadMap _roadMap;

    @Override
    public IVehicleConfiguration setUpperCtrlFactory(IUpperLayerFactory upperFact)
    {
        _upperFact = upperFact;
        return this;
    }

    @Override
    public IVehicleConfiguration setLowerCtrlFactory(ILowerLayerFactory lowerFact)
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
        return this;
    }
    
    @Override
    public void setOrientation(Vector2D orientation)
    {
        _orientation = orientation;
    }

    @Override
    public IVehicleConfiguration setRoadMap(RoadMap roadMap)
    {
        _roadMap = roadMap;
        return this;
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
        return _roadMap;
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

    @Override
    public Vector2D getOrientation()
    {
        return _orientation;
    }
}
