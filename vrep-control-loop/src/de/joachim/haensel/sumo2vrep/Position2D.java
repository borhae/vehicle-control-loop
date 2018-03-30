package de.joachim.haensel.sumo2vrep;

import coppelia.FloatWA;

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
    
    public static Position2D[] valueOf(String[] coordinates)
    {
        Position2D[] result = new Position2D[coordinates.length];
        for (int idx = 0; idx < coordinates.length; idx++)
        {
            result[idx] = new Position2D(coordinates[idx]);
        }
        return result;
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

    @Override
    public String toString()
    {
        return "p:(" + _x  + ", " + _y + ")";
    }

    public Position2D mul(double scaleFactor)
    {
        _x *= scaleFactor;
        _y *= scaleFactor;
        return this;
    }
}
