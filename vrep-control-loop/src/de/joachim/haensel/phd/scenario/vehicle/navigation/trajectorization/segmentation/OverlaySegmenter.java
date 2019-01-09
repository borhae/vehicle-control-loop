package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.ISegmentBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

public class OverlaySegmenter implements ISegmenter
{
    protected double _stepSize;
    protected List<ISegmentBuildingListener> _segmentBuildingListeners;
    private ISegmentationAlgorithm _algorithm;
   
    public OverlaySegmenter(double stepSize, ISegmentationAlgorithm algorithm)
    {
        _stepSize = stepSize;
        _segmentBuildingListeners = new ArrayList<>();
        _algorithm = algorithm;
        _algorithm.setSegmentBuildingListeners(_segmentBuildingListeners);
    }

    @Override
    public void addSegmentBuildingListeners(List<ISegmentBuildingListener> segmentBuildingListeners)
    {
        _segmentBuildingListeners = segmentBuildingListeners;
    }


    @Override
    public void notifyUpdateTrajectory(Vector2D newVector, Deque<Vector2D> updatedList)
    {
        _segmentBuildingListeners.forEach(listener -> listener.updateTrajectory(newVector, updatedList));
    }

    public List<TrajectoryElement> createSegments(List<Line2D> route)
    {
        List<TrajectoryElement> result = new ArrayList<>();
        LinkedList<Vector2D> unpatchedRoute = Line2D.lineListToVectorList(route);
        LinkedList<Vector2D> srcRoute = patchHolesInRoute(unpatchedRoute);
        LinkedList<Vector2D> quantizedRoute = new LinkedList<>();
        notifyOriginalTrajectory(quantizedRoute);
        _algorithm.quantize(srcRoute, quantizedRoute, _stepSize);
        Deque<Vector2D> overlay = createOverlay(srcRoute, _stepSize);
        int elementsToAdd = quantizedRoute.size() + overlay.size();
        int addCnt = 0;
        while(addCnt < elementsToAdd)
        {
            if(!overlay.isEmpty())
            {
                Vector2D vec1 = overlay.pop();
                addCnt++;
                TrajectoryElement elem1 = new TrajectoryElement(vec1);
                elem1.setIsOverlay();
                result.add(elem1);
            }
            if(!quantizedRoute.isEmpty())
            {
                Vector2D vec2 = quantizedRoute.pop();
                addCnt++;
                TrajectoryElement elem2 = new TrajectoryElement(vec2);
                elem2.setIsOriginal();
                result.add(elem2);
            }
        }
        return result;
    }

//    public abstract void quantize(Deque<Vector2D> input, Deque<Vector2D> result, double stepSize);

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
    /**
     * Create a list of vectors that will all have stepsize length. The
     * resulting first vector will have the same base as the first vector of the input list, the tip of the last vector of the result 
     * will accordingly be the same as the tip of the last vector of the input list. The first vector will only have half of the stepsize.
     * @param input list of vectors possibly of uneven size, possibly different from stepSize
     * @param stepSize the target length of the vectors in the result list
     * @return a list of vectors of size stepSize except for the first which will have have the stepsize
     */
    public Deque<Vector2D> createOverlay(Deque<Vector2D> input, double stepSize)
    {
        Deque<Vector2D> inputCopy = new LinkedList<>();
        Deque<Vector2D> result = new LinkedList<>();
        notifyOverlayTrajectory(result);
        input.stream().forEach(v -> inputCopy.add(new Vector2D(v)));

        Deque<Vector2D> startSector = computeStartSector(stepSize / 2.0, inputCopy);
        // here the start segment will have length equal or more of (stepsize/2.0)
        Deque<Vector2D> quantizedStartSegment = new LinkedList<>();
        _algorithm.quantize(startSector, quantizedStartSegment, stepSize / 2.0);
        // firstVector is of length (stepsize/2.0) 
        if(inputCopy.isEmpty())
        {
            // if there was only one vector or there are none left, we need to reassemble from what we got 
            while(!quantizedStartSegment.isEmpty())
            {
                if(quantizedStartSegment.size() >= 2)
                {
                    Vector2D newElem = new Vector2D(quantizedStartSegment.pop().getBase(), quantizedStartSegment.pop().getTip());
                    result.add(newElem);
                }
                else
                {
                    result.add(quantizedStartSegment.pop());
                }
            }
        }
        else
        {
            Vector2D firstVector = quantizedStartSegment.pop();
            Position2D base = firstVector.getTip();
            Position2D tip = inputCopy.peek().getBase();
            Vector2D secondVector = new Vector2D(base, tip);
            
            inputCopy.push(secondVector);
            _algorithm.quantize(inputCopy, result, stepSize);
            result.push(firstVector);
        }
        return result;
    }

    /**
     * Collects as many vectors as necessary to "inscribe" a vector of length size to the result vectorlist
     * @param size required minimum size between first vector base and last vector tip
     * @param input list of vectors to collect from. 
     * @return A list of vector(s) where the distance between first vectors base and last vectors tip is at least of size <code>size</code> 
     */
    private Deque<Vector2D> computeStartSector(double size, Deque<Vector2D> input)
    {
        Position2D inputBase = input.peek().getBase();

        Deque<Vector2D> result = new LinkedList<>();
        Vector2D vectorInStartSector = input.pop();
        result.add(vectorInStartSector);
        Position2D curTip = vectorInStartSector.getTip();
        double curDist = Position2D.distance(inputBase, curTip);
        while(curDist < (size / 2.0))
        {
            vectorInStartSector = input.pop();
            result.add(vectorInStartSector);
            curTip = vectorInStartSector.getTip();
            curDist = Position2D.distance(inputBase, curTip);
        }
        return result;
    }

    private void notifyOriginalTrajectory(LinkedList<Vector2D> emptyRoute)
    {
        _segmentBuildingListeners.forEach(listener -> listener.notifyStartOriginalTrajectory(emptyRoute));
    }
    
    private void notifyOverlayTrajectory(Deque<Vector2D> emptyOverlay)
    {
        _segmentBuildingListeners.forEach(listener -> listener.notifyStartOverlayTrajectory(emptyOverlay));
    }

    public void quantize(LinkedList<Vector2D> srcRoute, Deque<Vector2D> quantizedRoute, double stepSize)
    {
        _algorithm.quantize(srcRoute, quantizedRoute, stepSize);
    }

    @Override
    public ISegmentationAlgorithm getAlgorithm()
    {
        return _algorithm;
    }
}
