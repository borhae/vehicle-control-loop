package de.joachim.haensel.phd.scenario.math;

import de.joachim.haensel.phd.scenario.sumo2vrep.XYMinMax;

/**
 * For now this first scales and then adds an offset. Rotation not tested yet
 * @author dummy
 *
 */
public class TMatrix
{
    private double[][] _m;

    public TMatrix(double scale, double offX, double offY)
    {
        _m = new double[][]{
            {scale, 0,     offX},
            {0,     scale, offY},
            {0,     0,     1   }
        };
    }
    
    public TMatrix(double scale, double offX, double offY, double angle)
    {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        _m = new double[][]{
            {(scale * cos), (scale * -sin), offX},
            {(scale * sin), (scale * cos ), offY},
            {(0          ), (0           ), 1   }
        };
    }

    public double[] transform(double x, double y)
    {
        double[] input = new double[]{x, y, 1};
        double[] result = new double[3];
         
        for(int rowCnt = 0; rowCnt < 3; rowCnt++)
        {
            for(int colCnt = 0; colCnt < 3; colCnt++)
            {
                result[rowCnt] += _m[rowCnt][colCnt] * input[colCnt];
            }
        }
        // cut the last value
        result = new double[]{result[0], result[1]};
        return result;
    }

    public static TMatrix createCenterMatrix(XYMinMax dimensions)
    {
        double offX = dimensions.minX() + dimensions.distX()/2.0;
        double offY = dimensions.minY() + dimensions.distY()/2.0;

        return new TMatrix(1.0, -offX, -offY);
    }
}
