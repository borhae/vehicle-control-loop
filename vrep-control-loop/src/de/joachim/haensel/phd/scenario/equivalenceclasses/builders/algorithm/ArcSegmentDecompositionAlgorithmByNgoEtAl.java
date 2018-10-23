package de.joachim.haensel.phd.scenario.equivalenceclasses.builders.algorithm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.IArcsSegmentContainerElement;
import de.joachim.haensel.phd.scenario.equivalenceclasses.builders.Segment;
import de.joachim.haensel.phd.scenario.math.geometry.Midpoint;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSegment;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceMidpointComputer;
import de.joachim.haensel.phd.scenario.math.geometry.TangentSpaceTransformer;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

/**
 * Adapted version of Ngo et. al algorithm for decomposition of noisy digital contours
 * "An algorithm to decompose noisy digital contours"
 * The following changes have been made:
 * 1.) Adatpted segment vs. arc condition: in addition to the ISSE, the radius of the arc is taken into account. If it exceeds parameter maxRadius the result will be a segment 
 * @author Joachim Haensel
 *
 */
public class ArcSegmentDecompositionAlgorithmByNgoEtAl
{
    public List<IArcsSegmentContainerElement> createSegments(Deque<Vector2D> dataPoints, double thickness, double alphaMax, double nbCirclePoint, double isseTol, double maxRadius)
    {
        return ngoSegmentationAlgorithm(dataPoints, thickness, alphaMax , nbCirclePoint , isseTol, maxRadius);
    }

    public List<IArcsSegmentContainerElement> createSegments(Deque<Vector2D> dataPoints)
    {
        double thickness = 0.2;
        double alphaMax = Math.PI / 4.0;
        double nbCirclePoint = 3;
        double isseTol = 4.0;
        double maxRadius = 10000;
        return ngoSegmentationAlgorithm(dataPoints, thickness, alphaMax , nbCirclePoint , isseTol, maxRadius);
    }
    
    public List<IArcsSegmentContainerElement> createSegments(List<Position2D> dataPoints)
    {
        double thickness = 0.2;
        double alphaMax = Math.PI / 4.0;
        double nbCirclePoint = 3;
        double isseTol = 4.0;
        double maxRadius = 10000;
        return ngoSegmentationAlgorithm(dataPoints, thickness, alphaMax , nbCirclePoint , isseTol, maxRadius);
    }

    private List<IArcsSegmentContainerElement> ngoSegmentationAlgorithm(List<Position2D> dataPoints, double thickness, double alphaMax, double nbCirclePoint, double isseTol, double maxRadius)
    {
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        return inputFormatIndependentNgoSegmentationAlgorithm(tangentSpace, thickness, alphaMax, nbCirclePoint, isseTol, maxRadius);
    }

    private List<IArcsSegmentContainerElement> ngoSegmentationAlgorithm(Deque<Vector2D> dataPoints, double thickness, double alphaMax, double nbCirclePoint, double isseTol, double maxRadius)
    {
        List<TangentSegment> tangentSpace = TangentSpaceTransformer.transform(dataPoints);
        return inputFormatIndependentNgoSegmentationAlgorithm(tangentSpace, thickness, alphaMax, nbCirclePoint, isseTol, maxRadius);
    }

