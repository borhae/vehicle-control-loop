package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public abstract class AbstractTrajectorizer implements ITrajectorizer
{
    protected static final double EPSILON = 0.00001;
    protected double _stepSize;

    public AbstractTrajectorizer(double stepSize)
    {
        _stepSize = stepSize;
    }
    
    @Override 
    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        List<Trajectory> result = new ArrayList<>();
        LinkedList<Vector2D> srcRoute = lineListToVectorList(route);
        LinkedList<Vector2D> quantizedRoute = new LinkedList<>();
        quantize(srcRoute, quantizedRoute, _stepSize);
        Deque<Vector2D> overlay = createOverlay(srcRoute, _stepSize);
        int elementsToAdd = quantizedRoute.size() + overlay.size();
        int addCnt = 0;
        while(addCnt < elementsToAdd)
        {
            Vector2D vec1 = overlay.pop();
            addCnt++;
            Trajectory elem1 = new Trajectory(vec1);
            
            Vector2D vec2 = quantizedRoute.pop();
            addCnt++;
            Trajectory elem2 = new Trajectory(vec2);
            
            result.add(elem1);
            result.add(elem2);
        }
        //TODO add velocity profile information to Trajectory 
        return result;
    }

    public LinkedList<Vector2D> lineListToVectorList(List<Line2D> route)
    {
        return route.stream().map(line -> new Vector2D(line)).collect(Collectors.toCollection(LinkedList::new));
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
        input.stream().forEach(v -> inputCopy.add(new Vector2D(v)));

        Deque<Vector2D> startSector = computeStartSector(stepSize / 2.0, inputCopy);
        // here the start segment will have length equal or more of (stepsize/2.0)
        Deque<Vector2D> quantizedStartSegment = new LinkedList<>();
        quantize(startSector, quantizedStartSegment, stepSize / 2.0);
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
            quantize(inputCopy, result, stepSize);
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
    
    public abstract void quantize(Deque<Vector2D> source, Deque<Vector2D> result, double stepSize);
}