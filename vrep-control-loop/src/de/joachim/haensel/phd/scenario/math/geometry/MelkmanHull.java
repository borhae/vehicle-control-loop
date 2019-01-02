package de.joachim.haensel.phd.scenario.math.geometry;

import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.lists.CircularAccessList;

/**
 * Melkman's Algorithm An algorithm for convex hull computation Return a convex
 * hull in ccw order
 */
public class MelkmanHull
{
    // just a normal java LinkedList with an adapted interface to make it match
    // the original algorithm description
    private MelkmanDeque<Position2D> _hull;

    /**
     * Use this for an incremental version of the algorithm
     */
    public MelkmanHull()
    {
        _hull = new MelkmanDeque<>();
    }

    private MelkmanHull(MelkmanDeque<Position2D> clone)
    {
        _hull = clone;
    }

    public void add(Position2D newPoint)
    {
        if (_hull.size() < 2)
        {// empty, the first two points are just added
            _hull.push(newPoint);
        }
        else if (_hull.size() == 2)
        {// init, the third point will actually result in a convex hull (by
         // definition but we need to take care about the order)
            List<Position2D> start = _hull.asList();
            _hull.clear();
            Position2D v1 = start.get(0);
            Position2D v2 = start.get(1);
            Position2D v3 = newPoint;
            if (Line2D.side(v1, v2, v3) > 0)
            {
                _hull.push(v1);
                _hull.push(v2);
            }
            else
            {
                _hull.push(v2);
                _hull.push(v1);
            }
            _hull.push(v3);
            _hull.insert(v3);
        }
        else
        {// go
            Position2D v = newPoint;
            if (!isOnHull(v))
            {// point not on convex hull wait for the next offer
                return;
            }
            while (!(Line2D.side(_hull.get_t_minus1(), _hull.get_t(), v) > 0))
            {
                _hull.pop();
            }
            _hull.push(v);
            while (!(Line2D.side(v, _hull.get_b(), _hull.get_b_plus1()) > 0))
            {
                _hull.remove();
            }
            _hull.insert(v);
        }
    }

    public MelkmanHull addAndCopy(Position2D newPoint)
    {
        MelkmanDeque<Position2D> clone = _hull.copy();
        if (_hull.size() < 2)
        {// empty, the first two points are just added
            clone.push(newPoint);
        }
        else if (clone.size() == 2)
        {// init, the third point will actually result in a convex hull (by
         // definition but we need to take care about the order)
            List<Position2D> start = clone.asList();
            clone.clear();
            Position2D v1 = start.get(0);
            Position2D v2 = start.get(1);
            Position2D v3 = newPoint;
            if (Line2D.side(v1, v2, v3) > 0)
            {
                clone.push(v1);
                clone.push(v2);
            }
            else
            {
                clone.push(v2);
                clone.push(v1);
            }
            clone.push(v3);
            clone.insert(v3);
        }
        else
        {// go
            Position2D v = newPoint;
            if (!isOnHull(v))
            {// point not on convex hull wait for the next offer
                return new MelkmanHull(clone);
            }
            while (!(Line2D.side(clone.get_t_minus1(), clone.get_t(), v) > 0))
            {
                clone.pop();
            }
            clone.push(v);
            while (!(Line2D.side(v, clone.get_b(), clone.get_b_plus1()) > 0))
            {
                clone.remove();
            }
            clone.insert(v);
        }
        return new MelkmanHull(clone);
    }

    private boolean isOnHull(Position2D v)
    {
        return (Line2D.side(v, _hull.get_b(), _hull.get_b_plus1()) < 0)
                || (Line2D.side(_hull.get_t_minus1(), _hull.get_t(), v) < 0);
    }

    public List<Position2D> getHull()
    {
        List<Position2D> result = _hull.asList();
        if (_hull.size() < 3)
        {
            result.add(0, _hull.get_t());
        }
        return result;
    }

    public void clear()
    {
        _hull.clear();
    }

    /**
     * Static version to compute the convex hull. Should resemble the algorithm
     * in the original paper
     * 
     * @param P
     *            the input polygon
     * @return A polygon containing only points that are part of the convex
     *         hull. First and last point are the same
     */
    public static List<Position2D> hull(List<Position2D> P)
    {
        if (P.size() < 3)
        {
            // less than 3 points will kind of be just the input
            ArrayList<Position2D> result = new ArrayList<>(P);
            if (!P.isEmpty())
            {
                result.add(P.get(0));
            }
            return result;
        }
        // A MelkmanDeque is simply a java LinkedList with some renamings so
        // this looks more like
        // the algorithm in the paper
        MelkmanDeque<Position2D> D = new MelkmanDeque<>();
        Position2D v1 = P.get(0);
        Position2D v2 = P.get(1);
        Position2D v3 = P.get(2);
        if (Line2D.side(v1, v2, v3) > 0)
        {
            D.push(v1);
            D.push(v2);
        }
        else
        {
            D.push(v2);
            D.push(v1);
        }
        D.push(v3);
        D.insert(v3);
        for (int idx = 3; idx < P.size(); idx++)
        {
            Position2D v = P.get(idx);
            // there is no until in java so we use a reverse while :)
            while (!((Line2D.side(v, D.get_b(), D.get_b_plus1()) < 0)
                    || (Line2D.side(D.get_t_minus1(), D.get_t(), v) < 0)))
            {
                idx++;
                v = P.get(idx);
            }
            while (!(Line2D.side(D.get_t_minus1(), D.get_t(), v) > 0))
            {
                D.pop();
            }
            D.push(v);
            while (!(Line2D.side(v, D.get_b(), D.get_b_plus1()) > 0))
            {
                D.remove();
            }
            D.insert(v);
        }
        return D.asList();
    }

    public int size()
    {
        return _hull.size();
    }

    /**
     * The algorithm always makes it a closed polygon. The first and the last
     * point are the same. This method gives the amount of points, not double
     * counting the first and the last element.
     * 
     * @return
     */
    public int uniquePointSize()
    {
        return _hull.size() - 1;
    }

    public Position2D get(int index)
    {
        return _hull.get(index);
    }

    public CircularAccessList getAsCircularAccessList()
    {
        CircularAccessList cal = new CircularAccessList(_hull);
        return cal;
    }
}