    /**
     * Algorithm seems to work with the additions I made (mainly adding missing points)
     * @param tangentSpace
     * @param thickness
     * @param alphaMax
     * @param nbCirclePoint
     * @param isseTol
     * @param maxRadius 
     * @return
     */
    private List<IArcsSegmentContainerElement> inputFormatIndependentNgoSegmentationAlgorithm(List<TangentSegment> tangentSpace, double thickness, double alphaMax, double nbCirclePoint, double isseTol, double maxRadius)
    {
        List<IArcsSegmentContainerElement> result = new ArrayList<>();
        List<Midpoint> midpointSet = TangentSpaceMidpointComputer.compute(tangentSpace);
        if(midpointSet.isEmpty())
        {
            return new ArrayList<>();
        }
        MinimalBlurredSegment mbs = new MinimalBlurredSegment(thickness);
        PartialArc pArc = new PartialArc(isseTol);

        Midpoint mp_i_minus1 = null; 
        Midpoint mp_i = midpointSet.get(0);
        Midpoint mp_i_plus1 = null;

        //TODO added because otherwise we'll miss the first element
        pArc.add(mp_i.getAssociatedStartPosition(), mp_i.getAssociatedEndPosition());
        for(int idx = 1; idx < midpointSet.size() - 1; idx++) // 5
        {
            mp_i_minus1 = midpointSet.get(idx - 1); 
            mp_i = midpointSet.get(idx);
            mp_i_plus1 = midpointSet.get(idx + 1);
            if(isIsolatedPoint(alphaMax, mp_i, mp_i_minus1, mp_i_plus1)) // 7
            {
                // add everything 
                // TODO I added this because the algorithm would otherwise discard any collected pArc as soon as it finds an isolated point as the next point
                if(!pArc.isEmpty())
                {
                    if(mbs.size() >= nbCirclePoint) //15
                    {
                        //create arc from pArc // 16, 17
                        pArc.InitArcAndSegment(); // 16, 17
                        if(pArc.isArcsISSESmallerThanSegments() && pArc.radiusBelow(maxRadius)) //18
                        {
                            result.add(pArc.toArc()); //19
                            mbs.clear();// TODO added by me. I guess we need to clear since we want to start with a new one?
                        }
                        else
                        {
                            result.add(pArc.toSegment()); //21
                            mbs.clear();// TODO added by me. I guess we need to clear since we want to start with a new one?
                        }
                    }
                    else
                    {
                        result.add(pArc.toSegment()); //23
                        mbs.clear();// TODO added by me. I guess we need to clear since we want to start with a new one?
                    }
                    // maybe we need to take care of current mp_i? In this branch it only had been probed but not added to anything, so it's basically discarded
                    // this way we don't discard it
                    pArc.clear(mp_i.getAssociatedStartPosition());
                }
                result.add(new Segment(mp_i)); //8
                mbs.clear(); // 9 
            }
            else
            {
                if(mbs.staysMinimalBlurredSegmentWith(mp_i)) // 11
                {
                    mbs.add(mp_i); // 12
                    pArc.add(mp_i.getAssociatedStartPosition(), mp_i.getAssociatedEndPosition()); //13
                }
                else
                {
                    if(mbs.size() >= nbCirclePoint) //15
                    {
                        //create arc from pArc // 16, 17
                        pArc.InitArcAndSegment(); // 16, 17
                        if(pArc.isArcsISSESmallerThanSegments() && pArc.radiusBelow(maxRadius)) //18
                        {
                            result.add(pArc.toArc()); //19
                            mbs.clear();// TODO added by me. I guess we need to clear since we want to start with a new one?
                        }
                        else
                        {
                            result.add(pArc.toSegment()); //21
                            mbs.clear();// TODO added by me. I guess we need to clear since we want to start with a new one?
                        }
                    }
                    else
                    {
                        result.add(pArc.toSegment()); //23
                        mbs.clear();// TODO added by me. I guess we need to clear since we want to start with a new one?
                    }
                    //maybe we need to take care of current mp_i? In this branch it only had been probed but not added to anything, so it's basically discarded
                    pArc.clear(mp_i.getAssociatedStartPosition());
                }
            }
        }
//        //TODO added because otherwise we'll miss the last assembled points
        if(!pArc.isEmpty())
        {
            if(mbs.size() >= nbCirclePoint) //15
            {
                //create arc from pArc // 16, 17
                pArc.InitArcAndSegment(); // 16, 17
                if(pArc.isArcsISSESmallerThanSegments() && pArc.radiusBelow(maxRadius)) //18
                {
                    result.add(pArc.toArc()); //19
                    mbs.clear();// TODO added by me. I guess we need to clear since we want to start with a new one?
                }
                else
                {
                    result.add(pArc.toSegment()); //21
                    mbs.clear();// TODO added by me. I guess we need to clear since we want to start with a new one?
                }
            }
            else
            {
                result.add(pArc.toSegment()); //23
                mbs.clear();// TODO added by me. I guess we need to clear since we want to start with a new one?
            }
//          //maybe we need to take care of current mp_i? In this branch it only had been probed but not added to anything, so it's basically discarded
//          pArc.clear(mp_i.getAssociatedStartPosition());
        }
        return result;
    }

    private boolean isIsolatedPoint(double alphaMax, Midpoint mp_i, Midpoint mp_i_minus1, Midpoint mp_i_plus1)
    {
        return (Math.abs(mp_i.getY() - mp_i_minus1.getY()) > alphaMax) && (Math.abs(mp_i.getY() - mp_i_plus1.getY()) > alphaMax);
    }
}
