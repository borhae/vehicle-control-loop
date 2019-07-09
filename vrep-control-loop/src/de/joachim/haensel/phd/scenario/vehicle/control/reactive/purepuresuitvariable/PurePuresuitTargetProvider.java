package de.joachim.haensel.phd.scenario.vehicle.control.reactive.purepuresuitvariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IActuatingSensing;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class PurePuresuitTargetProvider
{
    private TrajectoryBuffer _trajectoryBuffer;
    private IActuatingSensing _actuatorsSensors;
    private Position2D _currentPosition;
    private Vector2D _currentOrientation;
    private TrajectoryElement _currentLookaheadElement;
    private TrajectoryElement _nearestReverseElement;
    private int _currentClosestElementIndex;
    private Position2D _rearWheelCenterPosition;

    public PurePuresuitTargetProvider(TrajectoryBuffer trajectoryBuffer, IActuatingSensing actuatorsSensors)
    {
        _trajectoryBuffer = trajectoryBuffer;
        _actuatorsSensors = actuatorsSensors;
        _currentLookaheadElement = null;
        _currentClosestElementIndex = -1;
    }

    public void reset()
    {
        _trajectoryBuffer.clear();
        _currentLookaheadElement = null;
        _currentClosestElementIndex = -1;
    }

    public void loopPrepare()
    {
        _trajectoryBuffer.triggerEnsureSize();
        _currentPosition = _actuatorsSensors.getPosition();
        _rearWheelCenterPosition = _actuatorsSensors.getRearWheelCenterPosition();
        _currentOrientation = _actuatorsSensors.getLockedOrientation();
    }

    public TrajectoryElement getNearestReverseElement()
    {
        return _nearestReverseElement;
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

        double minDist = Double.POSITIVE_INFINITY;
        int minDistIdx = Integer.MAX_VALUE;
        for (int idx = 0; idx < _trajectoryBuffer.size(); idx++)
        {
            TrajectoryElement curElem = _trajectoryBuffer.get(idx);
            Vector2D curVector = curElem.getVector();
            if (Math.toDegrees(Vector2D.computeAngle(curVector, _currentOrientation)) < 120 || curElem.isReverse())
            {
                double distance = curVector.toLine().distancePerpendicularOrEndpoints(_currentPosition);
                if (distance < minDist)
                {
                    minDist = distance;
                    minDistIdx = idx;
                    _currentClosestElementIndex = idx;
                    result = curElem;                    
                }
            }
        }
        if(minDist == Double.POSITIVE_INFINITY)
        {
            System.out.println("No element found with ok orientation: going for the closest");
            for (int idx = 0; idx < _trajectoryBuffer.size(); idx++)
            {
                TrajectoryElement curElem = _trajectoryBuffer.get(idx);
                Vector2D curVector = curElem.getVector();
                double distance = curVector.toLine().distancePerpendicularOrEndpoints(_currentPosition);
                if (distance < minDist)
                {
                    minDist = distance;
                    minDistIdx = idx;
                    _currentClosestElementIndex = idx;
                    result = curElem;
                }
            }
        }
        
        if(_trajectoryBuffer.get(_currentClosestElementIndex).isReverse())
        {
            System.out.println("current closest is reverse");
        }
        
        if (minDistIdx != 0 && minDist != Double.POSITIVE_INFINITY)
        {
            _trajectoryBuffer.removeBelowIndex(minDistIdx);
        }
        return result;
    }

    public TrajectoryElement getLookaheadTrajectoryElement(double lookahead)
    {
//        if(_currentLookaheadElement == null || !isInRange(_currentLookaheadElement, _rearWheelCenterPosition, lookahead))
//        {
            if(_currentClosestElementIndex == -1)
            {
                getClosestTrajectoryElement();
            }
            
            int maxSteps = (int) Math.round(lookahead / 5.0) * 4 + 1;           
            int targetIndex = -1;   
            
            
            //find a fitting trajectory element in front of the vehicle
            for(int i = _currentClosestElementIndex; i < Math.min(_currentClosestElementIndex + maxSteps, _trajectoryBuffer.size() -1); i++)
            {
                TrajectoryElement curTrajElement = _trajectoryBuffer.get(i);
                if(isInRange(curTrajElement, _rearWheelCenterPosition, lookahead))
                {
                    System.out.println("element in range found");
                    targetIndex = i;
                    if(!curTrajElement.isReverse()) {
                        _nearestReverseElement = null;
                    }
                    
                    break;
                }
                else if(curTrajElement.isReverse())
                {
                    System.out.println("reverse element found");
                    targetIndex = i;   
                    _nearestReverseElement = curTrajElement;
                    break;
                }
            }
            
            if(targetIndex != -1)
            {
                _currentLookaheadElement = _trajectoryBuffer.get(targetIndex);
            }
            else
            {
                for(int i = 0; i < Math.min(20, _trajectoryBuffer.size() -1); i++)
                {
                    if(_trajectoryBuffer.get(i).isReverse())
                    {
                        System.out.println("reverse stuff");
                    }
                    else {
                        System.out.println("no");
                    }
                }
                
                if(_trajectoryBuffer.getCurrentState() != RouteBufferStates.ROUTE_ENDING)
                {
                    boolean segmentsLeft = _trajectoryBuffer.elementsLeft();
                    System.out.println("Warning: No matching trajectory element found, taking the closest trajectory element");
                    System.out.println("Buffersize: " + _trajectoryBuffer.size());
                    String info = segmentsLeft ? "yep" : "nope";
                    System.out.println("Segmentprovider has segments? " + info);
                    _currentLookaheadElement = _trajectoryBuffer.get(_currentClosestElementIndex);
                    
                }
                else
                {
                    System.out.println("No segment in range on ending route");
                }
            }
//        }
        
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
