package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.streamextensions.IndexAdder;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public abstract class AbstractTrajectorizer implements ITrajectorizer
{
    protected static final float EPSILON = 0.00001f;
    protected double[][] _points;

    public double[][] routeToPointArray(List<Line2D> route)
    {
        double[][] result = new double[route.size() + 1][2];
        route.stream().map(IndexAdder.indexed())
                .forEach(indexedLine -> enterInto(result, indexedLine.idx(), indexedLine.v()));
        return result;
    }

    private void enterInto(double[][] points, int idx, Line2D line)
    {
        points[idx][0] = line.getX1();
        points[idx][1] = line.getY1();
    }

    @Override
    public double[][] getPoints()
    {
        return _points;
    }
    
    @Override 
    public abstract List<Trajectory> createTrajectory(List<Line2D> route);

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

    public Deque<Vector2D> createOverlay(Deque<Vector2D> srcRoute, double stepSize)
    {
        Deque<Vector2D> input = new LinkedList<>();
        Deque<Vector2D> result = new LinkedList<>();
        Deque<Vector2D> startSector = new LinkedList<>();
        srcRoute.stream().forEach(v -> input.add(new Vector2D(v)));

        Position2D inputBase = input.peek().getBase();
        Vector2D vectorInStartSector = input.pop();
        startSector.add(vectorInStartSector);
        Position2D curTip = vectorInStartSector.getTip();
        double curDist = Position2D.distance(inputBase, curTip);
        while(curDist < (stepSize / 2.0))
        {
            vectorInStartSector = input.pop();
            startSector.add(vectorInStartSector);
            curTip = vectorInStartSector.getTip();
            curDist = Position2D.distance(inputBase, curTip);
        }
        
        Deque<Vector2D> quantizedStartSegment = new LinkedList<>();
        quantize(startSector, quantizedStartSegment, stepSize / 2.0);
        Vector2D firstVector = quantizedStartSegment.pop();

        Position2D base = firstVector.getTip();
        Position2D tip = input.peek().getBase();
        Vector2D secondVector = new Vector2D(base, tip);
        
        input.push(secondVector);
        quantize(input, result, stepSize);
        result.push(firstVector);
        return result;
    }
    
    public abstract void quantize(Deque<Vector2D> source, Deque<Vector2D> result, double stepSize);
}