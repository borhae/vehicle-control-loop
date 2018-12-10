package de.joachim.haensel.phd.scenario.vehicle;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;
import de.joachim.haensel.phd.scenario.vehicle.control.IArrivedListener;

public interface IVehicle
{
    public void activateDebugging(DebugParams debParam);
    
    public void start();
    
    public void driveTo(double x, double y, RoadMap roadMap);
    
    public void driveTo(double x, double y, RoadMap map, IArrivedListener listener);

    public void stop();
    
    public void deacvtivateDebugging();
    
    public void removeFromSimulation();
    
    public void addLowLevelEventGeneratorListener(ILowerLayerControl listener);
    
    public void setPosition(double posX, double posY, double posZ) throws VRepException;
}
