package de.joachim.haensel.phd.scenario.vehicle;

import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.vehicle.ILowerLayerFactory;
import de.joachim.haensel.vehicle.IUpperLayerFactory;

public interface IVehicleConfiguration
{
    public IVehicleConfiguration upperCtrlFactory(IUpperLayerFactory upperFact);

    public IVehicleConfiguration lowerCtrlFactory(ILowerLayerFactory lowerFact);

    public IVehicleConfiguration setPosition(double x, double y, double z);
    
    public IVehicleConfiguration setRoadMap(RoadMap roadMap);

    public void setOrientation(Vector2D orientation);


    public double getXPos();

    public double getYPos();

    public double getZPos();

    public RoadMap getMap();

    public ILowerLayerFactory getLowerCtrlFactory();

    public IUpperLayerFactory getUpperCtrlFactory();

    public Vector2D getOrientation();
}
