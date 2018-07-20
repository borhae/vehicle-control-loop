package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm;

import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.ArcsLinesContainer;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.LineSegment;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceMidpointComputer;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceTransformer;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class LineArcsSegmentation
{
    public ArcsLinesContainer createSegments(Deque<Vector2D> dataPoints)
    {
        ArcsLinesContainer result = new ArcsLinesContainer();
        double width = 0.2;
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        List<Position2D> midpointSet = TangentSpaceMidpointComputer.compute(tangentSpace);
        
        result.add(new LineSegment(new Line2D(0.0, 0.0, 0.0, 0.0)));
        return result;
    }
}
