package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.hashing;

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

    public String toCode()
    {
        switch(this)
        {
            case N:
                return "0";
            case NE:
                return "1";
            case E:
                return "2";
            case SE:
                return "3";
            case S:
                return "4";
            case SW:
                return "5";
            case W:
                return "6";
            case NW:
                return "7";
            case C:
                return "8";
            default:
                return "U";
        }
    }
}
