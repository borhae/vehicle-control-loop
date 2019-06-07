package de.joachim.haensel.phd.scenario.vehicle;

import java.util.List;

import de.joachim.haensel.phd.scenario.map.RoadMap;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public interface IVehicleConfiguration
{
    public IVehicleConfiguration setUpperCtrlFactory(IUpperLayerFactory upperFact);

    public IVehicleConfiguration setLowerCtrlFactory(ILowerLayerFactory lowerFact);

    public IVehicleConfiguration setPosition(double x, double y, double z);
    
    public IVehicleConfiguration setRoadMap(RoadMap roadMap);

    public void setOrientation(Vector2D orientation);

    public void setAutoBodyNames(List<String> autoBodyNames);

    public double getXPos();

    public double getYPos();

    public double getZPos();

    public RoadMap getMap();

    public ILowerLayerFactory getLowerCtrlFactory();

    public IUpperLayerFactory getUpperCtrlFactory();

    public Vector2D getOrientation();

    public List<String> getAutoBodyNames();
    
    public int getControlLoopRate();

    public void setControlLoopRate(int controlLoopRate);
}
