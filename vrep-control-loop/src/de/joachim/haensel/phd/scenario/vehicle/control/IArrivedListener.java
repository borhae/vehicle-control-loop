package de.joachim.haensel.phd.scenario.vehicle.control;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public interface IArrivedListener
{
    public void arrived(Position2D frontWheelCenterPosition);
}
