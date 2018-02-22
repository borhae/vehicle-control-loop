package de.joachim.haensel.phd.scenario.math.bezier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.SystemColor;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CheckOutSpline2D
{

    public static void main(String[] args)
    {
        final float[][] points = new float[16][2];
        Random r = new Random(123456L);
        for (int i = 0; i < points.length; i++)
        {
            points[i][0] = 32 + r.nextInt(384);
            points[i][1] = 32 + r.nextInt(384);
        }

        final Spline2D growingSpline = new Spline2D(points);
        final Spline2D fixedSpline = new Spline2D(points);
        growingSpline.enabledTripCaching(16.0f, 0.001f);
        fixedSpline.enabledTripCaching(16.0f, 0.001f);

        for (int i = 0; i < 8; i++)
        {
            while (fixedSpline.getTravelCache().size() > 1)
                fixedSpline.getTravelCache().remove(fixedSpline.getTravelCache().size() - 1);
            long t1 = System.currentTimeMillis();
            fixedSpline.getTripPosition(4000.0f);
            long t2 = System.currentTimeMillis();
            System.out.println("full trip took: " + (t2 - t1) + "ms");
        }

        JPanel panel = new JPanel() {
            float travelled = 0.0f;
            float travelStep = (float) Math.PI;

            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);

                g.setColor(SystemColor.control);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());

                // draw full spline

                g.setColor(Color.BLUE);

                float d = 0.0f;
                while (d < points.length)
                {
                    float[] at = fixedSpline.getPositionAt(d);

                    this.drawCircle(g, at[0], at[1], 2);

                    d += 0.002f;
                }

                // draw GROWING cache
                g.setColor(Color.RED);

                for (CacheItem item : growingSpline.getTravelCache())
                {
                    this.drawCircle(g, item._xpos, item._ypos, 3);
                }

                // draw GROWING spline
                g.setColor(Color.GREEN);

                float[] xy;
                for (int i = 0; i < 25; i++)
                {
                    xy = growingSpline.getTripPosition(this.travelled * i / 25.0f);
                    this.drawCircle(g, xy[0], xy[1], 3);
                }

                this.travelled += this.travelStep;

                this.repaint();
            }

            private void drawCircle(Graphics g, float x, float y, int r)
            {
                g.fillOval((int) x - r, (int) y - r, r * 2, r * 2);
            }
        };
        panel.setPreferredSize(new Dimension(512, 512));
        JFrame frame = new JFrame();
        JPanel cp = (JPanel) frame.getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
