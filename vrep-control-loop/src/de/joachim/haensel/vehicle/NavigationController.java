package de.joachim.haensel.vehicle;

import de.joachim.haensel.vehiclecontrol.base.Position2D;

public class NavigationController implements ITopLayerControl
{
    public enum NavigationState
    {
        IDLE
    }

    private ILowLevelController _controllee;
    private NavigationState _navigationState;

    public NavigationController(ILowLevelController lowerControlLayer)
    {
        _controllee = lowerControlLayer;
        _navigationState = NavigationState.IDLE; 
    }

    @Override
    public void driveTo(Position2D position)
    {
        _controllee.driveTo(position);
    }

    @Override
    public void driveToBlocking(Position2D target)
    {
        _controllee.driveTo(target);
    }
}
