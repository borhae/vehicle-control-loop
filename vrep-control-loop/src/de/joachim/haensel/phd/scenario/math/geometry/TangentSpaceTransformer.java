package de.joachim.haensel.phd.scenario.math.geometry;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class TangentSpaceTransformer
{
    /**
     * Compute tangent space representation of a list of input-vectors (see "An Algorithm to Decompose noisy digital contours", by Phuc Ngo, Hayat Nasser, Isabelle Debled-Rennesson, Bertrand Kerautret)
     * @param dataPoints a polygon defined by a queue of vectors
     * @return Tangent Space representation of input 
     */
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

    //TODO maybe write a test case with data from their paper
    /**
     * Compute tangent space representation of a list of input-datapoints (see "An Algorithm to Decompose noisy digital contours", by Phuc Ngo, Hayat Nasser, Isabelle Debled-Rennesson, Bertrand Kerautret)
     * @param dataPoints a polygon defined by a list of points
     * @return Tangent Space representation of input 
     */
    public static List<TangentSegment> transform(List<Position2D> dataPoints)
    {
        List<TangentSegment> tangentSpace = new ArrayList<>();
        tangentSpace.add(new TangentSegment(null, new Position2D(0.0, 0.0)));
        int dataPointsSize = dataPoints.size();
        for(int idx = 0; idx < dataPointsSize - 2; idx++)
        {
            Position2D p0 = dataPoints.get(idx);
            Position2D p1 = dataPoints.get(idx + 1);
            Position2D p2 = dataPoints.get(idx + 2);
            Vector2D v0 = new Vector2D(p0, p1);
            Vector2D v1 = new Vector2D(p1, p2);
            TangentSegment lastTangentSegment = tangentSpace.get(tangentSpace.size() - 1);
            
            double tn1_x = lastTangentSegment.getTn2().getX() + Position2D.distance(p0, p1);
            double tn1_y = lastTangentSegment.getTn2().getY();
            Position2D tn1 = new Position2D(tn1_x, tn1_y);
            double tn2_x = tn1.getX();
            double tn2_y = tn1_y + Vector2D.computeSplitAngle(v0, v1);
            Position2D tn2 = new Position2D(tn2_x, tn2_y);
            tangentSpace.add(new TangentSegment(tn1, tn2));
        }
        int lastIdxDataPoints = dataPointsSize - 1;
        TangentSegment lastAdded = tangentSpace.get(tangentSpace.size() - 1);
        Position2D lastTn2 = lastAdded.getTn2();
        
        Position2D tn_minus_one1 = new Position2D(lastTn2.getX() + Position2D.distance(dataPoints.get(lastIdxDataPoints - 1), dataPoints.get(lastIdxDataPoints)), lastTn2.getY());
        Position2D tn_minus_one2 = null;
        TangentSegment tn_minus_one = new TangentSegment(tn_minus_one1, tn_minus_one2);
        tangentSpace.add(tn_minus_one);
        return tangentSpace;
    }

    public static List<String> tangentSpaceAsFile(List<TangentSegment> tangentSpace, String seperator)
    {
        List<String> result = new ArrayList<>(tangentSpace.size() * 2);
        
        Consumer<? super TangentSegment> transformAction = tangentSegment -> 
        {
            if(tangentSegment.getTn1() != null)
            {
                Position2D tn1 = tangentSegment.getTn1();
                String asString = tn1.getX() + seperator + tn1.getY();
                result.add(asString); 
            }
            if(tangentSegment.getTn2() != null)
            {
                Position2D tn2 = tangentSegment.getTn2();
                String asString = tn2.getX() + seperator + tn2.getY();
                result.add(asString); 
            }
        };
        tangentSpace.stream().forEach(transformAction);
        return result;
    }
}
