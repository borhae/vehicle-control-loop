package de.joachim.haensel.phd.scenario.tasks;

public class VehicleStopDebugTask implements ITask
{
    private IVehicleProvider _vehicleProvider;

    public VehicleStopDebugTask(IVehicleProvider vehicleProvider)
    {
        _vehicleProvider = vehicleProvider;
    }
    
    @Override
    public void execute()
    {
        _vehicleProvider.getVehicle().deacvtivateDebugging();
    }

    @Override
    public int getTimeout()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
