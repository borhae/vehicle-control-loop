package de.joachim.haensel.statemachine.test;

import java.util.function.Consumer;

import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Messages;
import de.joachim.haensel.statemachine.States;

public class TestStateMachineParams extends FiniteStateMachineTemplate
{
    public enum TestMessages implements Messages
    {
        ON, OFF, LIGHT_SWITCH, LIGHT_OUT_SWITCH;
    }

    public enum TestStates implements States
    {
        DEVICE_OFF, DEVICE_ON_LIGHT_OFF, DEVICE_ON_LIGHT_ON
    }

    public TestStateMachineParams()
    {
        Consumer<Object> actionOff = parameter -> System.out.println("Darkness"); 
        Consumer<Integer> actionOn = parameter -> System.out.println("Light, brightness: " + parameter);
        
        createTransition(TestStates.DEVICE_OFF, TestMessages.ON, null, TestStates.DEVICE_ON_LIGHT_OFF, null);
        createTransition(TestStates.DEVICE_ON_LIGHT_OFF, TestMessages.OFF, null, TestStates.DEVICE_OFF, null);
        
        createTransition(TestStates.DEVICE_ON_LIGHT_OFF, TestMessages.LIGHT_SWITCH, null, TestStates.DEVICE_ON_LIGHT_ON, actionOn);
        createTransition(TestStates.DEVICE_ON_LIGHT_ON, TestMessages.LIGHT_SWITCH, null, TestStates.DEVICE_ON_LIGHT_ON, actionOn);
        createTransition(TestStates.DEVICE_ON_LIGHT_ON, TestMessages.LIGHT_OUT_SWITCH, null, TestStates.DEVICE_ON_LIGHT_OFF, actionOff);
        
        createTransition(TestStates.DEVICE_ON_LIGHT_ON, TestMessages.OFF, null, TestStates.DEVICE_OFF, actionOff);
        
        setInitialState(TestStates.DEVICE_OFF);
        reset();
    }
    
    public void on()
    {
        transition(TestMessages.ON, null);
    }

    public void light(int brightness)
    {
        transition(TestMessages.LIGHT_SWITCH, brightness);
    }

    public void off()
    {
        transition(TestMessages.OFF, null);
    }

    public boolean isLightOn()
    {
        return getCurrentState() == TestStates.DEVICE_ON_LIGHT_ON;
    }

    public void lightOff()
    {
        transition(TestMessages.LIGHT_OUT_SWITCH, null);
    }
}
