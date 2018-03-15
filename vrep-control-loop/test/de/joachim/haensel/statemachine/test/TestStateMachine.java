package de.joachim.haensel.statemachine.test;

import java.util.function.Consumer;

import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Messages;
import de.joachim.haensel.statemachine.States;

public class TestStateMachine extends FiniteStateMachineTemplate
{
    public enum TestMessages implements Messages
    {
        ON, OFF, LIGHT_SWITCH;
    }

    public enum TestStates implements States
    {
        DEVICE_OFF, DEVICE_ON_LIGHT_OFF, DEVICE_ON_LIGHT_ON
    }

    public TestStateMachine()
    {
        createTransition(TestStates.DEVICE_OFF, TestMessages.ON, null, TestStates.DEVICE_ON_LIGHT_OFF, null);
        createTransition(TestStates.DEVICE_ON_LIGHT_OFF, TestMessages.OFF, null, TestStates.DEVICE_OFF, null);
        
        Consumer<Object> lightAction = o -> System.out.println("Light");
        createTransition(TestStates.DEVICE_ON_LIGHT_OFF, TestMessages.LIGHT_SWITCH, null, TestStates.DEVICE_ON_LIGHT_ON, lightAction);

        Consumer<Object> lightOutAction = o -> System.out.println("Darkness");
        createTransition(TestStates.DEVICE_ON_LIGHT_ON, TestMessages.LIGHT_SWITCH, null, TestStates.DEVICE_ON_LIGHT_OFF, lightOutAction );
        
        setInitialState(TestStates.DEVICE_OFF);
        reset();
    }
    
    public void on()
    {
        transition(TestMessages.ON, null);
    }

    public void light()
    {
        transition(TestMessages.LIGHT_SWITCH, null);
    }

    public void off()
    {
        transition(TestMessages.OFF, null);
    }

    public boolean isLightOn()
    {
        return getCurrentState() == TestStates.DEVICE_ON_LIGHT_ON;
    }
}
