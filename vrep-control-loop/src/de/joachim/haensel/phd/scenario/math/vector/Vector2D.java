package de.joachim.haensel.phd.scenario.math.vector;

import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class Vector2D
{
    private double _bX; //baseX
    private double _bY; //baseY
    private double _length;
    private double _dX; // direction x 
    private double _dY; // direction y 
    private double _normX; //normalized x
    private double _normY; // normalized y
    
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
     * @param stepSize to be cut away
     * @return a vector with the same direction as this one with length length
     */
    public Vector2D cutLengthFrom(double stepSize)
    {
        double newBX = _bX;
        double newBY = _bY;
        double newDX = _normX * stepSize;
        double newDY = _normY * stepSize;
        Vector2D cut = new Vector2D(newBX, newBY, newDX, newDY);
        _bX = _bX + stepSize * _normX;
        _bY = _bY + stepSize * _normY;
        _dX = _dX - newDX;
        _dY = _dY - newDY;
        _length = (double)Math.sqrt(_dX * _dX + _dY * _dY);
        if(Double.isNaN(_bX) || Double.isNaN(_bY) || Double.isNaN(_length))
        {
            System.out.println("we got some not a numbers!!");
        }
        return cut;
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

    public double getLength()
    {
        return computeLength(_dX, _dY);
    }

    public Position2D getNorm()
    {
        return new Position2D(_normX, _normY);
    }
}
