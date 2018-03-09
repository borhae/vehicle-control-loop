package de.joachim.haensel.statemachine;

import java.util.HashMap;
import java.util.Map;

public class FiniteStateMachineTemplate
{
    public class StateActionPair
    {
        private States _state;
        private Runnable _action;

        public StateActionPair(States toState, Runnable action)
        {
            _state = toState;
            _action = action;
        }

        public void runAction()
        {
            if(_action == null)
            {
                return;
            }
            else
            {
                _action.run();
            }
        }

        public States getState()
        {
            return _state;
        }

        @Override
        public String toString()
        {
            String action = null;
            if(_action == null)
            {
                action = "will not trigger an action";
            }
            else
            {
                action = "will trigger an action";
            }
            String state = _state.toString();
            return state + " " + action;
        }
    }

    private States _currentState;
    private States _initialState;
    private HashMap<States, Map<Messages, StateActionPair>> _transitionTable;

    public FiniteStateMachineTemplate()
    {
        _currentState = States.ILLEGAL;
        _transitionTable = new HashMap<States, Map<Messages, StateActionPair>>();
    }
    
    protected States getCurrentState()
    {
        return _currentState;
    }

    protected void transition(States fromState, Messages msg)
    {
        StateActionPair stateActionPair = _transitionTable.get(fromState).get(msg);
        if(stateActionPair == null)
        {
            return;
        }
        else
        {
            stateActionPair.runAction();
            _currentState = stateActionPair.getState();
        }
    }

    protected void createTransition(States fromState, Messages msg, States toState, Runnable action)
    {
        Map<Messages, StateActionPair> transitionsFromFromState = _transitionTable.get(fromState);
        if(transitionsFromFromState == null)
        {
            transitionsFromFromState = new HashMap<>();
            _transitionTable.put(fromState, transitionsFromFromState);
        }
        StateActionPair newStateAction = new StateActionPair(toState, action);
        transitionsFromFromState.put(msg, newStateAction);
    }

    protected void setInitialState(States initialState)
    {
        _initialState = initialState;
    }

    protected void reset()
    {
        _currentState = _initialState;
    }
}
