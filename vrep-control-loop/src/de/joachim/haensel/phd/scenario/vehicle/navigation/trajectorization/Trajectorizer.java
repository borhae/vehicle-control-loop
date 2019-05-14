package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.Linspace;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.IRouteBuildingListener;
import de.joachim.haensel.phd.scenario.vehicle.navigation.ITrajectorizer;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.ISegmenterFactory;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.Segmenter;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssigner;
import de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.velocity.IVelocityAssignerFactory;

public class Trajectorizer implements ITrajectorizer
{
    private ISegmenter _segmenter;
    private IVelocityAssigner _velocityAssigner;

    public Trajectorizer(ISegmenterFactory segmenterFactory, IVelocityAssignerFactory velocityAssignerFactory, double segmentSize)
    {
        _segmenter = segmenterFactory.create(segmentSize);
        _velocityAssigner = velocityAssignerFactory.create(segmentSize);
    }

    @Override
    public List<TrajectoryElement> createTrajectory(List<Line2D> route)
    {
        List<TrajectoryElement> result = _segmenter.createSegments(route);
        result = remove180Turns(result);
        _velocityAssigner.addVelocities(result);
        return result;
    }
    
    private LinkedList<TrajectoryElement> remove180Turns(List<TrajectoryElement> segmentList)
    {
        LinkedList<TrajectoryElement> result = new LinkedList<>();
        for(int idx = 0; idx < segmentList.size(); idx++)
        {   
            Vector2D curLine = segmentList.get(idx).getVector();
            result.add(new TrajectoryElement(curLine));
            if(idx + 2 < segmentList.size())
            {
                Vector2D nextLine = segmentList.get(idx + 1).getVector();
                Vector2D curLineV = new Vector2D(curLine);
                Vector2D nextLineVN = new Vector2D(nextLine);
                double angle = Math.toDegrees(Vector2D.computeAngle(curLineV, nextLineVN));
                if(angle > 120) //TODO add the other direction too
                {
                    //recalculate angle based on the real next line
                    Vector2D nextLineNotSkipped = segmentList.get(idx + 2).getVector();
                    Vector2D nextLineNotSkippedVN = new Vector2D(nextLineNotSkipped);
                    angle = Vector2D.computeAngle(curLineV, nextLineNotSkippedVN);
                    
                    replace180TurnWithThreePointTurn(result, curLine, nextLine, angle);
                    idx++;
                }
            }
        }
        
        for(int i = 0; i < result.size(); i++) {
            result.get(i).setIdx(i);
            result.get(i).setIsOriginal();         
        }
        
        return result;
    }

    //add 3 angle/3 * U_TURN_RADIUS turns (first and last forward, second backwards) 
    private void replace180TurnWithThreePointTurn(LinkedList<TrajectoryElement> result, Vector2D curLine, Vector2D nextLine, double angle)
    {
        double turnAngle = angle / 3.0;

        //first turn
        result.addAll(vectorsFromTurn(turnAngle, curLine.getTip(), curLine.getNorm(), Segmenter.U_TURN_RADIUS));
        
        //backwards turn
        LinkedList<TrajectoryElement> backwardsList = vectorsFromTurn(turnAngle, result.getLast().getVector().getTip(), Position2D.minus(new Position2D(0, 0),result.getLast().getVector().getNorm()), Segmenter.U_TURN_RADIUS);
        for(TrajectoryElement element : backwardsList)
        {
            element.setReverse(true);
        }
        result.addAll(backwardsList);
        
        //final turn
        result.addAll(vectorsFromTurn(turnAngle, result.getLast().getVector().getTip(), Position2D.minus(new Position2D(0, 0),result.getLast().getVector().getNorm()), Segmenter.U_TURN_RADIUS));
       
        //add a vector from the last point of turn to the next line
        Vector2D connection = new Vector2D(result.getLast().getVector().getTip(), nextLine.getTip());
        result.add(new TrajectoryElement(connection));
    }
    
    //creates a linked list of vectors for a given turn
    private LinkedList<TrajectoryElement> vectorsFromTurn(double angle, Position2D startPoint, Position2D normDirection, double radius){
        
         LinkedList<TrajectoryElement> result = new LinkedList<>();
         
         Position2D perpDir = new Position2D(-normDirection.getY(), normDirection.getX());
         
         Position2D center = startPoint.plus(perpDir.mul(radius));
         
         Position2D a = Position2D.minus(startPoint, center);
         double startAngle = Math.atan2(a.getY(), a.getX());
         double targetAngle = startAngle + angle;
         
         
         List<Double> thetaRange = new ArrayList<>();
         
         thetaRange = Linspace.linspace(startAngle, targetAngle, 4);
         List<Position2D> points = thetaRange.stream().map(theta -> new Position2D(center.getX() + Math.cos(theta) * Segmenter.U_TURN_RADIUS, center.getY() + Math.sin(theta) * Segmenter.U_TURN_RADIUS)).collect(Collectors.toList());

         Position2D last = null;
         for(int idx1 = 0; idx1 < points.size(); idx1++)
         {
             Position2D current = points.get(idx1);
             if(last != null)
             {
                 result.add(new TrajectoryElement(new Vector2D(last, current)));
             }
             last = current;
         }

         return result;
    }
    
    @Override
    public void addSegmentBuildingListeners(List<IRouteBuildingListener> listeners)
    {
        _segmenter.addSegmentBuildingListeners(listeners);
    }

    @Override
    public IVelocityAssigner getVelocityAssigner()
    {
        return _velocityAssigner;
    }
}
