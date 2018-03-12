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
    
    @Test 
    public void testExampleFSMWithParameters()
    {
        TestStateMachineParams sm = new TestStateMachineParams();
        System.out.println(sm.toString());
        sm.light(5);
        assert(!sm.isLightOn());
        sm.on();
        sm.light(7);
        assert(sm.isLightOn());
        sm.light(9);
        assert(sm.isLightOn());
        sm.lightOff();
        assert(!sm.isLightOn());
        sm.light(20);
        assert(sm.isLightOn());
        sm.off();
        sm.light(6);
        assert(!sm.isLightOn());
    }
}
