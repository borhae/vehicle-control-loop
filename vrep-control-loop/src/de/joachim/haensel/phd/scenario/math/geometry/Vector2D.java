package de.joachim.haensel.phd.scenario.math.geometry;

import java.util.ArrayList;
import java.util.List;

public class Vector2D
{
    private double _bX; //baseX
    private double _bY; //baseY
    private double _length;
    private double _dX; // direction x 
    private double _dY; // direction y 
    private double _normX; //normalized x
    private double _normY; // normalized y
    
    // parameterized: x(t) = _dX * t + _bX, y(t) = _dY * t + _bY
    
    public double getbX()
    {
        return _bX;
    }

    public double getbY()
    {
        return _bY;
    }

    public double getdX()
    {
        return _dX;
    }

    public double getdY()
    {
        return _dY;
    }

    public double getNormX()
    {
        return _normX;
    }

    public double getNormY()
    {
        return _normY;
    }

    public void setLength(double length)
    {
        _length = length;
    }

    public Vector2D(double baseX, double baseY, double dirX, double dirY)
    {
        _bX = baseX;
        _bY = baseY;
        _dX = dirX;
        _dY = dirY;
        updateLength();
        if(Double.isNaN(_bX) || Double.isNaN(_bY) || Double.isNaN(_length))
        {
            System.out.println("Illegal instantiation");
            throw new RuntimeException("Illegal vector instantiation (" + _bX + ", " + _bY + ", " + _length + ") (baseX, baseY, length)");
        }
        _normX = _dX / _length;
        _normY = _dY / _length;
    }

    public Vector2D(Line2D line)
    {
        _bX = line.getX1();
        _bY = line.getY1();
        _length = line.length();
        if(Double.isNaN(_bX) || Double.isNaN(_bY) || Double.isNaN(_length))
        {
            System.out.println("we got some not a numbers!!");
        }
        _dX = line.getX2() - line.getX1();
        _dY = line.getY2() - line.getY1();
        _normX = _dX / _length;
        _normY = _dY / _length;
    }

    public Vector2D(Position2D base, Position2D tip)
    {
        this(base.getX(), base.getY(), tip.getX() - base.getX(), tip.getY() - base.getY());
    }

    public Vector2D(Vector2D v)
    {
        this(v._bX, v._bY, v._dX, v._dY);
    }
    
    private double computeLength(double dirX, double dirY)
    {
        return (double)Math.sqrt(dirX * dirX + dirY * dirY);
    }
    
    private void updateLength()
    {
        _length = Math.sqrt(_dX * _dX + _dY * _dY);
    }

    public double length()
    {
        return _length;
    }

    public Position2D getBase()
    {
        return new Position2D(_bX, _bY);
    }

    /**
     * Cuts a vector of length length from this vector. this vector is shortened by that
     * @param amount to be cut away
     * @return a vector with the same direction as this one with length length
     */
    public Vector2D cutLengthFrom(double amount)
    {
        double newBX = _bX;
        double newBY = _bY;
        double newDX = _normX * amount;
        double newDY = _normY * amount;
        Vector2D cut = new Vector2D(newBX, newBY, newDX, newDY);
        _bX = _bX + amount * _normX;
        _bY = _bY + amount * _normY;
        _dX = _dX - newDX;
        _dY = _dY - newDY;
        _length = (double)Math.sqrt(_dX * _dX + _dY * _dY);
        if(Double.isNaN(_bX) || Double.isNaN(_bY) || Double.isNaN(_length))
        {
            System.out.println("we got some not a numbers!!");
        }
        return cut;
    }

