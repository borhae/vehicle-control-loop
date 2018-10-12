package de.joachim.haensel.phd.scenario.vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class LowLevelEventGenerator extends TimerTask
{
    private List<ILowerLayerControl> _eventListeners;
    private long _lastEventTimeStamp;

    public LowLevelEventGenerator()
    {
        _eventListeners = new ArrayList<>();
        _lastEventTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void run()
    {
        long curTime = System.currentTimeMillis();
        long diff = curTime - _lastEventTimeStamp;
        _lastEventTimeStamp = curTime;
        System.out.println("time between events in millis: " + diff);
        _eventListeners.stream().forEach(listener -> listener.controlEvent());
    }

    public void addEventListener(ILowerLayerControl lowerLevelController)
    {
        _eventListeners.add(lowerLevelController);
    }
}