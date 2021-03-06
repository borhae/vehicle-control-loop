package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.segmenting.algorithm;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.lists.CircularAccessList;
import de.joachim.haensel.phd.scenario.math.geometry.MelkmanHull;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;

/**
 * For now this is not really an implementation for a minimal blurred segment
 * Rather it collects points and can decide whether they could make a minimal blurred segment
 * within the limits of a given width. The computation of a, b, omega and my is not implemented.
 * @author dummy
 *
 */
public class MinimalBlurredSegment
{
    private double _thickness;
    
    private double _a;
    private double _b;
    private double _omega;
    private double _ny;

    private List<Position2D> _elements;
    private MelkmanHull _melkmanHull;

    private double _minimumDistanceHorizantalVertical;

    public MinimalBlurredSegment(double thickness)
    {
        _thickness = thickness;
        _elements = new ArrayList<Position2D>();
        _melkmanHull = new MelkmanHull();
        //describes a MBS with _m <= ax - by <= _m + _o
        clear();
    }

    public void add(Position2D newPoint)
    {
        _elements.add(newPoint);
        _melkmanHull.add(newPoint);
    }
    
    public double getIsothetickThickness()
    {
        return computeIsothethicThickness(_melkmanHull);
    }

    public void clear()
    {
        _elements.clear();
        _a = 0.0;
        _b = 0.0;
        _omega = 0.0;
        _ny = 0.0;
        _minimumDistanceHorizantalVertical = Double.MAX_VALUE;
        _melkmanHull.clear();
    }

    public boolean staysMinimalBlurredSegmentWith(Position2D probePoint)
    {
        MelkmanHull copyHull = _melkmanHull.addAndCopy(probePoint);
        double thickness = computeIsothethicThickness(copyHull);
        if(thickness < _thickness)
        {
            _minimumDistanceHorizantalVertical = thickness;
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * The minimum of the horizontal and vertical thickness of the polygon.
     * @param hull
     * @return
     */
    private double computeIsothethicThickness(MelkmanHull basicHull)
    {
        if(basicHull.uniquePointSize() <= 2)
        {
            return 0.0;
        }
        else
        {
            CircularAccessList hull = basicHull.getAsCircularAccessList();
            double resultThickness = Double.MAX_VALUE;
            double curThickness = 0;
            int i = 0; 
            int j = 1;
            
            //I guess this finds the point on the opposite side of the polygon to edge <hull(i), hull(i+1)>
            Position2D antiPodal1Base = hull.get(i);
            Position2D antiPodal1Tip = hull.get(i + 1);
            Vector2D antiPodal1 = new Vector2D(antiPodal1Base, antiPodal1Tip);
            Position2D antiPodal2Base = hull.get(j);
            Position2D antiPodal2Tip = hull.get(j + 1);
            Vector2D antiPodal2 = new Vector2D(antiPodal2Base, antiPodal2Tip);
            //I guess here we try to find an antipodal point to the current edge antipodal1
            while(Vector2D.computeAngleSpecial(antiPodal1, antiPodal2) < Math.PI)
            {
                j++;
                antiPodal1 = new Vector2D(hull.get(i), hull.get(i + 1));
                antiPodal2Base = hull.get(j);
                antiPodal2Tip = hull.get(j + 1);
                antiPodal2 = new Vector2D(antiPodal2Base, antiPodal2Tip);
            }
            curThickness = Vector2D.computeThicknessAntipodalPair(antiPodal1, antiPodal2Base);
            if(curThickness < resultThickness)
            {
                resultThickness = curThickness;
            }
            i++;
            while(i < hull.size())
            {
                antiPodal1 = new Vector2D(hull.get(i), hull.get(i + 1));
                antiPodal2Base = hull.get(j);
                antiPodal2Tip = hull.get(j + 1);
                antiPodal2 = new Vector2D(antiPodal2Base, antiPodal2Tip);
                if(Vector2D.computeAngleSpecial(antiPodal1, antiPodal2) < Math.PI)
                {
                    j++;
                }
                else
                {
                    curThickness = Vector2D.computeThicknessAntipodalPair(antiPodal1, antiPodal2Base);
                    if(curThickness < resultThickness)
                    {
                        resultThickness = curThickness;
                    }
                    if(Vector2D.computeAngleSpecial(antiPodal1, antiPodal2) == Math.PI)
                    {
                        curThickness = Vector2D.computeThicknessAntipodalPair(antiPodal1, antiPodal2Tip);
                        if(curThickness < resultThickness)
                        {
                            resultThickness = curThickness;
                        }
                    }
                    i++;
                }
            }
            return resultThickness;
        }
    }

    public int size()
    {
        return _elements.size();
    }
}
