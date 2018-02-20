package de.joachim.haensel.vehicle;

import de.joachim.haensel.vehiclecontrol.base.Position2D;

public interface ILowLevelController
{
    public void controlEvent();
    public void driveTo(Position2D position);
}
