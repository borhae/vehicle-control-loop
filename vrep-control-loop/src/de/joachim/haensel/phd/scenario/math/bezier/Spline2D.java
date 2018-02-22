/*
 * Created on Sep 23, 2005
 */

package de.joachim.haensel.phd.scenario.math.bezier;

import java.util.ArrayList;
import java.util.List;

public class Spline2D
{
    final int count;
    private final Cubic[] x;
    private final Cubic[] y;
    
    private List<CacheItem> travelCache;
    private float maxTravelStep;
    private float posStep;

    public Spline2D(float[][] points)
    {
        this.count = points.length;

        float[] x = new float[count];
        float[] y = new float[count];

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

    public final float[] getPositionAt(float param)
    {
        float[] v = new float[2];
        this.getPositionAt(param, v);
        return v;
    }

    public final void getPositionAt(float param, float[] result)
    {
        // clamp
        if (param < 0.0f)
            param = 0.0f;
        if (param >= count - 1)
            param = (count - 1) - Math.ulp(count - 1);

        // split
        int ti = (int) param;
        float tf = param - ti;

        // eval
        result[0] = x[ti].eval(tf);
        result[1] = y[ti].eval(tf);
    }

    public void enabledTripCaching(float maxTravelStep, float posStep)
    {
        this.maxTravelStep = maxTravelStep;
        this.posStep = posStep;

        float x = this.x[0].eval(0.0f);
        float y = this.y[0].eval(0.0f);

        this.travelCache = new ArrayList<CacheItem>();
        this.travelCache.add(new CacheItem(x, y, 0.0f));
    }

    public float[] getTripPosition(float totalTrip)
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

        float travel = totalTrip - last._travelled;
        last = this.getSteppingPosition(last._position, travel, posStep);
        return new float[] { last._xpos, last._ypos };
    }

    private CacheItem buildCache(float totalTrip, CacheItem last)
    {
        while (last._travelled < totalTrip)
        {
            if (totalTrip == 0.0f)
            {
                // don't even bother
                break;
            }

            float travel = Math.min(totalTrip - last._travelled, maxTravelStep);

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

    private CacheItem getSteppingPosition(float posOffset, float travel, float segmentStep)
    {
        float pos = posOffset;
        float[] last = this.getPositionAt(pos);

        float travelled = 0.0f;

        while (travelled < travel && pos < this.count)
        {
            float[] curr = this.getPositionAt(pos += segmentStep);
            travelled += Spline2D.dist(last, curr);
            last = curr;
        }

        CacheItem item = new CacheItem(last[0], last[1], 0.0f);
        item._position = pos;
        item._travelled = travelled;
        return item;
    }

    private static float dist(float[] a, float[] b)
    {
        float dx = b[0] - a[0];
        float dy = b[1] - a[1];

        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public List<CacheItem> getTravelCache()
    {
        return travelCache;
    }
}