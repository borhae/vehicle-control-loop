package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import de.joachim.haensel.phd.scenario.equivalenceclasses.TrajectoryNormalizer;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.vehicle.navigation.TrajectoryElement;

/**
 * Incoming trajectories have for example 25 elements of 5 meters length each.
 * This results in trajectories of max 125 meters length. The coverable area lies roughly in a box of 250 x 250 Meters
 * Trajectories are indexed into a grid as defined by gridsize (parameter of hash)
 * @author dummy
 *
 */
public class TurtleHash
{
    private int _gridSize;
    private int _elementLength;
    private int _numOfElems;
    private int _gridWidth;
    private int _gridHeight;
    private int _offsetX;
    private int _offsetY;

    public TurtleHash(int gridSize, double elementLength, int numOfElems)
    {
        _gridSize = gridSize;
        _elementLength = (int)Math.round(elementLength);
        _numOfElems = numOfElems;
        _gridWidth = (int)((elementLength * ((double)numOfElems) * 2.0) / ((double)gridSize));
        _gridHeight = _gridWidth;
        _offsetX = _gridWidth / 2;
        _offsetY = _gridHeight / 2;
    }

    public List<int[]> pixelate(List<TrajectoryElement> trajectory)
    {
        List<TrajectoryElement> centeredTrj = TrajectoryNormalizer.normalize(trajectory);
        centeredTrj.stream().forEach(trajE -> trajE.setVector(trajE.getVector().scale(1.0/(double)_gridSize)));
        List<Vector2D> cleanedVectors = cleanOutConnections(centeredTrj);
        List<int[]> pixels = new ArrayList<int[]>();
        for(int trajIdx = 0; trajIdx < cleanedVectors.size(); trajIdx++)
        {
            Vector2D curV = cleanedVectors.get(trajIdx);
            int[][] rasterizedV = rasterizeVector(curV);
            int rasterIdx = 0;
            if(!pixels.isEmpty() && same(pixels.get(pixels.size() - 1), rasterizedV[0]))
            {
                rasterIdx = 1;
            }
            System.out.println("");
            for(; rasterIdx < rasterizedV.length; rasterIdx++)
            {
                int[] curPoint = rasterizedV[rasterIdx];
                pixels.add(curPoint);
            }
        }
        return pixels;
    }

    private boolean same(int[] p1, int[] p2)
    {
        return p1[0] == p2[0] && p1[1] == p2[1];
    }

    private List<Vector2D> cleanOutConnections(List<TrajectoryElement> trajectory)
    {
        List<Vector2D> result = new ArrayList<Vector2D>();
        for(int idx = 0; idx < trajectory.size() - 1; idx++)
        {
            Vector2D curV = trajectory.get(idx).getVector();
            Vector2D nexV = trajectory.get(idx + 1).getVector();
            result.add(new Vector2D(curV.getBase(), nexV.getBase()));
        }
        result.add(trajectory.get(trajectory.size() - 1).getVector()); // add the last directly since we have no next base...
        return result;
    }

    public int[][] rasterizeVector(Vector2D vector)
    {
        int[][] result = null;
        Position2D p1 = vector.getBase();
        Position2D p2 = vector.getTip();
        double x0 = p1.getX();
        double y0 = p1.getY();
        double x1 = p2.getX();
        double y1 = p2.getY();
        double dx = vector.getdX();
        double dy = vector.getdY();
        if(Math.abs(dx) == 0) // line is vertical, will not cover more than one pixel
        {
            int[] yRange = range(y0, y1);
            result = new int[yRange.length][2];
            for(int idx = 0; idx < yRange.length; idx++)
            {
                int y = yRange[idx];
                int x = (int)x0;
                enterPixel(result, idx, y, x);
            }
        }
        else
        {
            double m = dy / dx;
            double b = y0 - m * x0;
            if(m <= 1 && m >= -1)
            {
                int[] xRange = range(x0, x1);
                result = new int[xRange.length][2];
                for(int idx = 0; idx < xRange.length; idx++)
                {
                    int x = xRange[idx];
                    int y = (int)(Math.round(m * x + b));
                    enterPixel(result, idx, y, x);
                }
            }
            else
            {
                int[] yRange = range(y0, y1);
                result = new int[yRange.length][2];
                for(int idx = 0; idx < yRange.length; idx++)
                {
                    int y = yRange[idx];
                    int x = (int)Math.round((y - b)/m);
                    enterPixel(result, idx, y, x);
                }
            }
        }
        return result;
    }

    private void enterPixel(int[][] result, int idx, int y, int x)
    {
        result[idx][0] = _offsetX + x;
        result[idx][1] = _offsetY + y;
    }

    private int[] range(double i0, double i1)
    {
        int start = (int) Math.round(i0);
        int end = (int) Math.round(i1);
        boolean increase = start <= end;
        int size = Math.abs(start - end);
        int[] result = new int[size];
        for(int idx = 0; idx < result.length; idx++)
        {
            if(increase)
            {
                result[idx] = start + idx;
            }
            else
            {
                result[idx] = start - idx;
            }
        }
        return result;
    }

    public List<StepDirection> createSteps(List<int[]> pixels)
    {
        List<StepDirection> result = new ArrayList<StepDirection>();
        pixels.stream().reduce((p1, p2) -> {result.add(StepDirection.get(p1, p2)); return p2;});
        return result;
    }
}
