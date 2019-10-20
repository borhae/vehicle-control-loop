package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.joachim.haensel.phd.scenario.math.geometry.Point3D;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;
import de.joachim.haensel.phd.scenario.math.geometry.Vector2D;
import de.joachim.haensel.phd.scenario.profile.equivalenceclasses.TrajectoryNormalizer;
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
    
    public String hash(List<TrajectoryElement> trajectory)
    {
        if(trajectory.size() != _numOfElems)
        {
            return String.format("length was %d should be %d", trajectory.size(), _numOfElems);
        }
        List<int[]> pixels = pixelate(trajectory);
        List<Integer> steps = createSteps3D(pixels);
        for(int idx = 0; idx < pixels.size() - 1; idx++)
        {
            int[] cur = pixels.get(idx);
            int[] nxt = pixels.get(idx + 1);
            boolean differentPoints = !TurtleHash.same3D(cur, nxt);
            boolean connectedPoints = TurtleHash.connected3D(cur, nxt);
            if(!differentPoints || !connectedPoints)
            {
                return String.format("consecutive points connected? %b not the same?", connectedPoints, differentPoints);
            }
        }
        String hash = steps.stream().map(d -> TurtleHash.toBase26(d)).collect(Collectors.joining());
        if(hash.equals(""))
        {
            return "to base26 results in empty string";
        }
        return hash;
    }

    public List<int[]> pixelate(List<TrajectoryElement> trajectory)
    {
        trajectory.stream().forEach(trajE -> {trajE.setVector(trajE.getVector().scale(1.0/_gridSize)); trajE.setVelocity(trajE.getVelocity()/_gridSize);});
        List<int[]> pixels = new ArrayList<int[]>();
        int[][] lastRasterizedVec = null;
        TrajectoryElement lastTrjE = null;
        for(int trajIdx = 0; trajIdx < trajectory.size(); trajIdx++)
        {
            TrajectoryElement curTrjE = trajectory.get(trajIdx);
            double lastVelocity = 0.0;
            if(trajIdx == 0)
            {
                lastVelocity = curTrjE.getVelocity();
            }
            else
            {
                lastVelocity = trajectory.get(trajIdx - 1).getVelocity();
            }
            Vector2D curVec = curTrjE.getVector();
            Point3D p1 = new Point3D(curVec.getBase());
            p1.setZ(lastVelocity);
            Point3D p2 = new Point3D(curVec.getTip());
            p2.setZ(curTrjE.getVelocity());
            int[][] rasterizedV = rasterizeVectorBresenham3D(p1, p2);
            validateVector3D(rasterizedV);
            if(rasterizedV.length <= 0)
            {
                continue;
            }
            int rasterIdx = 0;
            int[] pLast = pixels.isEmpty() ? null : pixels.get(pixels.size() - 1);
            int[] pNext = rasterizedV[0];
            if(!pixels.isEmpty() && !connected3D(pLast, pNext))
            {
                int filler[][] = rasterizeVectorBresenham3D(new Point3D(pLast), new Point3D(pNext));
                int size = rasterizedV.length + filler.length;
                List<int[]> tmp = new ArrayList<int[]>(size);
                for(int idx = 0; idx < size; idx++)
                {
                    if(idx == filler.length)
                    {
                        if((idx > 0) && same3D(tmp.get(idx - 1), rasterizedV[idx - filler.length]))
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
            if(!pixels.isEmpty() && same3D(pLast, pNext))
            {
                rasterIdx = 1;
            }
            for(; rasterIdx < rasterizedV.length; rasterIdx++)
            {
                int[] curPoint = rasterizedV[rasterIdx];
                pixels.add(curPoint);
            }
            validateVector3D(pixels.toArray(new int[0][0]));
            lastTrjE = curTrjE;
            lastRasterizedVec = rasterizedV;
        }
        pixels.parallelStream().forEach(p -> {p[0] += _offsetX; p[1] += _offsetY;});
        return pixels;
    }

    public int[][] rasterizeVectorBresenham3D(Point3D p1, Point3D p2)
    {
        double x0 = p1.getX();
        double y0 = p1.getY();
        double z0 = p1.getZ();
        
        double x1 = p2.getX();
        double y1 = p2.getY();
        double z1 = p2.getZ();

        double dx = Math.abs(x1 - x0);
        double dy = Math.abs(y1 - y0);
        double dz = Math.abs(z1 - z0);
        
        int xInc = x0 < x1 ? 1 : -1;
        int yInc = y0 < y1 ? 1 : -1;
        int zInc = z0 < z1 ? 1 : -1;
        
        int x = (int)Math.round(x0);
        int y = (int)Math.round(y0);
        int z = (int)Math.round(z0);
        
        double error1 = 0.0;
        double error2 = 0.0;
        
        List<int[]> result = new ArrayList<int[]>();
        if(dx >= dy && dx >= dz)
        {
            error1 = 2.0 * dy - dx;
            error2 = 2.0 * dz - dx;
            while(x != Math.round(x1))
            {
                result.add(new int[] {x, y, z});
                x += xInc;
                if(error1 >= 0.0)
                {
                    y += yInc;
                    error1 -= 2.0 * dx;
                }
                if(error2 >= 0.0)
                {
                    z += zInc;
                    error2 -= 2.0 * dx;
                }
                error1 += 2 * dy;
                error2 += 2 * dz;
            }
        }
        else if(dy >= dx && dy >= dz)
        {
            error1 = 2.0 * dx - dy;
            error2 = 2.0 * dz - dy;
            while(y != Math.round(y1))
            {
                result.add(new int[] {x, y, z});
                y += yInc;
                if(error1 >= 0.0)
                {
                    x += xInc;
                    error1 -= 2.0 * dy;
                }
                if(error2 >= 0.0)
                {
                    z += zInc;
                    error2 -= 2.0 * dy;
                }
                error1 += 2 * dx;
                error2 += 2 * dz;
            }
        }
        else
        {
            error1 = 2.0 * dx - dz;
            error2 = 2.0 * dy - dz;
            while(z != Math.round(z1))
            {
                result.add(new int[] {x, y, z});
                z += zInc;
                if(error1 >= 0.0)
                {
                    x += xInc;
                    error1 -= 2.0 * dz;
                }
                if(error2 >= 0.0)
                {
                    y += yInc;
                    error2 -= 2.0 * dz;
                }
                error1 += 2 * dx;
                error2 += 2 * dy;
            }
        }
        return result.toArray(new int[0][0]);
    }

    private List<int[]> pixelate2D(List<TrajectoryElement> trajectory)
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

    private void validateVector3D(int[][] rasterizedV)
    {
        for(int idx = 0; idx < rasterizedV.length - 1; idx++)
        {
            int[] p1 = rasterizedV[idx];
            int[] p2 = rasterizedV[idx + 1];
            if(!connected3D(p1, p2) || same3D(p1, p2))
            {
                System.out.println("shit");
            }
        }
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
    
    public static boolean same3D(int[] p1, int[] p2)
    {
        return p1[0] == p2[0] && p1[1] == p2[1] && p1[2] == p2[2];
    }

    public static boolean connected3D(int[] p1, int[] p2)
    {
        int dx = Math.abs(p2[0] - p1[0]);
        int dy = Math.abs(p2[1] - p1[1]);
        int dz = Math.abs(p2[2] - p1[2]);
        return dx <= 1 && dy <= 1 && dz <= 1;
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
    
    public List<Integer> createSteps3D(List<int[]> pixels)
    {
        List<Integer> result = new ArrayList<Integer>();
        pixels.stream().reduce((p1, p2) -> {int direction = computeDir(p1, p2); result.add(direction); return p2;});
        return result;
    }

    /**
     * Assign a number to every direction we can go in 3d.
     * p1 and p2 must differ, -1 will otherwise result as the illegal direction
     * @param p1
     * @param p2
     * @return
     */
    private int computeDir(int[] p1, int[] p2)
    {
        int dx = p2[0] - p1[0] + 1;
        int dy = p2[1] - p1[1] + 1;
        int dz = p2[2] - p1[2] + 1;
        return (dx + 3 * dy + 9 * dz);
    }

    public static String toBase26(Integer i)
    {
        String r = null;
        switch(i)
        {
            case 0:
                r = "0";
                break;
            case 1:
                r = "1";
                break;
            case 2:
                r = "2";
                break;
            case 3:
                r = "3";
                break;
            case 4:
                r = "4";
                break;
            case 5:
                r = "5";
                break;
            case 6:
                r = "6";
                break;
            case 7:
                r = "7";
                break;
            case 8:
                r = "8";
                break;
            case 9:
                r = "9";
                break;
            case 10:
                r = "a";
                break;
            case 11:
                r = "b";
                break;
            case 12:
                r = "c";
                break;
            case 13:
                r = "d";
                break;
            case 14:
                r = "e";
                break;
            case 15:
                r = "f";
                break;
            case 16:
                r = "g";
                break;
            case 17:
                r = "h";
                break;
            case 18:
                r = "i";
                break;
            case 19:
                r = "j";
                break;
            case 20:
                r = "k";
                break;
            case 21:
                r = "l";
                break;
            case 22:
                r = "m";
                break;
            case 23:
                r = "n";
                break;
            case 24:
                r = "o";
                break;
            case 25:
                r = "p";
                break;
            case 26:
                r = "q";
                break;
            default:
                r = "z";
        }
        return r;
    }
}
