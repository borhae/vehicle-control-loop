package de.joachim.haensel.phd.scenario.math;

import java.util.ArrayList;
import java.util.List;

public class Linspace
{
    public static List<Double> linspace(double start, double stop, int n)
    {
       List<Double> result = new ArrayList<>();

       double step = (stop-start)/(n-1);

       for(int i = 0; i <= n-2; i++)
       {
           result.add(start + (i * step));
       }
       result.add(stop);

       return result;
    }
}
