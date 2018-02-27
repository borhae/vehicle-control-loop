package de.joachim.haensel.phd.scenario.math.vector;

import de.joachim.haensel.sumo2vrep.Line2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class Vector2D
{
    private float _bX; //baseX
    private float _bY; //baseY
    private float _length;
    private float _dX; // direction x 
    private float _dY; // direction y 
    private float _normX; //normalized x
    private float _normY; // normalized y

    public Vector2D(Line2D line)
    {
        _bX = line.getX1();
        _bY = line.getY1();
        _length = line.length();
        if(Float.isNaN(_bX) || Float.isNaN(_bY) || Float.isNaN(_length))
        {
            System.out.println("we got some not a numbers!!");
        }
        _dX = line.getX2() - line.getX1();
        _dY = line.getY2() - line.getY1();
        _normX = _dX / _length;
        _normY = _dY / _length;
    }

    public Vector2D(float baseX, float baseY, float dirX, float dirY)
    {
        _bX = baseX;
        _bY = baseY;
        if(Float.isNaN(_bX) || Float.isNaN(_bY) || Float.isNaN(_length))
        {
            System.out.println("we got some not a numbers!!");
        }
        _length = computeLength(dirX, dirY);
        _dX = dirX;
        _dY = dirY;
        _normX = _dX / _length;
        _normY = _dY / _length;
    }

    private float computeLength(float dirX, float dirY)
    {
        return (float)Math.sqrt(dirX * dirX + dirY * dirY);
    }

    public Vector2D(Position2D base, Position2D tip)
    {
        this(base.getX(), base.getY(), tip.getX() - base.getX(), tip.getY() - base.getY());
    }

    public Vector2D(Vector2D v)
    {
        this(v.getBase(), v.getTip());
    }

    public float length()
    {
        return _length;
    }

    public Position2D getBase()
    {
        return new Position2D(_bX, _bY);
    }

    /**
     * Cuts a vector of length length from this vector. this vector is shortened by that
     * @param length
     * @return
     */
    public Vector2D cutLengthFrom(float length)
    {
        float newBX = _bX;
        float newBY = _bY;
        float newDX = _normX * length;
        float newDY = _normY * length;
        Vector2D cut = new Vector2D(newBX, newBY, newDX, newDY);
        _bX = _bX + length * _normX;
        _bY = _bY + length * _normY;
        _dX = _dX - newDX;
        _dY = _dY - newDY;
        _length = (float)Math.sqrt(_dX * _dX + _dY * _dY);
        if(Float.isNaN(_bX) || Float.isNaN(_bY) || Float.isNaN(_length))
        {
            System.out.println("we got some not a numbers!!");
        }
        return cut;
    }

    public static float computeAngle(Vector2D a, Vector2D b)
    {
        float dotProduct = Vector2D.dotProduct(a, b);
        float magnitudeProduct = a.length() * b.length();
        return (float)Math.cos(dotProduct/magnitudeProduct);
    }

    private static float dotProduct(Vector2D a, Vector2D b)
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
        return "(" + _bX + ", " + _bY + ") -> (" + _dX + ", " + _dY + ") l: " + _length; 
    }

    public Position2D getTip()
    {
        return new Position2D(_bX + _dX, _bY + _dY);
    }
    
    public static int compBaseX(Vector2D v1, Vector2D v2)
    {
        return Float.compare(v1.getBase().getX(), v2.getBase().getX());
    }
    
    public static int compBaseY(Vector2D v1, Vector2D v2)
    {
        return Float.compare(v1.getBase().getY(), v2.getBase().getY());
    }
    
    public static int compTipX(Vector2D v1, Vector2D v2)
    {
        return Float.compare(v1.getTip().getX(), v2.getTip().getX());
    }
    
    public static int compTipY(Vector2D v1, Vector2D v2)
    {
        return Float.compare(v1.getTip().getY(), v2.getTip().getY());
    }

    public void sub(float x, float y)
    {
        _bX -= x;
        _bY -= y;
    }
    
    public void add(int x, int y)
    {
        _bX += x;
        _bY += y;
    }
    
    public void mul(float f)
    {
        _bX *= f;
        _bY *= f;
        _dX *= f;
        _dY *= f;
        _length = computeLength(_dX, _dY);
    }
}
