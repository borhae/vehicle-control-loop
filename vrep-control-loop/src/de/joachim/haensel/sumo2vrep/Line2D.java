package de.joachim.haensel.sumo2vrep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Line2D
{
    private float _x1;
    private float _y1;
    private float _x2;
    private float _y2;
    private float _c;

    public Line2D(String p1, String p2)
    {
        String[] coordinate1 = p1.split(",");
        _x1  = Float.valueOf(coordinate1[0]);
        _y1  = Float.valueOf(coordinate1[1]);
        String[] coordinate2 = p2.split(",");
        _x2  = Float.valueOf(coordinate2[0]);
        _y2  = Float.valueOf(coordinate2[1]);
        _c = Position2D.distance(_x1, _y1, _x2, _y2);
    }
    
    public Line2D(float x1, float y1, float x2, float y2)
    {
        _x1 = x1;
        _y1 = y1;
        _x2 = x2;
        _y2 = y2;
        _c = Position2D.distance(_x1, _y1, _x2, _y2);
    }

    public float length(float x1, float y1, float x2, float y2)
    {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public float distance(Position2D position)
    {
        float pX = position.getX();
        float pY = position.getY();
        float a = length(pX, pY, _x1, _y1);
        float b = length(_x2, _y2, pX, pY);
        float s = (a + b + _c)/2.0f;
        float h = (float) (_c/2.0f*Math.sqrt(s*(s-a)*(s-b)*(s-_c)));
        return h;
    }
    
    public float length()
    {
        return _c;
    }

    public static List<Line2D> createLines(String shape)
    {
        ArrayList<Line2D> result = new ArrayList<Line2D>();
        createLines(Arrays.asList(shape.split(" ")), result);
        return result;
    }

    private static void createLines(List<String> coordinateList, List<Line2D> result)
    {
        if(coordinateList.size() <= 1)
        {
            // a line needs at least two coordinates
            return;
        }
        else
        {
            result.add(new Line2D(coordinateList.get(0), coordinateList.get(1)));
            if(coordinateList.size() >= 3)
            {
                createLines(coordinateList.subList(1, coordinateList.size()), result);
            }
        }
    }

    public float getX1()
    {
        return _x1;
    }

    public float getY1()
    {
        return _y1;
    }

    public float getX2()
    {
        return _x2;
    }

    public float getY2()
    {
        return _y2;
    }
}
