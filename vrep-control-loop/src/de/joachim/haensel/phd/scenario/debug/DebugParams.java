package de.joachim.haensel.phd.scenario.debug;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;

public class DebugParams
{
    private double _simulationDebugMarkerHeight;
    private Speedometer _speedometer;
    private ArrayList<INavigationListener> _navigationListeners;

    public DebugParams()
    {
        _navigationListeners = new ArrayList<INavigationListener>();
    }
    
    public void setSimulationDebugMarkerHeight(double simulationDebugMarkerHeight)
    {
        _simulationDebugMarkerHeight = simulationDebugMarkerHeight;
    }

    public double getSimulationDebugMarkerHeight()
    {
        return _simulationDebugMarkerHeight;
    }

    public void setSpeedometer(Speedometer speedometer)
    {
        _speedometer = speedometer;
    }

    public Speedometer getSpeedometer()
    {
        return _speedometer;
    }

    public void addNavigationListener(INavigationListener navigationListener)
    {
        _navigationListeners.add(navigationListener);
    }

    public void notifyNavigationListenersSegmentsChanged(List<Trajectory> segments)
    {
        _navigationListeners.forEach(listener -> listener.notifySegmentsChanged(segments));
    }

    public void notifyNavigationListenersRouteChanged(List<Line2D> route)
    {
        _navigationListeners.forEach(listener -> listener.notifyRouteChanged(route));
    }
}
