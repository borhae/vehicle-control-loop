package de.joachim.haensel.phd.scenario.tasks;

public class VehicleDeactivateDebugTask implements ITask
{
    private IVehicleProvider _vehicleProvider;

    public VehicleDeactivateDebugTask(IVehicleProvider vehicleProvider)
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
