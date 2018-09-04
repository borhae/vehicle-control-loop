package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsLineContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.LineSegment;
import de.joachim.haensel.phd.scenario.math.geometry.Line2D;
import de.joachim.haensel.phd.scenario.math.geometry.Midpoint;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSegment;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceMidpointComputer;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceTransformer;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

public class LineArcsSegmentation
{
    public List<IArcsLineContainerElement> createSegments(Deque<Vector2D> dataPoints)
    {
        double thickness = 0.2;
        double alphaMax = Math.PI / 4.0;
        double nbCirclePoint = 3;
        double isseTol = 4.0;
        return ngoSegmentationAlgorithm(dataPoints, thickness, alphaMax , nbCirclePoint , isseTol);
    }

    private List<IArcsLineContainerElement> ngoSegmentationAlgorithm(Deque<Vector2D> dataPoints, double thickness, double alphaMax, double nbCirclePoint, double isseTol)
    {
        List<IArcsLineContainerElement> result = new ArrayList<>();
        
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        List<Midpoint> midpointSet = TangentSpaceMidpointComputer.compute(tangentSpace);
        
        result.add(new LineSegment(new Line2D(0.0, 0.0, 0.0, 0.0)));
        return result;
    }
}
