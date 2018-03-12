package de.joachim.haensel.statemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;


public class FiniteStateMachineTemplate
{
    public class ActionTargetStatePair<T>
    {
        private States _state;
        private Consumer<T> _action;

        public ActionTargetStatePair(States toState, Consumer<T> action)
        {
            _state = toState;
            _action = action;
        }

        public void runAction(T parameter)
        {
            if(_action == null)
            {
                return;
            }
            else
            {
                _action.accept(parameter);
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
                action = _action.getClass().getName();
            }
            else
            {
                action = "";
            }
            String state = _state.toString();
            return "->" + state + "/{" + action + "}";
        }
    }

    private States _currentState;
    private States _initialState;
    private HashMap<States, Map<Messages, ActionTargetStatePair>> _transitionTable;

    public FiniteStateMachineTemplate()
    {
        _currentState = States.ILLEGAL;
        _transitionTable = new HashMap<States, Map<Messages, ActionTargetStatePair>>();
    }
    
    public States getCurrentState()
    {
        return _currentState;
    }

    public<T> void transition(Messages msg, T parameter)
    {
        States fromState = getCurrentState();
        ActionTargetStatePair stateActionPair = _transitionTable.get(fromState).get(msg);
        if(stateActionPair == null)
        {
            //no transition from current state possible with message msg
            return;
        }
        else
        {
            stateActionPair.runAction(parameter);
            _currentState = stateActionPair.getState();
        }
    }

    public <T, R> void createTransition(States fromState, Messages msg, States toState, Consumer<T> action)
    {
        Map<Messages, ActionTargetStatePair> transitionsFromFromState = _transitionTable.get(fromState);
        if(transitionsFromFromState == null)
        {
            transitionsFromFromState = new HashMap<>();
            _transitionTable.put(fromState, transitionsFromFromState);
        }
        ActionTargetStatePair<T> newStateAction = new ActionTargetStatePair<T>(toState, action);
        transitionsFromFromState.put(msg, newStateAction);
    }

    public void setInitialState(States initialState)
    {
        _initialState = initialState;
    }

    public void reset()
    {
        _currentState = _initialState;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        Set<States> fromStates = _transitionTable.keySet();
        for (States curFrom : fromStates)
        {
            Map<Messages, ActionTargetStatePair> map = _transitionTable.get(curFrom);
            Set<Messages> availableMessages = map.keySet();
            for (Messages curMessage : availableMessages)
            {
                ActionTargetStatePair actionTargetStatePair = map.get(curMessage);
                String actionName = actionTargetStatePair._action == null ? "" : actionTargetStatePair._action.getClass().getName();
                result.append(curFrom + "-" + curMessage + "/{" + actionName + "}" + actionTargetStatePair._state); 
                result.append(System.lineSeparator());
            }
        }
        return result.toString();
    }
}