    /**
     * Perpendicular distance or closest point distance
     * @param p
     * @return
     */
    public double unboundedDistance(Position2D p)
    {
        double x1 = _bX;
        double y1 = _bY;
        double x2 = _bX + _dX;
        double y2 = _bY + _dY;
        double xP = p.getX();
        double yP = p.getY();
        
        
        double a = xP - x1;
        double b = yP - y1;
        double c = _dX;
        double d = _dY;

        double dot = a * c + b * d;
        double squaredLength = c * c + d * d;
        
        double param;
        if (squaredLength != 0) 
        {
            param = dot / squaredLength;
        }
        else
        {
            // zero length vector
            return Position2D.distance(p, getBase());
        }

        double xOnVector;
        double yOnVector;

        if (param < 0) 
        {
          xOnVector = x1;
          yOnVector = y1;
        }
        else if (param > 1) 
        {
          xOnVector = x2;
          yOnVector = y2;
        }
        else 
        {
          xOnVector = x1 + param * c;
          yOnVector = y1 + param * d;
        }

        double dx = xP - xOnVector;
        double dy = yP - yOnVector;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public static double computeAngle(Vector2D a, Vector2D b)
    {
        double dotProduct = Vector2D.dotProduct(a, b);
        double magnitudeProduct = a.getLength() * b.getLength();
        double divsionResult = dotProduct/magnitudeProduct;
        return (double)Math.acos(divsionResult);
    }

    public static double dotProduct(Vector2D a, Vector2D b)
    {
        return a._dX * b._dX + a._dY * b._dY;
    }

    public Position2D getDir()
    {
        return new Position2D(_dX, _dY);
    }

    @Override
    public String toString()
    {
        return " <|(" + _bX + ", " + _bY + ")->(" + _dX + ", " + _dY + ")l:" + _length + "|> "; 
    }

    public Position2D getTip()
    {
        return new Position2D(_bX + _dX, _bY + _dY);
    }
    
    public static int compBaseX(Vector2D v1, Vector2D v2)
    {
        return Double.compare(v1.getBase().getX(), v2.getBase().getX());
    }
    
    public static int compBaseY(Vector2D v1, Vector2D v2)
    {
        return Double.compare(v1.getBase().getY(), v2.getBase().getY());
    }
    
    public static int compTipX(Vector2D v1, Vector2D v2)
    {
        return Double.compare(v1.getTip().getX(), v2.getTip().getX());
    }
    
    public static int compTipY(Vector2D v1, Vector2D v2)
    {
        return Double.compare(v1.getTip().getY(), v2.getTip().getY());
    }

    public void sub(double x, double y)
    {
        _bX -= x;
        _bY -= y;
    }
    
    public void add(int x, int y)
    {
        _bX += x;
        _bY += y;
    }
    
    public void mul(double f)
    {
        _bX *= f;
        _bY *= f;
        _dX *= f;
        _dY *= f;
        updateLength();
    }

    public double side(Vector2D left)
    {
        Position2D a = getBase();
        Position2D b = getTip();
        Position2D m = left.getTip(); 
        double determinant = (b.getX() - a.getX()) * (m.getY() - a.getY()) - (b.getY() - a.getY()) * (m.getX() - a.getX());
        return Math.signum(determinant);
    }

    public double getLength()
    {
        return computeLength(_dX, _dY);
    }

    public Position2D getNorm()
    {
        return new Position2D(_normX, _normY);
    }

    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof Vector2D))
        {
            return false;
        }
        else
        {
            Vector2D other = (Vector2D)obj;
            return _bX == other._bX 
                    && _bY == other._bY 
                    && _length == other._length 
                    && _dX == other._dX 
                    && _dY == other._dY 
                    && _normX == other._normX 
                    && _normY == other._normY;
        }
    }

    public static List<Vector2D> circleIntersection(Vector2D vector, Position2D center, double r)
    {
        List<Vector2D> result = new ArrayList<>();
        // formula: ((x1-x0)t+x0-h)^2+((y1-y0)t+y0-k)^2=r^2  with (h, k) centerpoint and r the radius
        // after collection: at^2 + bt +c with
        // a=(x1-x0)^2+(y1-y0)^2
        // b=2(x1-x0)(x0-h)+2(y1-y0)(y0-k)
        // c=(x0-h)^2+(y0-k)^2-r^2
        // t = (-b +- sqr(b^2-4ac)/2a
        double h = center.getX();
        double k = center.getY();
        double x0 = vector._bX;
        double y0 = vector._bY;
        double x0h = x0 - h;
        double y0k = y0 - k;
        double dx = vector._dX;
        double dy = vector._dY;

        double a = dx * dx + dy * dy;
        double b = 2*dx*x0h + 2*dy*y0k; 
        double c = x0h * x0h +  y0k * y0k - r * r;
        
        double root = b * b - 4*a*c;
        if(root >= 0)
        {
            double t1 = (-b + Math.sqrt(root))/(2*a);
            double t2 = (-b - Math.sqrt(root))/(2*a);
            
            if(0 <= t1 && t1 <= 1)
            {
                Vector2D v1 = new Vector2D(x0, y0, t1 * dx, t1 * dy);
                result.add(v1);
            }
            if(0 <= t2 && t2 <= 1)
            {
                Vector2D v2 = new Vector2D(x0, y0, t2 * dx, t2 * dy);
                result.add(v2);
            }
        }
        return result;
    }
    
    /**
     * Create a copy shifted perpendicularly. (negative means left, positive means right)
     * @param shift
     * @return
     */
    public Vector2D shift(double shift)
    {
        double mX = 0.0;
        double mY = 0.0;
        if(shift < 0)
        {
            mX = -_normY;
            mY = _normX;
        }
        else if (shift > 0)
        {
            mX = _normY;
            mY = -_normX;
        } 
        mX *= Math.abs(shift);
        mY *= Math.abs(shift);
        double bX = _bX + mX;
        double bY = _bY + mY;
        Vector2D result = new Vector2D(bX, bY, _dX, _dY);
        // nothing done if shift == 0
        return result;
    }

    /** Intersects the two line segments and returns the intersection point in intersection.
     * 
     * @param p1 The first point of the first line segment
     * @param p2 The second point of the first line segment
     * @param p3 The first point of the second line segment
     * @param p4 The second point of the second line segment
     * @param intersection The intersection point. May be null.
     * @return Whether the two line segments intersect */
