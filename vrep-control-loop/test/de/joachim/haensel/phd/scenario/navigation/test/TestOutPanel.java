package de.joachim.haensel.phd.scenario.navigation.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import de.joachim.haensel.phd.scenario.math.bezier.Spline2D;
import de.joachim.haensel.phd.scenario.math.vector.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.Trajectory;
import de.joachim.haensel.sumo2vrep.Position2D;

public class TestOutPanel extends JPanel
{
    float travelled = 0.0f;
    float travelStep = (float) Math.PI;
    private float[][] _points;
    private Spline2D _spline;
    private float _scale;
    private List<Trajectory> _trajectories;

    public TestOutPanel(float[][] points, Spline2D spline, float scale)
    {
        _points = points;
        _spline = spline;
        _scale = scale;
    }

    public TestOutPanel(float[][] points, List<Trajectory> trajectory, float scale)
    {
        _points = points;
        _trajectories = trajectory;
        _scale = scale;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        g.setColor(SystemColor.control);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        // draw full spline

        g.setColor(Color.BLUE);
        
        if(_spline != null)
        {
            drawSpline(g);
        }
        if(_trajectories != null)
        {
            drawTrajectories(g);
        }
        g.setColor(Color.GREEN);
        
        Arrays.asList(_points).stream().forEach(point -> drawFilledCircle(g, point[0] * _scale, point[1] * _scale, 3));
        travelled += travelStep;
        repaint();
    }

    private void drawTrajectories(Graphics g)
    {
        _trajectories.stream().forEach(traj -> drawTrajectory(g, traj));
    }

    private void drawTrajectory(Graphics g, Trajectory t)
    {
        Vector2D v = t.getVector();
        Position2D b = v.getBase();
        Position2D d = v.getDir();
        g.drawLine((int)(b.getX() * _scale), (int)(b.getY() * _scale), (int)((b.getX() + d.getX())* _scale), (int)((b.getY() + d.getY())* _scale));
    }

    private void drawSpline(Graphics g)
    {
        float d = 0.0f;
//        float delta = 0.02f;
        float delta = 0.08f;
        while (d < _points.length)
        {
            float[] at = _spline.getPositionAt(d);

            this.drawCircle(g, at[0] * _scale, at[1] * _scale, 10);

            d += delta;
        }
    }

    private void drawFilledCircle(Graphics g, float x, float y, int r)
    {
        g.fillOval((int) x - r, (int) y - r, r * 2, r * 2);
    }

    private void drawCircle(Graphics g, float x, float y, int r)
    {
        g.drawOval((int) x - r, (int) y - r, r * 2, r * 2);
    }
}
