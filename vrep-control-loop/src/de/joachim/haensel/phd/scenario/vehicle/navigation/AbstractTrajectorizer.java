package de.joachim.haensel.phd.scenario.vehicle.navigation;

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
    protected float[][] _points;

    public float[][] routeToPointArray(List<Line2D> route)
    {
        float[][] result = new float[route.size() + 1][2];
        route.stream().map(IndexAdder.indexed())
                .forEach(indexedLine -> enterInto(result, indexedLine.idx(), indexedLine.v()));
        return result;
    }

    private void enterInto(float[][] points, int idx, Line2D line)
    {
        points[idx][0] = line.getX1();
        points[idx][1] = line.getY1();
    }

    @Override
    public float[][] getPoints()
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
}