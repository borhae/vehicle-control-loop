package de.joachim.haensel.statemachine.test;

import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Messages;
import de.joachim.haensel.statemachine.States;

public class TestStateMachine extends FiniteStateMachineTemplate
{
    public enum TestMessages implements Messages
    {
        ON, OFF, LIGHT_SWITCH
    }

    public enum TestStates implements States
    {
        DEVICE_OFF, DEVICE_ON_LIGHT_OFF, DEVICE_ON_LIGHT_ON
    }

    public TestStateMachine()
    {
        createTransition(TestStates.DEVICE_OFF, TestMessages.ON, TestStates.DEVICE_ON_LIGHT_OFF, null);
        createTransition(TestStates.DEVICE_ON_LIGHT_OFF, TestMessages.OFF, TestStates.DEVICE_OFF, null);
        
        createTransition(TestStates.DEVICE_ON_LIGHT_OFF, TestMessages.LIGHT_SWITCH, TestStates.DEVICE_ON_LIGHT_ON, new Runnable() {
            
            @Override
            public void run()
            {
                System.out.println("Light");
            }
        });
        createTransition(TestStates.DEVICE_ON_LIGHT_ON, TestMessages.LIGHT_SWITCH, TestStates.DEVICE_ON_LIGHT_OFF, new Runnable() {
            
            @Override
            public void run()
            {
                System.out.println("Darkness");
            }
        });
        setInitialState(TestStates.DEVICE_OFF);
        reset();
    }
    
    public void on()
    {
        transition(getCurrentState(), TestMessages.ON);
    }

    public void light()
    {
        transition(getCurrentState(), TestMessages.LIGHT_SWITCH);
    }

    public void off()
    {
        transition(getCurrentState(), TestMessages.OFF);
    }

    public boolean isLightOn()
    {
        return getCurrentState() == TestStates.DEVICE_ON_LIGHT_ON;
    }
}
