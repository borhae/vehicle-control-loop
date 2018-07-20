package de.joachim.haensel.phd.scenario.math.geometry;

public class Point3D
{
    private double _x;
    private double _y;
    private double _z;

    public Point3D(double x, double y, double z)
    {
        _x = x;
        _y = y;
        _z = z;
    }

    /**
     * Z-Zero-Constructor for convenience
     * @param x
     * @param y
     */
    public Point3D(double x, double y)
    {
        this(x, y, 0.0);
    }

    public Point3D(Position2D pos2d)
    {
        this(pos2d.getX(), pos2d.getY());
    }

    public Point3D(Position2D pos2D, double z)
    {
        this(pos2D.getX(), pos2D.getY(), z);
    }

    public double[] getArray()
    {
        return new double[]{_x, _y, _z};
    }

    public static Point3D crossProduct(Point3D a, Point3D b)
    {
        double x = a._y * b._z - a._z * b._y;
        double y = a._z * b._x - a._x * b._z;
        double z = a._x * b._y - a._y * b._x;
        return new Point3D(x, y, z);
    }

    @Override
    public boolean equals(Object other)
    {
        if(!(other instanceof Point3D))
        {
            return false;
        }
        Point3D otherPoint3d = (Point3D)other;
        
        return ((_x == otherPoint3d._x) && (_y == otherPoint3d._y) && (_z == otherPoint3d._z));
    }

    @Override
    public String toString()
    {
        return "(" + _x + ", " + _y + ", " + _z + ")";
    }

    public double getZ()
    {
        return _z;
    }
}
