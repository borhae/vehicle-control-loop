/*
 * Created on Sep 23, 2005
 */

package de.joachim.haensel.phd.scenario.vehicle.navigation.trajectorization.segmentation.bezier;

import java.util.ArrayList;
import java.util.List;

public class Spline2D
{
    final int count;
    private final Cubic[] x;
    private final Cubic[] y;
    
    private List<CacheItem> travelCache;
    private double maxTravelStep;
    private double posStep;

    public Spline2D(double[][] points)
    {
        this.count = points.length;

        double[] x = new double[count];
        double[] y = new double[count];

        for (int i = 0; i < count; i++)
        {
            x[i] = points[i][0];
            y[i] = points[i][1];
        }

        this.x = Curve.calcCurve(count - 1, x);
        this.y = Curve.calcCurve(count - 1, y);
    }

    public final int pointCount()
    {
        return count;
    }

    public final double[] getPositionAt(double param)
    {
        double[] v = new double[2];
        this.getPositionAt(param, v);
        return v;
    }

    public final void getPositionAt(double param, double[] result)
    {
        // clamp
        if (param < 0.0f)
            param = 0.0f;
        if (param >= count - 1)
            param = (count - 1) - Math.ulp(count - 1);

        // split
        int ti = (int) param;
        double tf = param - ti;

        // eval
        result[0] = x[ti].eval(tf);
        result[1] = y[ti].eval(tf);
    }

    public void enabledTripCaching(double maxTravelStep, double posStep)
    {
        this.maxTravelStep = maxTravelStep;
        this.posStep = posStep;

        double x = this.x[0].eval(0.0f);
        double y = this.y[0].eval(0.0f);

        this.travelCache = new ArrayList<CacheItem>();
        this.travelCache.add(new CacheItem(x, y, 0.0f));
    }

    public double[] getTripPosition(double totalTrip)
    {
        CacheItem last = this.travelCache.get(this.travelCache.size() - 1);
        last = buildCache(totalTrip, last);

        // figure out position
        int lo = 0;
        int hi = this.travelCache.size() - 1;

        while (true)
        {
            int mid = (lo + hi) / 2;

            last = this.travelCache.get(mid);

            if (last._travelled < totalTrip)
            {
                if (lo == mid)
                {
                    break;
                }
                lo = mid;
            }
            else
            {
                if (hi == mid)
                {
                    break;
                }
                hi = mid;
            }
        }

        for (int i = lo; i <= hi; i++)
        {
            CacheItem item = this.travelCache.get(i);

            if (item._travelled <= totalTrip)
            {
                last = item;
            }
            else
            {
                break;
            }
        }

        double travel = totalTrip - last._travelled;
        last = this.getSteppingPosition(last._position, travel, posStep);
        return new double[] { last._xpos, last._ypos };
    }

    private CacheItem buildCache(double totalTrip, CacheItem last)
    {
        while (last._travelled < totalTrip)
        {
            if (totalTrip == 0.0f)
            {
                // don't even bother
                break;
            }

            double travel = Math.min(totalTrip - last._travelled, maxTravelStep);

            CacheItem curr = this.getSteppingPosition(last._position, travel, posStep);

            if (curr._position >= this.count)
            {
                // reached end of spline
                break;
            }

            // only cache if we travelled far enough
            if (curr._travelled > this.maxTravelStep * 0.95f)
            {
                this.travelCache.add(curr);
            }

            curr._travelled += last._travelled;

            last = curr;
        }
        return last;
    }

    private CacheItem getSteppingPosition(double posOffset, double travel, double segmentStep)
    {
        double pos = posOffset;
        double[] last = this.getPositionAt(pos);

        double travelled = 0.0f;

        while (travelled < travel && pos < this.count)
        {
            double[] curr = this.getPositionAt(pos += segmentStep);
            travelled += Spline2D.dist(last, curr);
            last = curr;
        }

        CacheItem item = new CacheItem(last[0], last[1], 0.0f);
        item._position = pos;
        item._travelled = travelled;
        return item;
    }

    private static double dist(double[] a, double[] b)
    {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];

        return  Math.sqrt(dx * dx + dy * dy);
    }

    public List<CacheItem> getTravelCache()
    {
        return travelCache;
    }
}