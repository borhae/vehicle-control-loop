package de.joachim.haensel.phd.scenario.math;

public class TriangleSolver
{
    private double a;
    private double b;
    private double c;
    private double A;
    private double B;
    private double C;
    private boolean aDef;
    private boolean bDef;
    private boolean cDef;
    private boolean ADef;
    private boolean BDef;
    private boolean CDef;
    private double[] As;
    private double[] Bs;
    private double[] as;
    private double[] bs;
    private double[] cs;
    private double[] Cs;
    private double area;
    private TriangleType type;
    private double[] areas;


    public TriangleSolver()
    {
        a = Double.NaN;
        b = Double.NaN;
        c = Double.NaN;

        A = Double.NaN;
        B = Double.NaN;
        C = Double.NaN;

        area = Double.NaN;
        type = TriangleType.UNKNOWN;
        aDef = false;
        bDef = false;
        cDef = false;
        ADef = false;
        BDef = false;
        CDef = false;
    }

    public double geta()
    {
        return a;
    }

    public void seta(double a)
    {
        this.a = a;
        aDef = true;
    }

    public double getb()
    {
        return b;
    }

    public void setb(double b)
    {
        this.b = b;
        bDef = true;
    }

    public double getc()
    {
        return c;
    }

    public void setc(double c)
    {
        this.c = c;
        cDef = true;
    }

    public double getAlpha()
    {
        return A;
    }

    public void setAlpha(double alpha)
    {
        A = alpha;
        ADef = true;
    }

    public double getBeta()
    {
        return B;
    }

    public void setBeta(double beta)
    {
        B = beta;
        BDef = true;
    }

    public double getGamma()
    {
        return C;
    }

    public void setGamma(double gamma)
    {
        C = gamma;
        CDef = true;
    }

    public double[] getTwoAlpha()
    {
        return As;
    }

    public double[] getTwoBeta()
    {
        return Bs;
    }

    public double[] getTwoGamma()
    {
        return Cs;
    }

    public double[] getTwoa()
    {
        return as;
    }

    public double[] getTwob()
    {
        return bs;
    }

    public double[] getTwoc()
    {
        return cs;
    }

    public double getArea()
    {
        return area;
    }

    public TriangleType getType()
    {
        return type;
    }

    
    /*---- Solver functions ----*/

    // Given some sides and angles, this returns a tuple of 8 number/string
    // values.
    public void solveTriangle() throws TriangleError 
    {
        int aCnt = aDef ? 1 : 0;
        int bCnt = bDef ? 1 : 0;
        int cCnt = cDef ? 1 : 0;
        int sides = aCnt + bCnt + cCnt; 
        int alphaCnt = ADef ? 1 : 0;
        int betaCnt = BDef ? 1 : 0;
        int gammaCnt = CDef ? 1 : 0;
        int angles = alphaCnt + betaCnt + gammaCnt; 
        if(sides + angles != 3)
        {
            throw new TriangleError("Unsolvable, need at least three elements");
        }
        else if (sides == 0)
        {
            throw new TriangleError("Unsolvable, need at least one side");
        }
        else if(sides == 3)
        {
            solveSideSideSide();
        }
        else if (angles == 2)
        {
            angleSideAngle();
        }
        else if ((ADef && !aDef) || (BDef && !bDef) || (CDef && !cDef)) 
        {
            solveSideAngleSide();
        }
        else
        {
            solveSideSideAngle();
        }
    }

