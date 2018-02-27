package de.joachim.haensel.phd.scenario.vehicle.navigation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.joachim.haensel.phd.scenario.math.TriangleError;
import de.joachim.haensel.phd.scenario.math.TriangleSolver;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.navigation.test.TrajectorySnippetFrame;
import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class InterpolationTrajectorizer extends AbstractTrajectorizer
{
    private static final float EPSILON = 0.00001f;

    @Override
    public List<Trajectory> createTrajectory(List<Line2D> route)
    {
        routeToPointArray(route);
        Stream<Float> lengths = route.stream().map(line -> line.length());
        Optional<Float> ressult = lengths.min( (d1, d2) -> Float.compare(d1, d2) );
        float minLength = ressult.get();
//        float stepSize = minLength/2.0f;
        float stepSize = 15.0f;
        List<Vector2D> pointList = interpolate(route, stepSize);
        return pointList.stream().map(vector -> new Trajectory(vector)).collect(Collectors.toList());
    }

    private List<Vector2D> interpolate(List<Line2D> route, float stepSize)
    {
        Collector<Vector2D, ?, LinkedList<Vector2D>> collector = Collectors.toCollection(LinkedList::new);
        LinkedList<Vector2D> unevenVectorRoute = route.stream().map(line -> new Vector2D(line)).collect(collector);
        unevenVectorRoute = patchHolesInRoute(unevenVectorRoute);
        List<Vector2D> evenVectorRoute = new ArrayList<>();
        Vector2D residue = null;
        
        
        //TODO remove this, was here for debugging
        List<Vector2D> snippet = new ArrayList<>(unevenVectorRoute.subList(0, 6));
        TrajectorySnippetFrame visual = new TrajectorySnippetFrame();
        visual.setCurRoute(snippet, snippet.get(0));
        visual.setVisible(true);
        
        while(!unevenVectorRoute.isEmpty())
        {
            Vector2D curVector = unevenVectorRoute.pop();

            //TOOD remove after debug

            snippet = new ArrayList<>(unevenVectorRoute.subList(0, Integer.min(6, unevenVectorRoute.size())));
            visual.setCurRoute(snippet, curVector);
            visual.repaint();
            
            // if the previous computation left a residue, merge it to the next 
            if(residue != null)
            {
                curVector = mergeAndCut(residue, curVector, evenVectorRoute, unevenVectorRoute, stepSize);
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

    private LinkedList<Vector2D> patchHolesInRoute(LinkedList<Vector2D> unevenVectorRoute)
    {
        LinkedList<Vector2D> patchedList = new LinkedList<>();
        while(!unevenVectorRoute.isEmpty())
        {
            Vector2D curVector = unevenVectorRoute.pop();
            if(unevenVectorRoute.isEmpty())
            {
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

    private Vector2D mergeAndCut(Vector2D residue, Vector2D newVec, List<Vector2D> route ,LinkedList<Vector2D> unevenVectorRoute,  float stepSize)
    {
        if(Position2D.distance(residue.getBase(), newVec.getTip()) < stepSize)
        {
            Vector2D newResidue = new Vector2D(residue.getBase(), newVec.getTip());
            if(unevenVectorRoute.isEmpty())
            {
                return mergeAndCut(newResidue, newVec, route, unevenVectorRoute, stepSize);
            }
            newVec = unevenVectorRoute.pop();
            return mergeAndCut(newResidue, newVec, route, unevenVectorRoute, stepSize);
        }
        else    
        {
            TriangleSolver tr = new TriangleSolver();
            tr.seta(stepSize);
            float baseDistances = Position2D.distance(residue.getBase(), newVec.getBase());
            tr.setc(baseDistances);
            float angleBetweenVectors = Vector2D.computeAngle(residue, newVec);
            tr.setAlpha(Math.toDegrees(angleBetweenVectors));
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
            Vector2D cut = newVec.cutLengthFrom(b);
            Vector2D newElem = new Vector2D(residue.getBase(), cut.getBase());
            route.add(newElem);
            return cut;
        }
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

//    private List<Vector2D> interpolate(List<Line2D> route, float stepSize)
//    {
//        List<Vector2D> unevenVectorRoute = route.stream().map(line -> new Vector2D(line)).collect(Collectors.toList());
//        List<Vector2D> evenVectorRoute = new ArrayList<>();
//        Vector2D residue = null;
//        for (Vector2D curVector : unevenVectorRoute)
//        {
//            if(residue != null)
//            {
//                curVector = mergeAndCut(residue, curVector, evenVectorRoute, stepSize);
//            }
//            if(curVector.length() < stepSize)
//            {
//                residue = curVector;
//                continue;
//            }
//            residue = fillEvenRoute(curVector, evenVectorRoute, stepSize);
//        }
//        return evenVectorRoute;
//    }

    private Vector2D fillEvenRoute(Vector2D curVector, List<Vector2D> evenVectorRoute, float stepSize)
    {
        int fitsNTimes = (int)(curVector.length() / stepSize);
        for(int cnt = 0; cnt < fitsNTimes; cnt++)
        {
            Vector2D cut = curVector.cutLengthFrom(stepSize);
            evenVectorRoute.add(cut);
        }
        return curVector;
    }
}
