package de.joachim.haensel.phd.scenario.tasks;

public class VehicleStopTask implements ITask
{
    private IVehicleProvider _vehicleProvider;
    public VehicleStopTask(IVehicleProvider vehicleProvider)
    {
        _vehicleProvider = vehicleProvider;
    }
    
    @Override
    public void execute()
    {
        _vehicleProvider.getVehicle().stop();
        waitForSimulation(1000);
    }

    @Override
    public int getTimeout()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    private void waitForSimulation(int sleepTime)
    {
        try
        {
            Thread.sleep(sleepTime);
        }
        catch (InterruptedException exc)
        {
            exc.printStackTrace();
        }
    }
}