    private void solveSideSideAngle() throws TriangleError
    {
        type = TriangleType.SSA;
        double knownSide = Double.NaN;
        double knownAngle = Double.NaN;
        double partialSide = Double.NaN;
        if (aDef && ADef) 
        { 
            knownSide = a; 
            knownAngle = A; 
        }
        if (bDef && BDef) 
        { 
            knownSide = b; 
            knownAngle = B; 
        }
        if (cDef && CDef) 
        { 
            knownSide = c;
            knownAngle = C; 
        }
        if (aDef && !ADef) 
        {
            partialSide = a;
        }
        if (bDef && !BDef) 
        {
            partialSide = b;
        }
        if (cDef && !CDef) 
        {
            partialSide = c;
        }
        if (knownAngle >= 180)
        {
            throw new TriangleError("No solution for SSA case for known angle larger than 180 degree.");
        }
        double ratio = knownSide / Math.sin(Math.toRadians(knownAngle));
        double temp = partialSide / ratio;  // sin(partialAngle)
        double partialAngle = Double.NaN;
        double unknownSide = Double.NaN;
        double  unknownAngle = Double.NaN;
        if (temp > 1 || (knownAngle >= 90 && partialSide >= knownSide))
        {
            throw new TriangleError("No solution for Side Side Angle case: sin is larger 1 or (known angle larger 90 deg. and partialSide >= knownSide)");
        }
        else if (temp == 1 || knownSide >= partialSide) 
        {
            solveSideSideAngleUniqueSolution(knownSide, knownAngle, partialSide, ratio, temp);
        } 
        else 
        {
            solveSideSideAngleTwoSolutions(knownSide, knownAngle, partialSide, ratio, temp, partialAngle, unknownSide, unknownAngle);
        }
    }

    private void solveSideSideAngleTwoSolutions(double knownSide, double knownAngle, double partialSide, double ratio,
            double temp, double partialAngle, double unknownSide, double unknownAngle)
    {
        type = TriangleType.SSA_TWO_SOLUTIONS;
        double partialAngle0 = Math.toDegrees(Math.asin(temp));
        double partialAngle1 = 180 - partialAngle0;
        double unknownAngle0 = 180 - knownAngle - partialAngle0;
        double unknownAngle1 = 180 - knownAngle - partialAngle1;
        double unknownSide0 = ratio * Math.sin(Math.toRadians(unknownAngle0));  // Law of sines
        double unknownSide1 = ratio * Math.sin(Math.toRadians(unknownAngle1));  // Law of sines
        double[] partialAngles = new double[]{partialAngle0, partialAngle1};
        double[] unknownAngles = new double[]{unknownAngle0, unknownAngle1};
        double[] unknownSides = new double[]{unknownSide0, unknownSide1};
        areas = new double[]{knownSide * partialSide * Math.sin(Math.toRadians(unknownAngle0)) / 2,
                knownSide * partialSide * Math.sin(Math.toRadians(unknownAngle1)) / 2};
        if (aDef && !ADef)
        {
            As = partialAngles;
        }
        if (bDef && !BDef)
        {
            Bs = partialAngles;
        }
        if (cDef && !CDef)
        {
            Cs = partialAngles;
        }
        if (!aDef && !ADef)
        {
            as = unknownSides;
            As = unknownAngles;
        }
        if (!bDef && !BDef)
        {
            bs = unknownSides;
            Bs = unknownAngles;
        }
        if (!cDef && !CDef)
        {
            cs = unknownSides;
            Cs = unknownAngles;
        }
    }

    private void solveSideSideAngleUniqueSolution(double knownSide, double knownAngle, double partialSide, double ratio,
            double temp)
    {
        double partialAngle;
        double unknownSide;
        double unknownAngle;
        type = TriangleType.SSA_UNIQUE_SOLUTION;
        partialAngle = Math.toDegrees(Math.asin(temp));
        unknownAngle = 180 - knownAngle - partialAngle;
        unknownSide = ratio * Math.sin(Math.toRadians(unknownAngle));  // Law of sines
        area = knownSide * partialSide * Math.sin(Math.toRadians(unknownAngle)) / 2;
        if (aDef && !ADef)
        {
            A = partialAngle;
        }
        if (bDef && !BDef)
        {
            B = partialAngle;
        }
        if (cDef && !CDef)
        {
            C = partialAngle;
        }
        if (!aDef && !ADef)
        {
            a = unknownSide;
            A = unknownAngle;
        }
        if (!bDef && !BDef)
        {
            b = unknownSide;
            B = unknownAngle;
        }
        if (!cDef && !CDef)
        {
            c = unknownSide;
            C = unknownAngle;
        }
    }

