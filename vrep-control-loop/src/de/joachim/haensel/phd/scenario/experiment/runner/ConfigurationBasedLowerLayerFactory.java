package de.joachim.haensel.phd.scenario.experiment.runner;

import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerControl;
import de.joachim.haensel.phd.scenario.vehicle.ILowerLayerFactory;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable.PurePursuitVariableLookaheadController;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.stanley.StanleyController;

public class ConfigurationBasedLowerLayerFactory implements ILowerLayerFactory
{
    private final ExperimentConfiguration _configuration;
    private final RegularSavingRequestListener _requestListener;
    private final RegularSavingReportListener _reportListener;

    public ConfigurationBasedLowerLayerFactory(ExperimentConfiguration configuration, RegularSavingRequestListener requestListener, RegularSavingReportListener reportListener)
    {
        _configuration = configuration;
        _requestListener = requestListener;
        _reportListener = reportListener;
    }

    @Override
    public ILowerLayerControl create()
    {
        String controllerType = _configuration.getControllerType();
        ILowerLayerControl controller = null;
        if(controllerType.equals("PurePursuitVariableLookahead"))
        {
            controller = new PurePursuitVariableLookaheadController();
        }
        else if (controllerType.equals("Stanley"))
        {
            controller = new StanleyController();
        }
        controller.addTrajectoryRequestListener(_requestListener);
        controller.addTrajectoryReportListener(_reportListener);
        return controller;
    }
}