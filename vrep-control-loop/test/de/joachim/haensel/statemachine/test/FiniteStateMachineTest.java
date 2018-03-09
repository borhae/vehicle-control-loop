package de.joachim.haensel.statemachine.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;

public class FiniteStateMachineTest
{
    @Test
    public void testExampleFSM()
    {
        TestStateMachine sm = new TestStateMachine();
        sm.light();
        assert(!sm.isLightOn());
        sm.on();
        sm.light();
        assert(sm.isLightOn());    
        sm.off();
        sm.light();
        assert(!sm.isLightOn());
    }
}