    private void solveSideAngleSide() throws TriangleError
    {
        type = TriangleType.SAS;
        if( (ADef && A >= 180) || (BDef && B >= 180) || (CDef && C >= 180))
        {
            throw new TriangleError("ASA case. One side not given and its complement angle is larger 180");
        }
        if (!aDef) a = solveSide(b, c, A);
        if (!bDef) b = solveSide(c, a, B);
        if (!cDef) c = solveSide(a, b, C);
        if (!ADef) A = solveAngle(b, c, a);
        if (!BDef) B = solveAngle(c, a, b);
        if (!CDef) C = solveAngle(a, b, c);
        if (ADef) area = b * c * Math.sin(Math.toRadians(A)) / 2.0;
        if (BDef) area = c * a * Math.sin(Math.toRadians(B)) / 2.0;
        if (CDef) area = a * b * Math.sin(Math.toRadians(C)) / 2.0;
    }

    private void angleSideAngle() throws TriangleError
    {
        type = TriangleType.ASA;
        // Find missing angle
        if (!ADef)
        {
            A = 180 - B - C;
        }
        if (!BDef)
        {
            B = 180 - C - A;
        }
        if (!CDef)
        {
            C = 180 - A - B;
        }
        if (0 >= A || 0 >= B || 0 >= C)
        {
            throw new TriangleError("Negative angles not allowed.");
        }
        double sinA = Math.sin(Math.toRadians(A));
        double sinB = Math.sin(B);
        double sinC = Math.sin(C);
        // Use law of sines to find sides
        double ratio = 0.0; // side / sin(angle)
        if (aDef)
        {
            ratio = a / sinA;
            area = a * ratio * sinB * sinC / 2;
        }
        if (bDef)
        {
            ratio = b / sinB;
            area = b * ratio * sinC * sinA / 2;
        }
        if (cDef)
        {
            ratio = c / sinC;
            area = c * ratio * sinA * sinB / 2;
        }
        if (!aDef)
        {
            a = ratio * sinA;
        }
        if (!bDef)
        {
            b = ratio * sinB;
        }
        if (!cDef)
        {
            c = ratio * sinC;
        }
    }

    private void solveSideSideSide() throws TriangleError
    {
        type = TriangleType.SSS;
        if (c >= a + b || a >= b + c || b >= c + a)
        {
            throw new TriangleError("Side side side, but no solution: one side is bigger than the sum of the others");
        }
        A = solveAngle(b, c, a);
        B = solveAngle(c, a, b);
        C = solveAngle(a, b, c);
        // Heron's formula
        double s = (a + b + c) / 2;
        area = Math.sqrt(s * (s - a) * (s - b) * (s - c));
    }

    /** Returns side c using law of cosines.
     * 
     * @param _a
     * @param _b
     * @param _C
     * @return
     */
    public double solveSide(double _a, double _b, double _C)
    {
        _C = Math.toRadians(_C);
        if(_C > 0.001)
        {
            return Math.sqrt(_a * _a + _b * _b - 2 * _a * _b * Math.cos(_C));
        }
        else
        {
         // Explained in https://www.nayuki.io/page/numerically-stable-law-of-cosines
            return Math.sqrt((_a - _b) * (_a - _b) + _a * _b * _C * _C * (1 - _C * _C / 12));
        }
    }

    /**
     * Use law of cosines to get angle C
     * 
     * @param a
     * @param b
     * @param c
     * @return
     * @throws TriangleError 
     */
    public double solveAngle(double a, double b, double c) throws TriangleError
    {
        double temp = (a * a + b * b - c * c) / (2 * a * b); // what's this?

        if (temp >= -1 && 0.9999999 >= temp)
        {
            return Math.toDegrees(Math.acos(temp));
        }
        // more preciseness for results between 1 and 0.9999999
        else if (1 >= temp) // Explained in https://www.nayuki.io/page/numerically-stable-law-of-cosines
        {
            return Math.toDegrees(Math.sqrt((c * c - (a - b) * (a - b)) / (a * b)));
        }
        else
        {
            throw new TriangleError("Cosinus formula can't operate out of range [-1, 1]");
        }
    }
}
