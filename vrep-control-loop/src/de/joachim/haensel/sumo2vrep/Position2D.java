package de.joachim.haensel.sumo2vrep;

import coppelia.FloatWA;

public class Position2D
{
    private float _x;
    private float _y;

    public Position2D(float x, float y)
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
    
    public float getX()
    {
        return _x;
    }

    public float getY()
    {
        return _y;
    }
    
    public void setX(float x)
    {
        _x = x;
    }

    public void setY(float y)
    {
        _y = y;
    }
    
    public void setXY(float[] xy)
    {
        _x = xy[0];
        _y = xy[1];
    }
    
    public void setXY(float x, float y)
    {
        _x = x;
        _y = y;
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
    
    public static Position2D between(Position2D p1, Position2D p2)
    {
        return new Position2D((p2._x  + p1._x)/2.0f, (p2._y + p1._y)/2.0f);
    }

    public boolean equals(Position2D other, double epsilon)
    {
        return distance(other) <= epsilon;
    }
    
    public float distance(String coordinates)
    {
        return distance(this, new Position2D(coordinates));
    }

    public double distance(Position2D other)
    {
        return distance(this, other);
    }

    public static boolean equals(Position2D p1, Position2D p2, float epsilon)
    {
        return distance(p1, p2) < epsilon;
    }

    public static float distance(Position2D p1, Position2D p2)
    {
        float dx = p2._x - p1._x;
        float dy = p2._y - p2._y;
        
        return (float)Math.sqrt(dx*dx + dy*dy);
    }

    @Override
    public String toString()
    {
        return "p:(" + _x  + ", " + _y + ")";
    }
}
