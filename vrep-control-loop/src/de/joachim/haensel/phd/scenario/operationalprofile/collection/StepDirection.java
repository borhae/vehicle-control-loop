package de.joachim.haensel.phd.scenario.operationalprofile.collection;

public enum StepDirection
{
    N, NE, E, SE, S, SW, W, NW, C, INVALID;
    //      N
    //  NW     NE
    //W     C     E
    //  SW     SE
    //      S  

    public static StepDirection get(int[] p1, int[] p2)
    {
        int x0 = p1[0];
        int y0 = p1[1];
        int x1 = p2[0];
        int y1 = p2[1];
        int dx = x1 - x0;
        int dy = y1 - y0;
        StepDirection result = INVALID;
        if(dx == 0 && dy == 0)
        {
            result = C;
        }
        else if(dx == 0)
        {
            result = dy > 0 ? N : S;
        }
        else if(dy == 0)
        {
            result = dx > 0 ? E : W;
        }
        else if(dx > 0)
        {
            result = dy > 0 ? NE : SE;
        }
        else
        {
            result = dy > 0 ? NW : SW;
        }
        return result;
    }
}
