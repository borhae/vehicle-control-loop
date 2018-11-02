package de.joachim.haensel.phd.scenario.vehicle;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.sumo2vrep.RoadMap;

public interface IVehicle
{
    public void activateDebugging(DebugParams debParam);
    
    public void start();
    
    public void driveTo(float x, float y, RoadMap roadMap);
    
    public void stop();
    
    public void deacvtivateDebugging();
    
    public void removeFromSimulation();

    public void addLowLevelEventGeneratorListener(ILowerLayerControl listener);
}
