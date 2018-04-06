package de.joachim.haensel.phd.scenario.math.interpolation;

import java.util.Deque;
import java.util.LinkedList;

import de.joachim.haensel.phd.scenario.math.TriangleError;
import de.joachim.haensel.phd.scenario.math.TriangleSolver;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.AbstractTrajectorizer;

public class InterpolationTrajectorizerTrigonometry extends AbstractTrajectorizer
{
    
    public InterpolationTrajectorizerTrigonometry(double stepSize)
    {
        super(stepSize);
    }

    @Override
    public void quantize(Deque<Vector2D> input, Deque<Vector2D> result, double stepSize)
    {
        Deque<Vector2D> pointList = new LinkedList<>();
        interpolateRecursive(input, null, pointList, _stepSize);
    }

    public void interpolateRecursiveNonWorking(LinkedList<Vector2D> unevenVectorRoute, Vector2D residue, Deque<Vector2D> resultList, double stepSize)
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
                interpolateRecursiveNonWorking(unevenVectorRoute, residue, resultList, stepSize);
            }
            else
            {
                if(residue == null)
                {
                    residue = fillEvenRoute(curVector, resultList, stepSize);
                    interpolateRecursiveNonWorking(unevenVectorRoute, residue, resultList, stepSize);
                }
                else
                {
                    Vector2D newFirstElement = mergeResidueAndNext(residue, curVector, resultList, stepSize);
                    unevenVectorRoute.push(newFirstElement);
                    interpolateRecursiveNonWorking(unevenVectorRoute, null, resultList, stepSize);
                }
            }
        }
    }
    
    public void interpolateRecursive(Deque<Vector2D> unevenRoute, Vector2D residue, Deque<Vector2D> result, double stepSize)
    {
        if(unevenRoute.isEmpty())
        {
            return;
        }
        else
        {
            Vector2D currentVector = unevenRoute.pop();
            if(currentVector.getLength() < stepSize)
            {
                Vector2D nextVector = unevenRoute.pop();
                while(Position2D.distance(currentVector.getBase(), nextVector.getTip()) < stepSize)
                {
                    nextVector = unevenRoute.pop();
                }
                residue = mergeResidueAndNext(currentVector, nextVector, result, stepSize);
                interpolateRecursive(unevenRoute, residue, result, stepSize);
            }
            else
            {
                if(residue == null)
                {
                    residue = fillEvenRoute(currentVector, result, stepSize);
                    interpolateRecursive(unevenRoute, residue, result, stepSize);
                }
                else
                {
                    residue = mergeResidueAndNext(residue, currentVector, result, stepSize);
                    interpolateRecursive(unevenRoute, residue, result, stepSize);
                }
            }
        }
    }

    private Vector2D mergeResidueAndNext(Vector2D residue, Vector2D curVector, Deque<Vector2D> resultList, double stepSize)
    {
            TriangleSolver tr = new TriangleSolver();
            tr.seta(stepSize);
            double baseDistances = Position2D.distance(residue.getBase(), curVector.getBase());
            tr.setc(baseDistances);
            double angleBetweenVectors = Vector2D.computeAngle(residue, curVector);
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

    private Vector2D fillEvenRoute(Vector2D curVector, Deque<Vector2D> evenVectorRoute, double stepSize)
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
