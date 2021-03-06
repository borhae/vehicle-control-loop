package de.joachim.haensel.phd.scenario.vehicle.control.reactive.stanley;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ITargetProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.TrajectoryBuffer;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ppvadaptable.AtomicSetActualError;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class StanleyTargetProvider implements ITargetProvider
{
    private TrajectoryBuffer _trajectoryBuffer;
    private IActuatingSensing _actuatorsSensors;
    private Position2D _currentPosition;
    private Vector2D _currentOrientation;
    private TrajectoryElement _currentLookaheadElement;
    private Position2D _rearWheelCenterPosition;
    private double _trajectoryElementLength;

    public StanleyTargetProvider(TrajectoryBuffer trajectoryBuffer, IActuatingSensing actuatorsSensors)
    {
        _trajectoryBuffer = trajectoryBuffer;
        _trajectoryElementLength = trajectoryBuffer.getTrajectoryElementLength();
        _actuatorsSensors = actuatorsSensors;
        _currentLookaheadElement = null;
    }

    public void reset()
    {
        _trajectoryBuffer.clear();
        _currentLookaheadElement = null;
    }

    public void loopPrepare()
    {
        _trajectoryBuffer.triggerEnsureSize(AtomicSetActualError.NO_FEEDBACK);
        _currentPosition = _actuatorsSensors.getPosition();
        _rearWheelCenterPosition = _actuatorsSensors.getRearWheelCenterPosition();
        _currentOrientation = _actuatorsSensors.getLockedOrientation();
    }

    public TrajectoryElement getClosestTrajectoryElement()
    {
        TrajectoryElement result = null;
        if (_currentPosition == null)
        {
            System.out.println("Closest Trajectory Element: No reference position available!");
            return null;
        }
        if (_trajectoryBuffer.isEmpty())
        {
            System.out.println("Closest Trajectory Element: Buffer is empty!");
            return null;
        }

        double minDistForwardAngle = Double.POSITIVE_INFINITY;
        int minDistForwardAngleIdx = Integer.MAX_VALUE;
        double minDistGeneral = Double.POSITIVE_INFINITY;
        int minDistGeneralIdx = Integer.MAX_VALUE;
        double minDistLowestIdxs = Double.POSITIVE_INFINITY;
        int minDistLowestIdxsIdx = Integer.MAX_VALUE;
        
        for (int idx = 0; idx < _trajectoryBuffer.size(); idx++)
        {
            TrajectoryElement curElem = _trajectoryBuffer.get(idx);
            Vector2D curVector = curElem.getVector();
            double angle = Math.toDegrees(Vector2D.computeAngle(curVector, _currentOrientation));
            double distance = curVector.toLine().distancePerpendicularOrEndpoints(_currentPosition);
            if(distance < minDistGeneral)
            {
                minDistGeneral = distance;
                minDistGeneralIdx = idx;
            }
            if((angle < 120) && distance < minDistForwardAngle)
            {
                minDistForwardAngle = distance;
                minDistForwardAngleIdx = idx;
            }
            if((idx < 4) && distance < minDistLowestIdxs)
            {
                minDistLowestIdxs = distance;
                minDistLowestIdxsIdx = idx;
            }
        }   

        int minDistIdx = Integer.MAX_VALUE;
        double minDist = Double.POSITIVE_INFINITY;
        if(minDistForwardAngle == Double.POSITIVE_INFINITY && minDistGeneral == Double.POSITIVE_INFINITY && minDistLowestIdxs == Double.POSITIVE_INFINITY)
        {
            System.out.println("Could not determine any closest element. Debug here!!");
            return null;
        }
        
        double deltaLowestGeneral = minDistLowestIdxs - minDistGeneral;
        double deltaLowestForwardAngle = minDistLowestIdxs - minDistForwardAngle;
        double deltaForwardGeneral = minDistForwardAngle - minDistGeneral;
        
        if(minDistForwardAngleIdx != Integer.MAX_VALUE && minDistLowestIdxsIdx != Integer.MAX_VALUE && Math.abs(deltaLowestForwardAngle) < (_trajectoryElementLength / 2.0))
        {
            // If there is no significant difference between the forward and the low-index element prefer the low-index element.
            minDist = minDistLowestIdxs;
            minDistIdx = minDistLowestIdxsIdx;
        }
        else if(minDistForwardAngleIdx != Integer.MAX_VALUE && minDistGeneralIdx != Integer.MAX_VALUE && Math.abs(deltaForwardGeneral) < (_trajectoryElementLength * 2.0))
        {
            // If there is no significant difference between the overall closest element and the forward closest element prefer the forward one.
            minDist = minDistForwardAngle;
            minDistIdx = minDistForwardAngleIdx;
        }
        else if(minDistLowestIdxsIdx != Integer.MAX_VALUE && minDistGeneralIdx != Integer.MAX_VALUE && Math.abs(deltaLowestGeneral) < (_trajectoryElementLength / 2.0))
        {
            // if there is no significant difference between the overall closest and the low-index closest element prefer the low-index element
            minDistIdx = minDistLowestIdxsIdx;
            minDist = minDistLowestIdxs;
        }
        else if(minDistGeneralIdx != Integer.MAX_VALUE)
        {
            // worst case: nothing useful found. We are probably far of the track!
            System.out.println("Could not find an element that is either low on index or correct in angle. Going for overall smallest value.");
            minDist = minDistGeneral;
            minDistIdx = minDistGeneralIdx;
        }
        else
        {
            System.out.println("Could not determine any closest element. Debug here!!");
            return null;
        }
        result = _trajectoryBuffer.get(minDistIdx);
        if (minDistIdx != 0 && minDist != Double.POSITIVE_INFINITY)
        {
            _trajectoryBuffer.removeBelowIndex(minDistIdx);
        }
        return result;
    }
}
