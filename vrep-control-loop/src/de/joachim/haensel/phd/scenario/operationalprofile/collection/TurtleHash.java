package de.joachim.haensel.phd.scenario.operationalprofile.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
    private double _gridSize;
    private int _elementLength;
    private int _numOfElems;
    private int _gridWidth;
    private int _gridHeight;
    private int _offsetX;
    private int _offsetY;

    public TurtleHash(double gridSize, double elementLength, int numOfElems)
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
        List<Vector2D> cleanedVectors = cleanVectors(centeredTrj);
        List<int[]> pixels = new ArrayList<int[]>();
        int[][] lastRasterizedVec = null;
        Vector2D lastVector = null;
        for(int trajIdx = 0; trajIdx < cleanedVectors.size(); trajIdx++)
        {
            Vector2D curV = cleanedVectors.get(trajIdx);
            int[][] rasterizedV = rasterizeVectorBresenham(curV);
            validateVector(rasterizedV);
            if(rasterizedV.length <= 0)
            {
                continue;
            }
            int rasterIdx = 0;
            int[] pLast = pixels.isEmpty() ? null : pixels.get(pixels.size() - 1);
            int[] pNext = rasterizedV[0];
            if(!pixels.isEmpty() && !connected(pLast, pNext))
            {
                int filler[][] = rasterizeVectorBresenham(new Vector2D(new Position2D(asDouble(pLast)), new Position2D(asDouble(pNext))));
                int size = rasterizedV.length + filler.length;
                List<int[]> tmp = new ArrayList<int[]>(size);
                for(int idx = 0; idx < size; idx++)
                {
                    if(idx == filler.length)
                    {
                        if((idx > 0) && same(tmp.get(idx - 1), rasterizedV[idx - filler.length]))
                        {
                            continue;
                        }
                    }
                    if(idx < filler.length)
                    {
                        tmp.add(filler[idx]);
                    }
                    else
                    {
                        tmp.add(rasterizedV[idx - filler.length]);
                    }
                }
                rasterizedV = tmp.toArray(new int[0][0]);
            }
            pNext = rasterizedV[0];
            if(!pixels.isEmpty() && same(pLast, pNext))
            {
                rasterIdx = 1;
            }
            for(; rasterIdx < rasterizedV.length; rasterIdx++)
            {
                int[] curPoint = rasterizedV[rasterIdx];
                pixels.add(curPoint);
            }
            validateVector(pixels.toArray(new int[0][0]));
            lastVector = curV;
            lastRasterizedVec = rasterizedV;
        }
        pixels.parallelStream().forEach(p -> {p[0] += _offsetX; p[1] += _offsetY;});
        return pixels;
    }

    private List<Vector2D> cleanVectors(List<TrajectoryElement> centeredTrj)
    {
        List<Vector2D> vectors = centeredTrj.stream().map(t -> t.getVector()).collect(Collectors.toList());
        List<Vector2D> result = new ArrayList<Vector2D>();
        for(int idx = 0; idx < vectors.size() - 1; idx++)
        {
            Position2D b1 = vectors.get(idx).getBase();
            Position2D b2 = vectors.get(idx + 1).getBase();
            result.add(new Vector2D(b1, b2));
        }
        result.add(vectors.get(vectors.size() - 1));
        return result;
    }
    
    public int[][] rasterizeVectorBresenham(Vector2D vector)
    {
        Position2D p1 = vector.getBase();
        Position2D p2 = vector.getTip();
        double x0 = p1.getX();
        double y0 = p1.getY();
        double x1 = p2.getX();
        double y1 = p2.getY();
        double dx = Math.abs(vector.getdX());
        double dy = Math.abs(vector.getdY());
        
        int xInc = x0 < x1 ? 1 : -1;
        int yInc = y0 < y1 ? 1 : -1;
        
        int x = (int)Math.round(x0);
        int y = (int)Math.round(y0);
        
        double error = 0.0;
        
        List<int[]> result = new ArrayList<int[]>();
        if(dx >= dy)
        {
            error = dx / 2.0;
            while(x != Math.round(x1))
            {
                result.add(new int[] {x, y});
                x += xInc;
                error -= dy;;
                if(error < 0.0)
                {
                    y += yInc;
                    error += dx;
                }
            }
        }
        else
        {
            error = dy / 2.0;
            while(y != Math.round(y1))
            {
                result.add(new int[] {x, y});
                y += yInc;
                error -= dx;
                if(error < 0.0)
                {
                    x += xInc;
                    error += dy;
                }
            }
        }
        return result.toArray(new int[0][0]);
    }

    private void validateVector(int[][] rasterizedV)
    {
        for(int idx = 0; idx < rasterizedV.length - 1; idx++)
        {
            int[] p1 = rasterizedV[idx];
            int[] p2 = rasterizedV[idx + 1];
            if(!connected(p1, p2) || same(p1, p2))
            {
                System.out.println("shit");
            }
        }
    }

    private int[] lastPixelOffsetRemoved(List<int[]> pixels)
    {
        if(pixels.isEmpty())
        {
            return null;
        }
        else
        {
            int[] base = pixels.get(pixels.size() - 1);
            int[] result = new int[2];
            result[0] = base[0] - _offsetX;
            result[1] = base[1] - _offsetY;
            return result;
        }
    }

    private double[] asDouble(int[] p)
    {
        return new double[] {(double) p[0], (double) p[1]};
    }

    public static boolean connected(int[] p1, int[] p2)
    {
        int dx = Math.abs(p2[0] - p1[0]);
        int dy = Math.abs(p2[1] - p1[1]);
        return dx <= 1 && dy <= 1;
    }

    public static boolean same(int[] p1, int[] p2)
    {
        return p1[0] == p2[0] && p1[1] == p2[1];
    }
    
    public int[][] rasterizeVectorAmanatidesWoo(Vector2D vector)
    {
        Position2D p1 = vector.getBase();
        Position2D p2 = vector.getTip();
        double x0 = fix(p1.getX());
        double y0 = fix(p1.getY());
        double x1 = fix(p2.getX());
        double y1 = fix(p2.getY());
        int x = (int) x0;
        int y = (int) y0;
        int endX = (int) x1;
        int endY = (int) y1;
        double gridCellWidth = 1.0;
        double gridCellHeight = 1.0;

        // deltaX and deltaY is how far we have to move in ray direction until we find a new cell in x or y direction 
        // y = u + t * v, where u=(x1,x2) and v=(stepX,stepY) is the direction vector 
        double deltaX = gridCellWidth / Math.abs(x1 - x0);
        int stepX = (int) Math.signum(x1 - x0);
        double maxX = deltaX * (1.0 - (frac(x0 / gridCellWidth)));
        
        double deltaY = gridCellHeight / Math.abs(y1 - y0);
        int stepY = (int) Math.signum(y1 - y0);
        double maxY = deltaY * (1.0 - (frac(y0 / gridCellHeight)));
        
        boolean reachedX = false;
        boolean reachedY = false;
        
        List<int[]> result = new ArrayList<int[]>();
        result.add(new int[] {x, y});
        while(!(reachedX && reachedY))
        {
            if((maxX < maxY) && !reachedX)
            {
                maxX += deltaX;
                x += stepX;
            }
            else
            {
                maxY += deltaY;
                y += stepY;
            }
            result.add(new int[]{x, y});
            if(stepX > 0.0)
            {
                reachedX = x >= endX;
            }
            else if (x <= endX)
            {
                reachedX = true;
            }
            if(stepY > 0.0)
            {
                reachedY = y >= endY;
            }
            else if(y <= endY)
            {
                reachedY = true;
            }
        }
        return result.toArray(new int[0][0]);
    }
    
    static final double fix(double val) 
    { 
        //why not val % 1 == 0 ? val + 0.1 : val;
        return frac(val) == 0 ? val + 0.1 : val; 
    } 
 
    static final double frac(double val) 
    { 
        return val - (int)val; 
    }

    public int[][] rasterizeVectorSimple(Vector2D vector)
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
        result[idx][0] = x;
        result[idx][1] = y;
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
