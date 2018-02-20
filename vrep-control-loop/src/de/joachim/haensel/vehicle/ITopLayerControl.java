package de.joachim.haensel.vehicle;

import de.joachim.haensel.vehiclecontrol.base.Position2D;

public interface ITopLayerControl
{
    void driveTo(Position2D position2d);

    void driveToBlocking(Position2D position2d);
}
