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
        _length = computeLength(dirX, dirY);
        if(Double.isNaN(_bX) || Double.isNaN(_bY) || Double.isNaN(_length))
        {
            System.out.println("Illegal instantiation");
            throw new RuntimeException("Illegal vector instantiation (" + _bX + ", " + _bY + ", " + _length + ") (baseX, baseY, length)");
        }
        _dX = dirX;
        _dY = dirY;
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
     * Perpendicular (smallest) distance of point to vector
     * @param point to be considered
     * @return The length of perpendicular between given point and this vector
     */
    public double distance(Position2D point)
    {
        double pX = point.getX();
        double pY = point.getY();
        double a = computeLength(_bX -pX, _bY - pY);
        double b = computeLength(_bX + _dX - pX, _bY + _dY - pY);
        double s = (a + b + _length)/2.0f;
        double h = _length/2.0f*Math.sqrt(s*(s-a)*(s-b)*(s-_length));
        return h;
    }

    public static double computeAngle(Vector2D a, Vector2D b)
    {
        double dotProduct = Vector2D.dotProduct(a, b);
        double magnitudeProduct = a.getLength() * b.getLength();
        double divsionResult = dotProduct/magnitudeProduct;
        return (double)Math.acos(divsionResult);
    }

    private static double dotProduct(Vector2D a, Vector2D b)
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
        _length = computeLength(_dX, _dY);
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
            
            if(0 < t1 && t1 <= 1)
            {
                Vector2D v1 = new Vector2D(x0, y0, t1 * dx, t1 * dy);
                result.add(v1);
            }
            if(0 < t2 && t2 <= 1)
            {
                Vector2D v2 = new Vector2D(x0, y0, t2 * dx, t2 * dy);
                result.add(v2);
            }
        }
        return result;
    }

    /**
     * Create a copy shifted orthogonally. (negative means right, positive means left)
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
}
