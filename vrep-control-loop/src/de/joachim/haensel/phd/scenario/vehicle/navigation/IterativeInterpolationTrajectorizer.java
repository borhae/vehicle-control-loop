package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class IterativeInterpolationTrajectorizer extends AbstractTrajectorizer
{
    private double _stepSize;

    public IterativeInterpolationTrajectorizer(double stepSize)
    {
        _stepSize = stepSize;
    }
    
    @Override
    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        LinkedList<Vector2D> unevenVectorRoute = lineListToVectorList(route);
        List<Vector2D> result = new LinkedList<>();
        unevenVectorRoute  = patchHolesInRoute(unevenVectorRoute);
        quantize(unevenVectorRoute, result, _stepSize);
        return result.stream().map(vector -> new Trajectory(vector)).collect(Collectors.toList());
    }

    public void quantize(LinkedList<Vector2D> unevenVectorRoute, List<Vector2D> result, double stepSize)
    {
        while(!unevenVectorRoute.isEmpty())
        {
            Vector2D curVector = unevenVectorRoute.pop();
            if(curVector.getLength() > stepSize)
            {
                Vector2D newElem = curVector.cutLengthFrom(stepSize);
                result.add(newElem);
                unevenVectorRoute.push(curVector);
            }
            else if(!unevenVectorRoute.isEmpty())
            {
                Vector2D nextVector = unevenVectorRoute.pop();
                Position2D curBase = curVector.getBase();
                Position2D nextTip = nextVector.getTip();
                double distance = Position2D.distance(curBase, nextTip);
                while(distance < stepSize && !unevenVectorRoute.isEmpty())
                {
                    nextVector = unevenVectorRoute.pop();
                    nextTip = nextVector.getTip();
                    distance = Position2D.distance(curBase, nextTip);
                }
                if(distance < stepSize)
                {
                    //even taken together the elements won't be long enough. So last element
                    result.add(new Vector2D(curBase, nextTip));
                }
                else
                {
                    Position2D nextBase = nextVector.getBase();
                    Position2D newElemTip = binaryFindNewTip(curBase, nextBase, nextBase, nextTip, stepSize, 20);
                    Vector2D newElem = new Vector2D(curBase, newElemTip);
                    result.add(newElem);
                    Vector2D residue = new Vector2D(newElemTip, nextTip);
                    unevenVectorRoute.push(residue);
                }
            }
            else
            {
                //last element
                result.add(curVector);
            }
        }
    }

    private Position2D binaryFindNewTip(Position2D base, Position2D curTarget, Position2D lower, Position2D higher, double stepSize, int recursionLimit)
    {
        if(recursionLimit <= 0)
        {
            return curTarget;
        }
        recursionLimit--;
        double distance = Position2D.distance(base, curTarget);
        System.out.println(distance);
        if(distance < stepSize)
        {
            Position2D newTarget = Position2D.between(curTarget, higher);
            return binaryFindNewTip(base, newTarget, curTarget, higher, stepSize, recursionLimit);
        }
        else if(distance > stepSize)
        {
            Position2D newTarget = Position2D.between(curTarget, lower);
            return binaryFindNewTip(base, newTarget, lower, curTarget, stepSize, recursionLimit);
        }
        else
        {
            return curTarget;
        }
    }
}
