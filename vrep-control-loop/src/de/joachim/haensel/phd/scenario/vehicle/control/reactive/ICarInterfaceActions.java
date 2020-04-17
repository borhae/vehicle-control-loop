package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

public interface ICarInterfaceActions
{
    public void reInit();
    public void brakeAndStopAction();
    public void driveLoopAction();
    public boolean hasLookahead();
}
