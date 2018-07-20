package de.joachim.haensel.phd.scenario.math.geometry;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm.TangentSegment;

public class TangentSpaceTransformer
{
    public static List<TangentSegment> transform(Deque<Vector2D> dataPoints)
    {
        List<TangentSegment> result = new ArrayList<>();
        result.add(new TangentSegment(null, new Position2D(0.0, 0.0)));
        for(int idx = 0; idx < dataPoints.size() - 1; idx++)
        {
            Vector2D p1 = dataPoints.pop();
            Vector2D p2 = dataPoints.peek();
            TangentSegment lastTangentSegment = result.get(result.size() - 1);
            
            double tn1_x = lastTangentSegment.getTn2().getX() + Position2D.distance(p1.getBase(), p2.getBase());
            double tn1_y = lastTangentSegment.getTn2().getY();
            Position2D tn1 = new Position2D(tn1_x, tn1_y);
            double tn2_x = tn1.getX();
            double tn2_y = tn1_y + Vector2D.computeSplitAngle(p1, p2);
            Position2D tn2 = new Position2D(tn2_x, tn2_y);
            result.add(new TangentSegment(tn1, tn2));
        }
        return result;
    }
}
