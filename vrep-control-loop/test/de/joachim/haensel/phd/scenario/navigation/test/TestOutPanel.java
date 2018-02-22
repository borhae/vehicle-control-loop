package de.joachim.haensel.phd.scenario.navigation.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.util.Arrays;

import javax.swing.JPanel;

import de.joachim.haensel.phd.scenario.math.bezier.Spline2D;

public class TestOutPanel extends JPanel
{
    float travelled = 0.0f;
    float travelStep = (float) Math.PI;
    private float[][] _points;
    private Spline2D _spline;

    public TestOutPanel(float[][] points, Spline2D spline)
    {
        _points = points;
        _spline = spline;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        g.setColor(SystemColor.control);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        // draw full spline

        g.setColor(Color.BLUE);

        float d = 0.0f;
        while (d < _points.length)
        {
            float[] at = _spline.getPositionAt(d);

            this.drawCircle(g, at[0], at[1], 2);

            d += 0.02f;
        }
        
        g.setColor(Color.GREEN);
        
        Arrays.asList(_points).stream().forEach(point -> drawCircle(g, point[0], point[1], 3));
        travelled += travelStep;
        repaint();
    }

    private void drawCircle(Graphics g, float x, float y, int r)
    {
        g.fillOval((int) x - r, (int) y - r, r * 2, r * 2);
    }
}
