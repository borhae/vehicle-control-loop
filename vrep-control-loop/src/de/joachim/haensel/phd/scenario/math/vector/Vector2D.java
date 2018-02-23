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
        _length = (float)Math.sqrt(dirX * dirX + dirY * dirY);
        _dX = dirX;
        _dY = dirY;
        _normX = _dX / _length;
        _normY = _dY / _length;
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
}
