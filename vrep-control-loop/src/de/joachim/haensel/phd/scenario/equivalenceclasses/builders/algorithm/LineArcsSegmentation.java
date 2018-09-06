package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsLineContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.LineSegment;
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
    
    public List<IArcsLineContainerElement> createSegments(List<Position2D> dataPoints)
    {
        double thickness = 0.2;
        double alphaMax = Math.PI / 4.0;
        double nbCirclePoint = 3;
        double isseTol = 4.0;
        return ngoSegmentationAlgorithm(dataPoints, thickness, alphaMax , nbCirclePoint , isseTol);
    }

    private List<IArcsLineContainerElement> ngoSegmentationAlgorithm(List<Position2D> dataPoints, double thickness, double alphaMax, double nbCirclePoint, double isseTol)
    {
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        return inputFormatIndependentNgoSegmentationAlgorithm(tangentSpace, thickness, alphaMax, nbCirclePoint, isseTol);
    }

    private List<IArcsLineContainerElement> ngoSegmentationAlgorithm(Deque<Vector2D> dataPoints, double thickness, double alphaMax, double nbCirclePoint, double isseTol)
    {
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        return inputFormatIndependentNgoSegmentationAlgorithm(tangentSpace, thickness, alphaMax, nbCirclePoint, isseTol);
    }

    private List<IArcsLineContainerElement> inputFormatIndependentNgoSegmentationAlgorithm(List<TangentSegment> tangentSpace, double thickness, double alphaMax, double nbCirclePoint, double isseTol)
    {
        List<IArcsLineContainerElement> result = new ArrayList<>();
        List<Midpoint> midpointSet = TangentSpaceMidpointComputer.compute(tangentSpace);
        
        MinimalBlurredSegment mbs = new MinimalBlurredSegment(thickness);
        PartialArc pArc = new PartialArc();

        for(int idx = 1; idx < midpointSet.size() - 2; idx++)
        {
            Midpoint mp_i_minus1 = midpointSet.get(idx - 1);
            Midpoint mp_i = midpointSet.get(idx);
            Midpoint mp_i_plus1 = midpointSet.get(idx + 1);
            if(isIsolatedPoint(alphaMax, mp_i, mp_i_minus1, mp_i_plus1))
            {
                result.add(new LineSegment(mp_i));
                mbs.clear();
            }
            else
            {
                if(mbs.staysMinimalBlurredSegmentWith(mp_i))
                {
                    mbs.add(mp_i);
                    pArc.add(mp_i.getAssociatedStartPosition(), mp_i.getAssociatedEndPosition());
                }
                else
                {
                    result.add(pArc.toArcSegment());
                    pArc.clear();
                    //maybe we need to take care of current mp_i? In this branch it only had been probed but not added to anything
                }
            }
        }
        return result;
    }

    private boolean isIsolatedPoint(double alphaMax, Midpoint mp_i, Midpoint mp_i_minus1, Midpoint mp_i_plus1)
    {
        return (Math.abs(mp_i.getY() - mp_i_minus1.getY()) > alphaMax) && (Math.abs(mp_i.getY() - mp_i_plus1.getY()) > alphaMax);
    }
}
