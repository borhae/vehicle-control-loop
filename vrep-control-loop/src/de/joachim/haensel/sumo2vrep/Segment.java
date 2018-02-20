package de.joachim.haensel.sumo2vrep;

public class Segment
{
    private float _x1;
    private float _y1;
    private float _x2;
    private float _y2;
    private float _c;

    public Segment(String p1, String p2)
    {
        String[] coordinates1 = p1.split(",");
        _x1  = Float.valueOf(coordinates1[0]);
        _y1  = Float.valueOf(coordinates1[1]);
        String[] coordinates2 = p2.split(",");
        _x2  = Float.valueOf(coordinates2[0]);
        _y2  = Float.valueOf(coordinates2[1]);
        _c = length(_x1, _y1, _x2, _y2);
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
}
