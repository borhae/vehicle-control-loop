package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.Linspace;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class Segmenter implements ISegmenter
{
    public static final double U_TURN_RADIUS = 10.0;
    private double _stepSize;
    private List<IRouteBuildingListener>_segmentBuildingListeners;
    private ISegmentationAlgorithm _algorithm;

    public Segmenter(double stepSize, ISegmentationAlgorithm algorithm)
    {
        _stepSize = stepSize;
        _segmentBuildingListeners = new ArrayList<>();
        _algorithm = algorithm;
        _algorithm.setSegmentBuildingListeners(_segmentBuildingListeners);
    }
    
    @Override
    public List<TrajectoryElement> createSegments(List<Line2D> route)
    {
        List<TrajectoryElement> result = new ArrayList<>();
        LinkedList<Vector2D> unpatchedRoute = Line2D.lineListToVectorList(route);
        LinkedList<Vector2D> srcRoute = patchHolesInRoute(unpatchedRoute);
        LinkedList<Vector2D> segmentList = new LinkedList<>();
        notifyOriginalTrajectory(segmentList);
        _algorithm.quantize(srcRoute, segmentList, _stepSize);
        segmentList = remove180Turns(segmentList);
        int elementsToAdd = segmentList.size();
        int addCnt = 0;
        while(addCnt < elementsToAdd)
        {
            if(!segmentList.isEmpty())
            {
                Vector2D v = segmentList.pop();
                addCnt++;
                TrajectoryElement t = new TrajectoryElement(v);
                t.setIsOriginal();
                t.setIdx(addCnt);
                result.add(t);
            }
        }
        return result;
    }

    private LinkedList<Vector2D> remove180Turns(List<Vector2D> segmentList)
    {
        LinkedList<Vector2D> result = new LinkedList<>();
        for(int idx = 0; idx < segmentList.size(); idx++)
        {	
        	Vector2D curLine = segmentList.get(idx);
            result.add(curLine);
            if(idx + 1 < segmentList.size())
            {
            	Vector2D nextLine = segmentList.get(idx + 1);
                Vector2D curLineV = new Vector2D(curLine);
                Vector2D nextLineVN = new Vector2D(nextLine);
                double angle = Math.toDegrees(Vector2D.computeAngle(curLineV, nextLineVN));
                if(angle > 120) //TODO add the other direction too
                {
                    //recalculate angle based on the real next line
                    Vector2D nextLineNotSkipped = segmentList.get(idx + 2);
                    Vector2D nextLineNotSkippedVN = new Vector2D(nextLineNotSkipped);
                    angle = Vector2D.computeAngle(curLineV, nextLineNotSkippedVN);
                    
                    replace180TurnWithThreePointTurn(result, curLine, nextLine, angle);
                    idx++;
                }
            }
        }
        return result;
    }

    //add 3 angle/3 * U_TURN_RADIUS turns (first and last forward, second backwards) 
    private void replace180TurnWithThreePointTurn(LinkedList<Vector2D> result, Vector2D curLine, Vector2D nextLine, double angle)
    {
        double turnAngle = angle / 3.0;

        //first turn
        result.addAll(vectorsFromTurn(turnAngle, curLine.getTip(), curLine.getNorm(), U_TURN_RADIUS));
        
        //backwards turn
        LinkedList<Vector2D> backwardsList = vectorsFromTurn(turnAngle, result.getLast().getTip(), Position2D.minus(new Position2D(0, 0),result.getLast().getNorm()), U_TURN_RADIUS);
        //TODO: handling for backwards driving
        result.addAll(backwardsList);
        
        //final turn
        result.addAll(vectorsFromTurn(turnAngle, result.getLast().getTip(), Position2D.minus(new Position2D(0, 0),result.getLast().getNorm()), U_TURN_RADIUS));
       
        //add a vector from the last point of turn to the next line
        Vector2D connection = new Vector2D(result.getLast().getTip(), nextLine.getTip());
        result.add(connection);
    }
    
    //creates a linked list of vectors for a given turn
    private LinkedList<Vector2D> vectorsFromTurn(double angle, Position2D startPoint, Position2D normDirection, double radius){
        
    	 LinkedList<Vector2D> result = new LinkedList<>();
    	 
    	 Position2D perpDir = new Position2D(-normDirection.getY(), normDirection.getX());
    	 
    	 Position2D center = startPoint.plus(perpDir.mul(radius));
    	 Vector2D center2Start = new Vector2D(center, startPoint);
    	 
         Position2D a = Position2D.minus(startPoint, center);
         double startAngle = Math.atan2(a.getY(), a.getX());
         double targetAngle = startAngle + angle;
    	 
    	 
    	 List<Double> thetaRange = new ArrayList<>();
         
         thetaRange = Linspace.linspace(startAngle, targetAngle, 4);
         List<Position2D> points = thetaRange.stream().map(theta -> new Position2D(center.getX() + Math.cos(theta) * U_TURN_RADIUS, center.getY() + Math.sin(theta) * U_TURN_RADIUS)).collect(Collectors.toList());

         Position2D last = null;
         for(int idx1 = 0; idx1 < points.size(); idx1++)
         {
             Position2D current = points.get(idx1);
             if(last != null)
             {
                 result.add(new Vector2D(last, current));
             }
             last = current;
         }

    	 return result;
    }
    
    public LinkedList<Vector2D> patchHolesInRoute(LinkedList<Vector2D> unevenVectorRoute)
    {
        LinkedList<Vector2D> patchedList = new LinkedList<>();
    
        while(!unevenVectorRoute.isEmpty())
        {
            Vector2D curVector = unevenVectorRoute.pop();
            if(unevenVectorRoute.isEmpty())
            {
                patchedList.add(curVector);
                continue;
            }
            Vector2D nextVector = unevenVectorRoute.peek();
            // if we have non adjacent vectors, create the intermediate one and push it onto the list
            if(Position2D.distance(curVector.getTip(), nextVector.getBase()) > EPSILON)
            {
                unevenVectorRoute.push(new Vector2D(curVector.getTip(), nextVector.getBase()));
            }
            patchedList.add(curVector);
        }
        return patchedList;
    }
    
    @Override
    public void addSegmentBuildingListeners(List<IRouteBuildingListener> segmentBuildingListeners)
    {
        _segmentBuildingListeners = segmentBuildingListeners;
    }

    @Override
    public void notifyUpdateTrajectory(Vector2D newVector, Deque<Vector2D> updatedList)
    {
        _segmentBuildingListeners.forEach(listener -> listener.updateTrajectory(newVector, updatedList));
    }
    
    private void notifyOriginalTrajectory(LinkedList<Vector2D> emptyRoute)
    {
        _segmentBuildingListeners.forEach(listener -> listener.notifyStartOriginalTrajectory(emptyRoute));
    }

    @Override
    public ISegmentationAlgorithm getAlgorithm()
    {
        return _algorithm;
    }

    public void quantize(LinkedList<Vector2D> srcRoute, Deque<Vector2D> quantizedRoute, double stepSize)
    {
        _algorithm.quantize(srcRoute, quantizedRoute, stepSize);
    }
}
