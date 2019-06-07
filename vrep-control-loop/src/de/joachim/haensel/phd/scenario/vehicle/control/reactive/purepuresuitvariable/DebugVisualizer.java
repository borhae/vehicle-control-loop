package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import java.awt.Color;

import de.joachim.haensel.phd.scenario.debug.DebugParams;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vrepdebugging.DrawingType;
import de.joachim.haensel.phd.scenario.vrepdebugging.IVrepDrawing;

public class DebugVisualizer implements IDebugVisualizer
{
    private static final String CURRENT_SEGMENT_DEBUG_KEY = "curSeg";

    private DebugParams _debugParams;
    private IVrepDrawing _vrepDrawing;
    private IActuatingSensing _actuatorsSensors;

    public DebugVisualizer(DebugParams params, IVrepDrawing vrepDrawing, IActuatingSensing actuatorsSensors)
    {
        _debugParams = params;
        _vrepDrawing = vrepDrawing;
        _vrepDrawing.registerDrawingObject(CURRENT_SEGMENT_DEBUG_KEY, DrawingType.LINE, Color.RED);
        _actuatorsSensors = actuatorsSensors;
    }

    @Override
    public void showLookaheadElement(TrajectoryElement lookaheadTrajectoryElement)
    {
        Vector2D curElemVector = lookaheadTrajectoryElement.getVector();
        double debugMarkerHeight = _debugParams.getSimulationDebugMarkerHeight();
        _vrepDrawing.updateLine(CURRENT_SEGMENT_DEBUG_KEY, curElemVector, debugMarkerHeight, Color.RED);
    }

    @Override
    public void showVelocities(float targetWheelRotation, TrajectoryElement lookaheadTrajectoryElement, TrajectoryElement closestTrajectoryElement)
    {
        if (_debugParams.getSpeedometer() != null)
        {
            _debugParams.getSpeedometer().updateWheelRotationSpeed(targetWheelRotation);
            _debugParams.getSpeedometer().updateCurrentSegment(lookaheadTrajectoryElement);
            if (closestTrajectoryElement != null)
            {
                double[] actualVelocity = _actuatorsSensors.getVehicleVelocity();
                Vector2D orientation = _actuatorsSensors.getLockedOrientation();
                double setVelocity = closestTrajectoryElement.getVelocity();
                _debugParams.getSpeedometer().updateVelocities(actualVelocity, orientation, setVelocity);
            }
            _debugParams.getSpeedometer().repaint();
        }
    }

    @Override
    public void deactivate()
    {
        _vrepDrawing.removeAllDrawigObjects();
    }
}
