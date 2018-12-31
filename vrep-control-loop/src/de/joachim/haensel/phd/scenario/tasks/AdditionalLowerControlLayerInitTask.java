package de.joachim.haensel.phd.scenario.tasks;

import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.IVehicle;

public class AdditionalLowerControlLayerInitTask implements ITask
{
    private List<ILowerLayerControl> _lowerLayerControls;
    private IVehicleProvider _vehicleProvider;

    public AdditionalLowerControlLayerInitTask(List<ILowerLayerControl> lowerLayerControls, IVehicleProvider vehicleProvider)
    {
        _lowerLayerControls = lowerLayerControls;
        _vehicleProvider = vehicleProvider;
    }

    @Override
    public void execute()
    {
        IVehicle vehicle = _vehicleProvider.getVehicle();
        _lowerLayerControls.forEach(control -> vehicle.addLowLevelEventGeneratorListener(control));
    }

    @Override
    public int getTimeout()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
