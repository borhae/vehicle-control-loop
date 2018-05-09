package de.joachim.haensel.phd.scenario.math.geometry;

import java.awt.Graphics2D;

import coppelia.FloatWA;
import de.joachim.haensel.phd.scenario.math.TMatrix;

public class Position2D
{
    private double _x;
    private double _y;

    public Position2D(double x, double y)
    {
        _x = x;
        _y = y;
    }

    public Position2D(String coordinatesAsString)
    {
        String[] coordinates = coordinatesAsString.split(",");
        _x  = Float.valueOf(coordinates[0]);
        _y  = Float.valueOf(coordinates[1]);
    }

    public Position2D(FloatWA pos3d)
    {
        _x = pos3d.getArray()[0];
        _y = pos3d.getArray()[1];
    }
    
    public double getX()
    {
        return _x;
    }

    public double getY()
    {
        return _y;
    }
    
    public void setX(double x)
    {
        _x = x;
    }

    public void setY(double y)
    {
        _y = y;
    }
    
    public void setXY(float[] xy)
    {
        _x = xy[0];
        _y = xy[1];
    }
    
    public void setXY(double x, double y)
    {
        _x = x;
        _y = y;
    }
    
    /**
     * The point between p1 and p2
     * @param p1
     * @param p2
     * @return
     */
    public static Position2D between(Position2D p1, Position2D p2)
    {
        return new Position2D((p2._x  + p1._x)/2.0f, (p2._y + p1._y)/2.0f);
    }

    public boolean equals(Position2D other, double epsilon)
    {
        return distance(other) <= epsilon;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if(other instanceof Position2D)
        {
            return equals((Position2D)other, Math.ulp(0.0));
        }
        else
        {
            return false;
        }
    }

    public Position2D mul(double scaleFactor)
    {
        _x *= scaleFactor;
        _y *= scaleFactor;
        return this;
    }

    public Position2D transform(TMatrix transformationMatrix)
    {
        double[] result = transformationMatrix.transform(_x, _y);
        _x = result[0];
        _y = result[1];
        return this;
    }

    public String toSumoString()
    {
        return _x + "," + _y ;
    }

    public Point3D toPoint3D()
    {
        return new Point3D(_x, _y);
    }
    
    public double distance(String coordinates)
    {
        return distance(this, new Position2D(coordinates));
    }
    
    public double distance(double x, double y)
    {
        return distance(this._x, this._y, x, y);
    }
    
    public double distance(Position2D other)
    {
        return distance(this, other);
    }

    @Override
    public String toString()
    {
        return "p:(" + _x  + ", " + _y + ")";
    }
    
    public static Position2D[] valueOf(String[] coordinates)
    {
        Position2D[] result = new Position2D[coordinates.length];
        for (int idx = 0; idx < coordinates.length; idx++)
        {
            result[idx] = new Position2D(coordinates[idx]);
        }
        return result;
    }
    
    public static Position2D[] valueOfString(String coords)
    {
        return valueOf(coords.split(" "));
    }

    /**
     * Search for the closest position in the input array regarding the reference position. 
     * @param reference
     * @param positionsToLookIn
     * @return Index in incoming array of closest position. -1 if input data is not valid
     */
    public static int getClosestIdx(Position2D reference, Position2D[] positionsToLookIn)
    {
        if(reference == null || positionsToLookIn == null || positionsToLookIn.length == 0)
        {
            return -1;
        }
        int closestIdx = 0;
        double curMinimumDistance = Double.MAX_VALUE;
        for (int idx = 0; idx < positionsToLookIn.length; idx++)
        {
            Position2D curPosition = positionsToLookIn[idx];
            double curDistance = distance(reference, curPosition);
            if(curDistance < curMinimumDistance)
            {
                closestIdx = idx;
            }
        }
        return closestIdx;
    }

    public static boolean equals(Position2D p1, Position2D p2, double epsilon)
    {
        return distance(p1, p2) < epsilon;
    }

    public static double distance(Position2D p1, Position2D p2)
    {
        return distance(p1._x,  p1._y, p2._x, p2._y);
    }

    public static double distance(double x1, double y1, double x2, double y2)
    {
        double dx = x2 - x1;
        double dy = y2 - y1;
        
        return Math.sqrt(dx*dx + dy*dy);
    }

    public static Position2D minus(Position2D a, Position2D b)
    {
        return new Position2D(a._x - b._x, a._y - b._y);
    }
    
    public static Position2D plus(Position2D a, Position2D b)
    {
        return new Position2D(a._x + b._x, a._y + b._y);
    }

    public static double crossProduct2D(Position2D a, Position2D b)
    {
        return a._x * b._y - a._y * b._x;
    }

    public static Position2D multiply(double mult, Position2D pos)
    {
        return new Position2D(pos._x * mult, pos._y * mult);
    }

    public Position2D copy()
    {
        return new Position2D(_x, _y);
    }

    public void normalize()
    {
        double length = distance(0.0, 0.0);
        if(length != 0.0)
        {
            _x = _x / length;
            _y = _y / length;
        }
    }
}