//    public static boolean intersectSegments (Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4, Vector2 intersection) 
//    {
//        float x1 = p1.x;
//        float y1 = p1.y;
//        float x2 = p2.x;
//        float y2 = p2.y;
//        float x3 = p3.x;
//        float y3 = p3.y;
//        float x4 = p4.x;
//        float y4 = p4.y;
//        
//        float d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
//        if (d == 0) return false;
//        
//        float yd = y1 - y3;
//        float xd = x1 - x3;
//        float ua = ((x4 - x3) * yd - (y4 - y3) * xd) / d;
//        if (ua < 0 || ua > 1) return false;
//        
//        float ub = ((x2 - x1) * yd - (y2 - y1) * xd) / d;
//        if (ub < 0 || ub > 1) return false;
//        
//        if (intersection != null) intersection.set(x1 + (x2 - x1) * ua, y1 + (y2 - y1) * ua);
//        return true;
//    }

    //TODO unfinished!! Low Priority
    public Position2D intersectPolygon(Position2D[] polygon)
    {
        for(int idx = 0; idx < polygon.length - 1; idx++)
        {
            Vector2D curSide = new Vector2D(polygon[idx], polygon[idx + 1]);
//            Position2D intersection = Vector2D.intersect(this, curSide);
            double t = scalarIntersect(this, curSide);
        }
        
        return null;
    }

    public static Double scalarIntersect(Vector2D a, Vector2D b)
    {
        // t = (a.base - b.base) x a.dir / b.dir x a.dir
        // u = (a.base - b.base) x b.dir / b.dir x a.dir
        Position2D basDiff = Position2D.minus(a.getBase(), b.getBase());
        double crossDir = Position2D.crossProduct(b.getDir(), a.getDir());
        
        double tNumerator = Position2D.crossProduct(basDiff, a.getDir());
        double uNumerator = Position2D.crossProduct(basDiff, b.getDir());
        
        if(crossDir != 0.0)
        {
            double t = tNumerator / crossDir;
            double u = uNumerator / crossDir;
            if(((0 <= t) && (t <= 1)) && ((0 <= u) && (u <= 1)))
            {
                return t;
            }
        }
        return Double.NaN;
    }

    public static Position2D rangedIntersect(Vector2D a, Vector2D b)
    {
        Position2D p = a.getBase();
        Position2D r = a.getDir();
        Position2D q = b.getBase();
        Position2D s = b.getDir();
        // t = (q - p) x s / (r x s) -> t is on a
        // u = (q - p) x r / (r x s) == (p - q) x r / (s x r)-> u is on b
        // t = qp x s / rs
        // u = qp x r / rs
        Position2D qp = Position2D.minus(q, p);
        double rs = Position2D.crossProduct(r, s);
        double tNominator = Position2D.crossProduct(qp, s);
        double uNominator = Position2D.crossProduct(qp, r);
        if(rs == 0)
        {
            //lines are parallel
            if(uNominator == 0)
            {
                //lines are collinear, let's not deal yet with it
                //TODO fix this!!!
                return null;
            }
            else
            {
                //No intersection
                return null;
            }
        }
        else
        {
            double t = tNominator / rs;
            double u = uNominator / rs;
            if(((0 <= t) && (t <= 1)) && ((0 <= u) && (u <= 1))) 
            {
                return Position2D.plus(p, Position2D.multiply(t, r));
            }
            else
            {
                //intersection outside of vectors length
                return null; 
            }
        }
    }
    
    public static Position2D unrangedOnFirstIntersect(Vector2D a, Vector2D b)
    {
        Position2D p = a.getBase();
        Position2D r = a.getDir();
        Position2D q = b.getBase();
        Position2D s = b.getDir();
        // t = (q - p) x s / (r x s) -> t is on a
        // u = (q - p) x r / (r x s) == (p - q) x r / (s x r)-> u is on b
        // t = qp x s / rs
        // u = qp x r / rs
        Position2D qp = Position2D.minus(q, p);
        double rs = Position2D.crossProduct(r, s);
        double tNominator = Position2D.crossProduct(qp, s);
        double uNominator = Position2D.crossProduct(qp, r);
        if(rs == 0)
        {
            //lines are parallel
            if(uNominator == 0)
            {
                //lines are collinear, let's not deal yet with it
                //TODO fix this!!!
                return null;
            }
            else
            {
                //No intersection
                return null;
            }
        }
        else
        {
            double t = tNominator / rs;
            double u = uNominator / rs;
            if(((0 <= u) && (u <= 1))) 
            {
                return Position2D.plus(q, Position2D.multiply(u, s));
            }
            else
            {
                //intersection outside of vectors length
                return null; 
            }
        }
    }

    public static Position2D unrangedIntersect(Vector2D a, Vector2D b)
    {
        // t = (a.base - b.base) x a.dir / b.dir x a.dir
        // u = (a.base - b.base) x b.dir / b.dir x a.dir
        Position2D basDiff = Position2D.minus(a.getBase(), b.getBase());
        double crossDir = Position2D.crossProduct(b.getDir(), a.getDir());
        
        double tNumerator = Position2D.crossProduct(basDiff, a.getDir());
        double uNumerator = Position2D.crossProduct(basDiff, b.getDir());

        //if crossDir is 0.0 we have parallel lines...
        if(crossDir != 0.0)
        {
            double t = tNumerator / crossDir;
            return Position2D.plus(b.getBase(), Position2D.multiply(t, b.getDir()));
        }
        return null;
    }

    public Vector2D getPerpendicular()
    {
        return new Vector2D(_bX, _bY, -_dY, _dX);
    }

    public Vector2D getMiddlePerpendicular()
    {
        Vector2D result = getPerpendicular();
        double newBX = _bX + _dX * 0.5;
        double newBY = _bY + _dY * 0.5;
        result.resetBase(newBX, newBY);
        return result;
    }

    public static Position2D getUnrangedPerpendicularIntersection(Vector2D v, Position2D p)
    {
        Vector2D vP = new Vector2D(p.getX(), p.getY(), -v.getdY(), v.getdX());
        return unrangedIntersect(v, vP);
    }
    
    public static Position2D getPerpendicularIntersection(Vector2D v, Position2D p)
    {
        Vector2D vP = new Vector2D(p.getX(), p.getY(), -v.getdY(), v.getdX());
        
        return unrangedOnFirstIntersect(vP, v);
    }
    
    public Vector2D scale(double s)
    {
        _dX = _normX * s;
        _dY = _normY * s;
        updateLength();
        return this;
    }

    public void resetBase(double bX, double bY)
    {
        _bX = bX;
        _bY = bY;
    }
}
