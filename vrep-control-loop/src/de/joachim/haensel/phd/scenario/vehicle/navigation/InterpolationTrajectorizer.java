package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.Triangle;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class InterpolationTrajectorizer extends AbstractTrajectorizer
{
    @Override
    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        routeToPointArray(route);
        float minLength = route.stream().map(line -> line.length()).min((d1, d2) -> Float.compare(d1, d2)).get();
//        float stepSize = minLength/2.0f;
        float stepSize = 15.0f;
        List<Vector2D> pointList = interpolate(route, stepSize);
        return pointList.stream().map(vector -> new Trajectory(vector)).collect(Collectors.toList());
    }

    private List<Vector2D> interpolate(List<Line2D> route, float stepSize)
    {
        List<Vector2D> unevenVectorRoute = route.stream().map(line -> new Vector2D(line)).collect(Collectors.toList());
        List<Vector2D> evenVectorRoute = new ArrayList<>();
        Vector2D residue = null;
        for (Vector2D curVector : unevenVectorRoute)
        {
            if(residue != null)
            {
                curVector = mergeAndCut(residue, curVector, evenVectorRoute, stepSize);
            }
            if(curVector.length() < stepSize)
            {
                residue = curVector;
                continue;
            }
            residue = fillEvenRoute(curVector, evenVectorRoute, stepSize);
        }
        return evenVectorRoute;
    }

    private Vector2D fillEvenRoute(Vector2D curVector, List<Vector2D> evenVectorRoute, float stepSize)
    {
        int fitsNTimes = (int)(curVector.length() / stepSize);
        if(!(fitsNTimes * stepSize <= curVector.length()))
        {
            try
            {
                throw new Exception();
            }
            catch (Exception exc)
            {
                exc.printStackTrace();
            }
        }
        for(int cnt = 0; cnt < fitsNTimes; cnt++)
        {
            evenVectorRoute.add(curVector.cutLengthFrom(stepSize));
        }
        return curVector;
    }

    private Vector2D mergeAndCut(Vector2D residue, Vector2D newVec, List<Vector2D> route, float stepSize)
    {
        Triangle tr = new Triangle();
        tr.setA(stepSize);
        float baseDistances = Position2D.distance(residue.getBase(), newVec.getBase());
        tr.setC(baseDistances);
        float angleBetweenVectors = Vector2D.computeAngle(residue, newVec);
        tr.setAlpha(Math.PI - angleBetweenVectors);
        float b = tr.getB();
        if(Float.isNaN(b))
        {
            System.out.println("b is not a number");
        }
        route.add(newVec.cutLengthFrom(b));
        return newVec;
    }
}
