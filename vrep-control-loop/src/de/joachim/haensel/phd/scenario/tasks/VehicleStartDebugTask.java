package de.joachim.haensel.phd.scenario.tasks;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.debug.INavigationListener;
import de.joachim.haensel.phd.scenario.debug.Speedometer;
import de.joachim.haensel.phd.scenario.debug.VRepNavigationListener;
import de.joachim.haensel.vrepshapecreation.VRepObjectCreation;

public class VehicleStartDebugTask implements ITask
{
    private VRepObjectCreation _objectCreator;
    private IVehicleProvider _vehicleProvider;

    public VehicleStartDebugTask(VRepObjectCreation objectCreator, IVehicleProvider vehicleProvider)
    {
        _objectCreator = objectCreator;
        _vehicleProvider = vehicleProvider;
    }
    
    @Override
    public void execute()
    {
        DebugParams debParam = new DebugParams();
        debParam.setSimulationDebugMarkerHeight(2.0);
        Speedometer speedometer = Speedometer.createWindow();
        debParam.setSpeedometer(speedometer);
        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
        navigationListener.activateSegmentDebugging();
        debParam.addNavigationListener(navigationListener);
        _vehicleProvider.getVehicle().activateDebugging(debParam);
    }

    @Override
    public int getTimeout()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
