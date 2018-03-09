package de.joachim.haensel.phd.scenario.math.interpolation;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.AbstractTrajectorizer;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class IterativeInterpolationTrajectorizer extends AbstractTrajectorizer
{
    public IterativeInterpolationTrajectorizer(double stepSize)
    {
        super(stepSize);
    }

    @Override
    public void quantize(Deque<Vector2D> srcRoute, Deque<Vector2D> result, double stepSize)
    {
        Deque<Vector2D> srcCopy = new LinkedList<>();
        srcRoute.stream().forEach(v -> srcCopy.add(new Vector2D(v)));
        while(!srcCopy.isEmpty())
        {
            Vector2D curVector = srcCopy.pop();
            if(curVector.getLength() > stepSize)
            {
                Vector2D newElem = curVector.cutLengthFrom(stepSize);
                result.add(newElem);
                srcCopy.push(curVector);
            }
            else if(!srcCopy.isEmpty())
            {
                Vector2D nextVector = srcCopy.pop();
                Position2D curBase = curVector.getBase();
                Position2D nextTip = nextVector.getTip();
                double distance = Position2D.distance(curBase, nextTip);
                while(distance < stepSize && !srcCopy.isEmpty())
                {
                    nextVector = srcCopy.pop();
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
                    srcCopy.push(residue);
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
