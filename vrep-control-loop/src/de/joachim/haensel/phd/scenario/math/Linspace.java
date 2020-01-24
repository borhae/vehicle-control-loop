package de.joachim.haensel.phd.scenario.math;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Linspace
{
    public static List<Double> linspace(double start, double stop, int n)
    {
       double step = (stop-start)/(((double)n)-1);
//       List<Double> result = new ArrayList<>();
//
//
//       for(int i = 0; i <= n-2; i++)
//       {
//           result.add(start + (i * step));
//       }
//       result.add(stop);

       List<Double> result = IntStream.rangeClosed(0, n - 2).mapToDouble(intValue -> (start + ((double)intValue) * step)).boxed().collect(Collectors.toList());
       result.add(stop);
       return result;
    }
}
