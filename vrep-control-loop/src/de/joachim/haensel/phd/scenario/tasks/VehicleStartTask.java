package de.joachim.haensel.phd.scenario.tasks;

public class VehicleStartTask implements ITask
{
    private IVehicleProvider _vehicleProvider;

    public VehicleStartTask(IVehicleProvider vehicleProvider)
    {
        _vehicleProvider = vehicleProvider;
    }
    
    @Override
    public void execute()
    {
        _vehicleProvider.getVehicle().start();
    }

    @Override
    public int getTimeout()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
