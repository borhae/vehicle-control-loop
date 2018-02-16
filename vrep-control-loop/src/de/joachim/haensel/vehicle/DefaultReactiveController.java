package de.joachim.haensel.vehicle;

import de.joachim.haensel.vehiclecontrol.base.Position2D;

public class DefaultReactiveController implements ILowLevelController
{
    private Position2D _expectedTarget;

    @Override
    public void driveTo(float x, float y)
    {
        _expectedTarget = new Position2D(x, y);
    }
    
    @Override
    public void driveToBlocking(float x, float y)
    {
    }

    @Override
    public void controlEvent()
    {
    }
}
