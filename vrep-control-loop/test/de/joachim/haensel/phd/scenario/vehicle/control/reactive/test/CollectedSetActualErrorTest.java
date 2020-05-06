package de.joachim.haensel.phd.scenario.vehicle.control.reactive.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.joachim.haensel.phd.scenario.vehicle.control.reactive.ppvadaptable.AtomicSetActualError;

public class CollectedSetActualErrorTest
{
    /**
     * Somehow the testconfiguration is broken. Even the added run-time code does not work
     * @param args
     */
    public static void main(String[] args)
    {
        CollectedSetActualErrorTest tester = new CollectedSetActualErrorTest();
        tester.testReduction();
    }
    
    public void testReduction()
    {
        List<AtomicSetActualError> input = new ArrayList<AtomicSetActualError>(Arrays.asList(
                new AtomicSetActualError(1.0, 2.0), 
                new AtomicSetActualError(1.0, 2.0), 
                new AtomicSetActualError(1.0, 2.0), 
                new AtomicSetActualError(1.0, 2.0) 
            ));
        AtomicSetActualError actual = input.stream().reduce(new AtomicSetActualError(0.0, 0.0), (subtotal, element) -> subtotal.add(element));
        AtomicSetActualError expected = new AtomicSetActualError(4.0, 8.0);
    }
}
