package de.joachim.haensel.phd.scenario.experiment.runner;

import de.joachim.haensel.phd.converters.UnitConverter;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.IUpperLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.DefaultNavigationController;
import de.joachim.haensel.phd.scenario.vehicle.navigation.adaptable.AdaptingNavigationController;

public class ConfigurationBasedUpperLayerFactory implements IUpperLayerFactory
{
    private ExperimentConfiguration _configuration;

    public ConfigurationBasedUpperLayerFactory(ExperimentConfiguration configuration)
    {
        _configuration = configuration;
    }

    @Override
    public IUpperLayerControl create()
    {
        IUpperLayerControl result;
        String navigatorType = _configuration.getNavigatorType();
        double maxVelocity = _configuration.getMaxVelocity();
        double maxLongitudinalAcceleration = _configuration.getMaxLongitudinalAcceleration();
        double maxLongitudinalDecceleration = _configuration.getMaxLongitudinalDecceleration();
        double maxLateralAcceleration = _configuration.getMaxLateralAcceleration();
        double segmentSize = _configuration.getSegmentSize();
        switch (navigatorType)
        {
            case "DefaultNavigationController":
                result = new DefaultNavigationController(5.0, UnitConverter.kilometersPerHourToMetersPerSecond(maxVelocity), maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);
                break;
            case "AdaptingNavigationController" :
                result = new AdaptingNavigationController(5.0, UnitConverter.kilometersPerHourToMetersPerSecond(maxVelocity));
                break;
            default:
                result = new DefaultNavigationController(5.0, UnitConverter.kilometersPerHourToMetersPerSecond(maxVelocity), maxLongitudinalAcceleration, maxLongitudinalDecceleration, maxLateralAcceleration);
                break;
        }
        return result;
    }
}
