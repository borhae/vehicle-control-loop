package de.joachim.haensel.statemachine.test;

import de.joachim.haensel.statemachine.FiniteStateMachineTemplate;
import de.joachim.haensel.statemachine.Guard;
import de.joachim.haensel.statemachine.Messages;
import de.joachim.haensel.statemachine.States;

public class TestStateMachineGuards extends FiniteStateMachineTemplate
{
    public enum TestMessages implements Messages
    {
        LISTEN_ACTIVE, ENVIRONMENNT_EVENT
    }

    public enum TestStates implements States
    {
        ACTIVE, IDLE
    }

    private int _valA;
    
    public TestStateMachineGuards()
    {
        super();
        createTransition(TestStates.IDLE, TestMessages.LISTEN_ACTIVE, null, TestStates.ACTIVE, null);
        Guard elseGuard = () -> !((Guard) () -> _valA < 10).isTrue();
        createTransition(TestStates.ACTIVE, TestMessages.ENVIRONMENNT_EVENT, () -> _valA < 10, TestStates.ACTIVE, null);
        createTransition(TestStates.ACTIVE, TestMessages.ENVIRONMENNT_EVENT, elseGuard, TestStates.IDLE, null);
        setInitialState(TestStates.IDLE);
        reset();
    }
    
    public void listenActive()
    {
        transition(TestMessages.LISTEN_ACTIVE, null);
    }
    
    public void environmentEvent()
    {
        transition(TestMessages.ENVIRONMENNT_EVENT, null);
    }

    public boolean isListening()
    {
        return getCurrentState() == TestStates.ACTIVE;
    }

    public void setValA(int valA)
    {
        _valA = valA;
    }
}
