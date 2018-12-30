package de.joachim.haensel.phd.scenario.tasks.execution;

import java.util.List;

import de.hpi.giese.coppeliawrapper.VRepException;
import de.joachim.haensel.phd.scenario.tasks.ITask;

public class TaskExecutor
{
    public void execute(List<ITask> tasks) throws VRepException
    {
//        IVehicle vehicle = createVehicle(_map, tasks.get(0).getSource(), tasks.get(0).getTarget());
//        _vrep.simxStartSimulation(_clientID, remoteApi.simx_opmode_blocking);
//        waitForSimulation(1000);
//        vehicle.start();
//        
//        DebugParams debParam = new DebugParams(); 
//        debParam.setSimulationDebugMarkerHeight(2.0);
//        Speedometer speedometer = Speedometer.createWindow();
//        debParam.setSpeedometer(speedometer);
//        INavigationListener navigationListener = new VRepNavigationListener(_objectCreator);
//        navigationListener.activateSegmentDebugging();
//        debParam.addNavigationListener(navigationListener);
//        vehicle.activateDebugging(debParam);
//
//        for (ITask curTask : tasks)
//        {
//            curTask.execute();
//            //driveTo was called here
//        }
//        vehicle.deacvtivateDebugging();
//        vehicle.stop();
//        waitForSimulation(1000);
//        _vrep.simxStopSimulation(_clientID, remoteApi.simx_opmode_blocking);
//        waitForSimulation(1000);
        for(ITask curTask : tasks)
        {
            curTask.execute();
        }
    }
}
