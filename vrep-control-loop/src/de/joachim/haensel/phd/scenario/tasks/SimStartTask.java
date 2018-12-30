package de.joachim.haensel.phd.scenario.tasks;

import coppelia.remoteApi;
import de.hpi.giese.coppeliawrapper.VRepException;
import de.hpi.giese.coppeliawrapper.VRepRemoteAPI;

public class SimStartTask implements ITask
{
    private static final int SIMULATION_START_TIME = 1000;
    private VRepRemoteAPI _vrep;
    private int _clientID;

    public SimStartTask(VRepRemoteAPI vrep, int clientID)
    {
        _vrep = vrep;
        _clientID = clientID;
    }
    
    @Override
    public void execute()
    {
        try
        {
            _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
        }
        catch (VRepException exc)
        {
            exc.printStackTrace();
        }
        waitForSimulation(SIMULATION_START_TIME);
    }

    @Override
    public int getTimeout()
    {
        return SIMULATION_START_TIME;
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
