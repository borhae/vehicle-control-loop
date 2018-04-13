package de.joachim.haensel.phd.scenario.math.interpolation;

import java.util.Deque;
import java.util.LinkedList;

import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.AbstractTrajectorizer;

public class InterpolationTrajectorizerCircleIntersection extends AbstractTrajectorizer
{
    public InterpolationTrajectorizerCircleIntersection(double stepSize)
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
//            if(curVector.getLength() > stepSize)
            if(curVector.getLength() - stepSize > EPSILON)
            {
                Vector2D newElem = curVector.cutLengthFrom(stepSize);
                result.add(newElem);
                notifyUpdateTrajectory(newElem, result);
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
                if(distance <= stepSize)
                {
                    //even taken together the elements won't be long enough. So last element
                    Vector2D newElem = new Vector2D(curBase, nextTip);
                    result.add(newElem);
                    notifyUpdateTrajectory(newElem, result);
                }
                else
                {
                    Vector2D intersectionOnNextVector = Vector2D.circleIntersection(nextVector, curBase, stepSize).get(0);
                    Position2D newElemTip = intersectionOnNextVector.getTip();
                    Vector2D newElem = new Vector2D(curBase, newElemTip);
                    result.add(newElem);
                    
                    notifyUpdateTrajectory(newElem, result);
                    
                    Vector2D residue = new Vector2D(newElemTip, nextTip);
                    srcCopy.push(residue);
                }
            }
            else
            {
                //last element
                result.add(curVector);
                notifyUpdateTrajectory(curVector, result);
            }
        }
    }
}