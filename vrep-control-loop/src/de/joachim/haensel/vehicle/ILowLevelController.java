package de.joachim.haensel.vehicle;

public interface ILowLevelController
{
    void driveTo(float f, float y);
    void driveToBlocking(float x, float y);
    void controlEvent();
}
