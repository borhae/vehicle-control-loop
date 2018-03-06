package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.sumo2vrep.Position2D;

public class RoutePanel extends ScrollableMovablePanel
{
    private List<Vector2D> _content;
    private Vector2D _curVector;

    @Override
    protected void drawInternal(Graphics2D g2)
    {
        g2.setColor(Color.BLACK);
        List<Vector2D> localCopy = new ArrayList<Vector2D>(_content);
        localCopy.stream().forEach(v -> drawVector(g2, v, 1.0f));
        
        
        g2.setColor(Color.RED);
        drawVector(g2, new Vector2D(_curVector), 2.0f);
    }

    private void drawVector(Graphics2D g2, Vector2D v, float scale)
    {
        v.mul(scale);
        Position2D base = v.getBase();
        Position2D tip = v.getTip();
//        drawArrowLine(g2, (int)base.getX(), (int)base.getY(), (int)tip.getX(), (int)tip.getY(), 2, 2);
        int x1 = (int)base.getX() ;
        int y1 = (int)base.getY() ;
        int x2 = (int)tip.getX() ;
        int y2 = (int)tip.getY() ;
        g2.drawLine(x1, y1, x2, y2);
        int r = 2;
        g2.drawOval(x1 - r, y1 - r, r, r);
    }

//    private void drawVector(Graphics2D g2, Vector2D v)
//    {
//        float scale = 2f;
//        Position2D base = v.getBase();
//        Position2D tip = v.getTip();
////        drawArrowLine(g2, (int)base.getX(), (int)base.getY(), (int)tip.getX(), (int)tip.getY(), 2, 2);
//        int x1 = (int)(base.getX() * scale);
//        int y1 = (int)(base.getY() * scale);
//        int x2 = (int)(tip.getX() * scale);
//        int y2 = (int)(tip.getY() * scale);
//        g2.drawLine(x1, y1, x2, y2);
//        int r = 2;
//        g2.drawOval(x1 - r, y1 - r, r, r);
//    }

    public void setRouteAndVector(List<Vector2D> route, Vector2D curVector)
    {
        _content = route;
        _curVector = curVector;
        repaint();
    }
    
    /**
     * Draw an arrow line between two points.
     * @param g the graphics component.
     * @param x1 x-position of first point.
     * @param y1 y-position of first point.
     * @param x2 x-position of second point.
     * @param y2 y-position of second point.
     * @param d  the width of the arrow.
     * @param h  the height of the arrow.
     */
    private void drawArrowLine(Graphics g, int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
    }
}