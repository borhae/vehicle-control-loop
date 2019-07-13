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
        int elementsToAdd = segmentList.size();
        int addCnt = 0;
        while(addCnt < elementsToAdd)
        {
            if(segmentList.isEmpty())
            {
                Vector2D v = segmentList.pop();
                addCnt++;
                TrajectoryElement t = new TrajectoryElement(v);
                t.setIdx(addCnt);
                result.add(t);
            }
            
            Vector2D v = segmentList.pop();
            
            addCnt++;
            result.add(new TrajectoryElement(v));
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
