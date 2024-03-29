package de.joachim.haensel.phd.scenario.vehicle.control.reactive.ppvadaptable;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ITargetProvider;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.RouteBufferStates;
import de.joachim.haensel.phd.scenario.vehicle.control.reactive.TrajectoryBuffer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class PurePuresuitAdaptableTargetProvider implements ITargetProvider
{
    private TrajectoryBuffer _trajectoryBuffer;
    private IActuatingSensing _actuatorsSensors;
    private Position2D _currentPosition;
    private Vector2D _currentOrientation;
    private TrajectoryElement _currentLookaheadElement;
    private Position2D _rearWheelCenterPosition;
    private double _trajectoryElementLength;

    public PurePuresuitAdaptableTargetProvider(TrajectoryBuffer trajectoryBuffer, IActuatingSensing actuatorsSensors)
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
        TrajectoryElement closestTrajectoryElement = internalGetClosestTrajectoryElement(false);
        Vector2D v = closestTrajectoryElement.getVector();
        double distanceErr = v.toLine().distance(_currentPosition);
        double angleErr = Vector2D.computeAngle(v, _currentOrientation);
        AtomicSetActualError error = new AtomicSetActualError(distanceErr, angleErr);
        
        _trajectoryBuffer.triggerEnsureSize(error);
        _currentPosition = _actuatorsSensors.getPosition();
        _rearWheelCenterPosition = _actuatorsSensors.getRearWheelCenterPosition();
        _currentOrientation = _actuatorsSensors.getLockedOrientation();
    }

    /**
     * Get the closest trajectory element from path. Might remove elements from buffer.
     * @return
     */
    public TrajectoryElement getClosestTrajectoryElement()
    {
        return internalGetClosestTrajectoryElement(true);
    }

    private TrajectoryElement internalGetClosestTrajectoryElement(boolean externalCall)
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
        if (minDistIdx != 0 && minDist != Double.POSITIVE_INFINITY && externalCall)
        {
            _trajectoryBuffer.removeBelowIndex(minDistIdx);
        }
        return result;
    }

    public TrajectoryElement getLookaheadTrajectoryElement(double lookahead)
    {
        if(_currentLookaheadElement == null || !isInRange(_currentLookaheadElement, _rearWheelCenterPosition, lookahead))
        {
//            int maxSteps = (int) Math.round(lookahead / _trajectoryElementLength) * 2;           
            int targetIdx = -1;   
            
            
            //find a fitting trajectory element in front of the vehicle
//            int lastIdx = Math.min(_currentClosestElementIndex + maxSteps, _trajectoryBuffer.size() -1 );
            int lastIdx = _trajectoryBuffer.size();
            for(int idx = 0; idx < lastIdx; idx++)
            {
                if(isInRange(_trajectoryBuffer.get(idx), _rearWheelCenterPosition, lookahead))
                {
                    targetIdx = idx;
                    break;
                }                           
            }
            
            if(targetIdx != -1)
            {
                _currentLookaheadElement = _trajectoryBuffer.get(targetIdx);
            }
            else
            {
                if(_trajectoryBuffer.getCurrentState() != RouteBufferStates.ROUTE_ENDING)
                {
                    boolean segmentsLeft = _trajectoryBuffer.elementsLeft();
                    System.out.println("Warning: No matching trajectory element found, taking the closest trajectory element");
                    System.out.println("Buffersize: " + _trajectoryBuffer.size());
                    String info = segmentsLeft ? "yep" : "nope";
                    System.out.println("Segmentprovider has segments? " + info);
                    _currentLookaheadElement = _trajectoryBuffer.get(0);
                    
                }
                else
                {
                    _currentLookaheadElement = _trajectoryBuffer.get(_trajectoryBuffer.size() - 1);
                }
            }
        }
        
        return _currentLookaheadElement;
    }
    
    private boolean isInRange(TrajectoryElement trajectoryElement, Position2D position, double lookahead)
    {
        Vector2D vector = trajectoryElement.getVector();
        double baseDist = Position2D.distance(vector.getBase(), position);
        double tipDist = Position2D.distance(vector.getTip(), position);
        
        return tipDist > lookahead && baseDist <= lookahead;
    }
}
