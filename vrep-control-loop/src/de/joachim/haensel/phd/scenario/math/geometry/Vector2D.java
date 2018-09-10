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
        double result = Math.acos(divsionResult);
        return result;
    }
    
    /**
     * Takes too input vectors. Computes the single angle between them. If the result is larger PI 
     * p2 is considered to point to the left side of p1, otherwise to the right.
     * Get's a positive value for right and a negative for left side results
     * @param v1 the reference vector
     * @param v2 the angled vector
     * @return the positive or negative angle vector (pointing to the right or left -hand side)
     */
    public static double computeSplitAngle(Vector2D v1, Vector2D v2)
    {
        double result = computeAngle(v1, v2);
        double side = v1.side(v2);
        boolean rightOfV1 = side <= 0;
        if(rightOfV1)
        {
            return result;
        }
        else
        {
            return - result;
        }
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

    /**
     * Computes on which side the other vector is in relation to this one
     * @param other the other vector
     * @return values smaller than 0 indicate on the right side, greater 0 on the left side and 0 means collinear
     */
    public double side(Vector2D other)
    {
        Position2D a = getBase();
        Position2D b = getTip();
        Position2D m = other.getTip(); 
        return Line2D.side(a, b, m);
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

    //TODO unfinished!! Low Priority
    public Position2D intersectPolygon(Position2D[] polygon)
    {
//        for(int idx = 0; idx < polygon.length - 1; idx++)
//        {
//            Vector2D curSide = new Vector2D(polygon[idx], polygon[idx + 1]);
////            Position2D intersection = Vector2D.intersect(this, curSide);
//            double t = scalarIntersect(this, curSide);
//        }
        
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
                //lines are collinear, no intersection
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

    public static double computeHorizontalThickness(Vector2D line, Position2D point)
    {
        double result = 0.0;
        if(line.getdY() == 0.0)
        {
            // TODO Still no idea why in the original code there is not this simple solution
            // original:  return std::numeric_limits<double>::max();
            // The part that I do know is that they usually use infinity to mark invalid values.
            // TODO In case of erroneous behavior figure out why it might make sense to put this to infinity
            return Math.abs(line._bY - point.getY());
        }
        else
        {
            //TODO until I fully understand their code I rather translate these names to make sure the copy does the same
            // I could replace this with Vector2D.unrangedIntersect(v1, v2). v1 = new Vector2D(point.getX(), point.getY(), 0.0, 1.0) and v2. = line
            // the distance between the new point and point is the result
            Position2D a = line.getBase();
            Position2D b = line.getTip();
            Position2D c = point;
            // original code from C++
            //  k = -(a[0]-b[0])*c[1]-(b[0]*a[1]-a[0]*b[1]);
            double dotProduct = b.getX() * a.getY() - a.getX() * b.getY();
            double deltaX = a.getX() - b.getX();
            double k = -deltaX * c.getY() - dotProduct;
            result = Math.abs(k / (b.getY() - a.getY()) - c.getX());
            return result;
        }
    }

    public static double computeVerticalThickness(Vector2D line, Position2D point)
    {
        double result = 0.0;
        if(line.getdX() == 0.0)
        {
            // TODO No idea why they put that there, need to reevaluate when more focused
            // original:  return std::numeric_limits<double>::max();
            // The part that I do know is that they usually use infinity to mark invalid values.
            // TODO In case of erroneous behavior figure out why it might make sense to put this to infinity
            return Math.abs(line._bX - point.getX());
        }
        else
        {
            //TODO until I fully understand their code I rather translate these names to make sure the copy does the same
            // I could replace this with Vector2D.unrangedIntersect(v1, v2). v1 = new Vector2D(point.getX(), point.getY(), 0.0, 1.0) and v2. = line
            // the distance between the new point and point is the result
            Position2D a = line.getBase();
            Position2D b = line.getTip();
            Position2D c = point;
            // original code from C++
            //  k = -(a[0]-b[0])*c[1]-(b[0]*a[1]-a[0]*b[1]);
            double k = -(b.getY() - a.getY()) * c.getX() - (b.getX() * a.getY() - a.getX() * b.getY());
            result = Math.abs(k / (b.getX() - a.getX()) - c.getY());
            return result;
        }
    }
    

    public static double computeThicknessAntipodalPair(Vector2D line, Position2D point)
    {
        double horizontalThickness = Vector2D.computeHorizontalThickness(line, point);
        double verticalThickness = Vector2D.computeVerticalThickness(line, point);
        return Math.min(horizontalThickness, verticalThickness);
    }

    /**
     * This method is implemented according to the implementation for the arc segment decomposition algorithm.
     * should kind of do the same as my own algorithm
     * @param v1
     * @param v2
     * @return
     */
    public static double computeAngleSpecial(Vector2D v1, Vector2D v2)
    {
        Position2D a = v1.getBase();
        Position2D b = v1.getTip();
        Position2D c = v2.getBase();
        Position2D d = v2.getTip();
        double angle1 = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX()); // or atan2(v1.getdY(), v1.getdX())
        double angle2 = Math.atan2(d.getY() - c.getY(), d.getX() - c.getX());
        double r = angle2 - angle1;
        return r < 0 ? 2 * Math.PI + r : r;
    }
}
