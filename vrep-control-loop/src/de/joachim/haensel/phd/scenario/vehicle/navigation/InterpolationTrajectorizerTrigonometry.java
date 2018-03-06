package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.TriangleError;
import de.joachim.haensel.phd.scenario.math.TriangleSolver;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class InterpolationTrajectorizerTrigonometry extends AbstractTrajectorizer
{
    @Override
    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        float stepSize = 15.0f;
        LinkedList<Vector2D> unevenVectorRoute = lineListToVectorList(route);
//        List<Vector2D> pointList = interpolateRecursive(unevenVectorRoute, stepSize);
        List<Vector2D> pointList = new LinkedList<>();
        unevenVectorRoute  = patchHolesInRoute(unevenVectorRoute);
        interpolateRecursive(unevenVectorRoute, null, pointList, stepSize);
        return pointList.stream().map(vector -> new Trajectory(vector)).collect(Collectors.toList());
    }

    public void interpolateRecursive(LinkedList<Vector2D> unevenVectorRoute, Vector2D residue, List<Vector2D> resultList, float stepSize)
    {
        if(unevenVectorRoute.isEmpty())
        {
            return;
        }
        else
        {
            Vector2D curVector = unevenVectorRoute.pop();
            if(curVector.getLength() < stepSize)
            {
                //push longer vector to beginning of input list
                Vector2D nextVector = unevenVectorRoute.pop();
                Vector2D newFirstElement = new Vector2D(curVector.getBase(), nextVector.getTip());
                unevenVectorRoute.push(newFirstElement);
                interpolateRecursive(unevenVectorRoute, residue, resultList, stepSize);
            }
            else
            {
                if(residue == null)
                {
                    residue = fillEvenRoute(curVector, resultList, stepSize);
                    interpolateRecursive(unevenVectorRoute, residue, resultList, stepSize);
                }
                else
                {
                    Vector2D newFirstElement = mergeResidueAndNext(residue, curVector, resultList, stepSize);
                    unevenVectorRoute.push(newFirstElement);
                    interpolateRecursive(unevenVectorRoute, null, resultList, stepSize);
                }
            }
        }
    }

    private Vector2D mergeResidueAndNext(Vector2D residue, Vector2D curVector, List<Vector2D> resultList, float stepSize)
    {
            TriangleSolver tr = new TriangleSolver();
            tr.seta(stepSize);
            float baseDistances = Position2D.distance(residue.getBase(), curVector.getBase());
            tr.setc(baseDistances);
            float angleBetweenVectors = Vector2D.computeAngle(residue, curVector);
            double angleInDegrees = Math.toDegrees(angleBetweenVectors);
            tr.setAlpha(angleInDegrees);
            float b = Float.NaN;
            try
            {
                tr.solveTriangle();
                switch (tr.getType())
                {
                    case SSA_UNIQUE_SOLUTION:
                        b = (float)tr.getb();
                        break;
                    case SSA_TWO_SOLUTIONS:
                        double[] bs = tr.getTwob();
                        b = getMaxOrNonNaN(bs);
                        break;
                    default:
                        break;
                }
            }
            catch (TriangleError exc)
            {
                exc.printStackTrace();
            }
            if(Float.isNaN(b))
            {
                System.out.println("b is not a number, triangle type: " + tr.getType());
            }
            curVector.cutLengthFrom(b); // the leftover does not matter
            
            Vector2D newElem = new Vector2D(residue.getBase(), curVector.getBase());
            if(newElem.length() > stepSize)
            {
                System.out.println("oops");
            }
            resultList.add(newElem);
            return curVector;
    }

    private float getMaxOrNonNaN(double[] bs) throws TriangleError
    {
        if(Double.isNaN(bs[0]) && Double.isNaN(bs[1]))
        {
            throw new TriangleError("both values not a number!");
        }
        else
        {
           if(Double.isNaN(bs[0]) || Double.isNaN(bs[1]))
           {
               return (float)(Double.isNaN(bs[0]) ? bs[1] : bs[0]);
           }
           else
           {
               return (float)Double.max(bs[0], bs[1]);
           }
        }
    }

    private Vector2D fillEvenRoute(Vector2D curVector, List<Vector2D> evenVectorRoute, float stepSize)
    {
        int fitsNTimes = (int)(curVector.getLength() / stepSize);
        for(int cnt = 0; cnt < fitsNTimes; cnt++)
        {
            Vector2D cut = curVector.cutLengthFrom(stepSize);
            if(cut.length() > stepSize + EPSILON)
            {
                System.out.println("oops");
            }
            evenVectorRoute.add(cut);
        }
        return curVector;
    }
}
