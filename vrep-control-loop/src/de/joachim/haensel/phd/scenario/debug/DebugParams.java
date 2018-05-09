package de.joachim.haensel.phd.scenario.debug;

public class DebugParams
{
    private double _simulationDebugMarkerHeight;
    private Speedometer _speedometer;

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
}
