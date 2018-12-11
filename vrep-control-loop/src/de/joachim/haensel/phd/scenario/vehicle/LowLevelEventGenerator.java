package de.joachim.haensel.phd.scenario.vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class LowLevelEventGenerator extends TimerTask
{
    private List<ILowerLayerControl> _eventListeners;

    public LowLevelEventGenerator()
    {
        _eventListeners = new ArrayList<>();
    }

    @Override
    public void run()
    {
        _eventListeners.stream().forEach(listener -> listener.controlEvent());
    }

    public void addEventListener(ILowerLayerControl lowerLevelController)
    {
        _eventListeners.add(lowerLevelController);
    }

    public List<ILowerLayerControl> getListeners()
    {
        return _eventListeners;
    }
}
