package de.joachim.haensel.vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class LowLevelEventGenerator extends TimerTask
{
    private List<ILowLevelController> _eventListeners;

    public LowLevelEventGenerator()
    {
        _eventListeners = new ArrayList<>();
    }

    @Override
    public void run()
    {
        _eventListeners.stream().forEach(listener -> listener.controlEvent());
    }

    public void addEventListener(ILowLevelController lowerLevelController)
    {
        _eventListeners.add(lowerLevelController);
    }
}
