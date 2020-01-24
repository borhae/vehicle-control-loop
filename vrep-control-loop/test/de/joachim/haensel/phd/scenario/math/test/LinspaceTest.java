package de.joachim.haensel.phd.scenario.math.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

import de.joachim.haensel.phd.scenario.math.Linspace;

public class LinspaceTest
{
    @Test
    public void testLinSpace()
    {
        List<Double> linspace = Linspace.linspace(0.0, 1.0, 10);
        assertEquals(0.0, (double)linspace.get(0));
    }
}
