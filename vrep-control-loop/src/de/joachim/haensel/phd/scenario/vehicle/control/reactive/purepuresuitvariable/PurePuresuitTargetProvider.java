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

    public PurePuresuitTargetProvider(TrajectoryBuffer trajectoryBuffer, IActuatingSensing actuatorsSensors)
    {
        _trajectoryBuffer = trajectoryBuffer;
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
        _trajectoryBuffer.triggerEnsureSize();
        _currentPosition = _actuatorsSensors.getPosition();
        _currentOrientation = _actuatorsSensors.getLockedOrientation();
    }

    public TrajectoryElement getClosestTrajectoryElement()
    {
        TrajectoryElement result = null;
        if (_currentPosition == null)
        {
            System.out.println("Closest Trajectory Element: No reference position available!");
        }
        else if (_trajectoryBuffer.isEmpty())
        {
            System.out.println("Closest Trajectory Element: Buffer is empty!");
        }
        else
        {
            double minDist = Double.POSITIVE_INFINITY;
            int minDistIdx = Integer.MAX_VALUE;
            for (int idx = 0; idx < _trajectoryBuffer.size(); idx++)
            {
                TrajectoryElement curElem = _trajectoryBuffer.get(idx);
                Vector2D curVector = curElem.getVector();
                if (Math.toDegrees(Vector2D.computeAngle(curVector, _currentOrientation)) < 120)
                {
                    double distance = curVector.toLine().distancePerpendicularOrEndpoints(_currentPosition);
                    if (distance < minDist)
                    {
                        minDist = distance;
                        minDistIdx = idx;
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
                        result = curElem;
                    }
                }
            }
            if (minDistIdx != 0 && minDist != Double.POSITIVE_INFINITY)
            {
                _trajectoryBuffer.removeBelowIndex(minDistIdx);
            }
        }
        return result;
    }

    public TrajectoryElement getLookaheadTrajectoryElement(double lookahead)
    {
        if(_currentLookaheadElement == null || !isInRange(_currentLookaheadElement, _currentPosition, lookahead))
        {
            int bestMatchingSegmentIdx = 0;
            Map<TrajectoryElement, Integer> matchingElements = new HashMap<>();
            for(int idx = 0; idx < _trajectoryBuffer.size(); idx++)
            {
                TrajectoryElement curElement = _trajectoryBuffer.get(idx);
                if(isInRange(curElement, _currentPosition, lookahead))
                {
                    matchingElements.put(curElement, idx);
                }
            }
            if(matchingElements.size() > 0)
            {
                Predicate<? super Entry<TrajectoryElement, Integer>> isSharpTurn = entry -> Math.toDegrees(Vector2D.computeAngle(entry.getKey().getVector(), _currentOrientation)) < 120;
                List<Entry<TrajectoryElement, Integer>> matchingOrientation = matchingElements.entrySet().stream().filter(isSharpTurn).collect(Collectors.toList());
                if(matchingOrientation.size() > 0)
                {
                    bestMatchingSegmentIdx = matchingOrientation.get(0).getValue();
                }
                else
                {
                    List<Double> elementDegrees = matchingElements.entrySet().stream().map(entry -> Math.toDegrees(Vector2D.computeAngle(entry.getKey().getVector(), _currentOrientation))).collect(Collectors.toList());
                    System.out.println("Current lookahead: " + lookahead + "Warning: no trajectory element found matching our orientation close enough (120 degrees). Matching elements have these angle differences: " + elementDegrees);
                    
                    bestMatchingSegmentIdx = matchingElements.entrySet().iterator().next().getValue();
                }
                TrajectoryElement newLookaheadTrajectoryElement = _trajectoryBuffer.get(bestMatchingSegmentIdx);
                //don't go back to a former trajectory element (would be the case for new idx < old idx)
                if((_currentLookaheadElement == null) || (newLookaheadTrajectoryElement.getIdx() > _currentLookaheadElement.getIdx()))
                {
                    _currentLookaheadElement = newLookaheadTrajectoryElement;
                }
            }
            else
            {
                if(_trajectoryBuffer.getCurrentState() != RouteBufferStates.ROUTE_ENDING)
                {
                    boolean segmentsLeft = _trajectoryBuffer.elementsLeft();
                    System.out.println("Warning: No matching trajectory element found, waiting for next buffer read");
                    System.out.println("Buffersize: " + _trajectoryBuffer.size());
                    String info = segmentsLeft ? "yep" : "nope";
                    System.out.println("Segmentprovider has segments? " + info);
                }
                else
                {
                    System.out.println("No segment in range on ending route");
                }
            }
        }
        return null;
    }
    
    private boolean isInRange(TrajectoryElement trajectoryElement, Position2D position, double lookahead)
    {
        Vector2D vector = trajectoryElement.getVector();
        double baseDist = Position2D.distance(vector.getBase(), position);
        double tipDist = Position2D.distance(vector.getTip(), position);
        return tipDist >= lookahead && baseDist <= lookahead;
    }
}