package de.joachim.haensel.phd.scenario.math.geometry;

import java.util.List;

/** Melkman's Algorithm
 *  www.ams.sunysb.edu/~jsbm/courses/345/melkman.pdf
 *  Return a convex hull in ccw order
 */
public class MelkmanHull
{

    public void add(Position2D newPoint)
    {
        // TODO Auto-generated method stub  
    }
    
    public static List<Position2D> hull(List<Position2D> P)
    {
        // A MelkmanDeque is simply a java LinkedList with some renamings so this looks more like the original algorithm
        MelkmanDeque<Position2D> D = new MelkmanDeque<>();
        Position2D v1 = P.get(0);
        Position2D v2 = P.get(1);
        Position2D v3 = P.get(2);
        if(Line2D.side(v1, v2, v3) > 0)
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
        for (int idx = 0; idx < P.size(); idx++)
        {
            Position2D v = P.get(idx);
            //there is no until in java so we use a reverse while :)
            while(!(
                    (Line2D.side(v, D.get_b(), D.get_b_plus1()) < 0) || (Line2D.side(D.get_t_minus1(), D.get_t(), v) < 0) 
                    ))
            {
                idx++;
                v = P.get(idx);
            }
            while(!(
                    Line2D.side(D.get_t_minus1(), D.get_t(), v) > 0
                    ))
            {
                D.pop();
            }
            D.push(v);
            while(!(
                    Line2D.side(v, D.get_b(), D.get_b_plus1()) > 0
                    ))
            {
                D.remove();
            }
            D.insert(v);
        }
        return D.asList();
    }
    
    public List<Position2D> hullInternet(List<Position2D> V)
    {
        int n = V.size();
//        val D = new Array[Vector2f](2 * n + 1)
//        Deque<Position2D> D = new LinkedList<>(); // should contain 2 * n + 1 entries (why?)
        
        int bot = n - 2;
        int top = bot + 3;

//        
//        D(bot) = V(2)
//        D(top) = V(2)
//
//        if (left(V(0), V(1), V(2))) {
//          D(bot+1) = V(0)
//          D(bot+2) = V(1)
//        } else {
//          D(bot+1) = V(1)
//          D(bot+2) = V(0)
//        }
//
//        var i = 3
//        while(i < n) {
//          while (left(D(bot), D(bot+1), V(i)) && left(D(top-1), D(top), V(i))) {
//            i += 1
//          }
//          while (!left(D(top-1), D(top), V(i))) top -= 1
//          top += 1; D(top) = V(i)
//          while (!left(D(bot), D(bot+1), V(i))) bot += 1
//          bot -= 1; D(bot) = V(i)
//          i += 1
//        }
//
//        val H = new Array[Vector2f](top - bot)
//        var h = 0
//        while(h < (top - bot)) {
//          H(h) = D(bot + h)
//          h += 1
//        }
//        H
        return null;
    }
    
    
}
