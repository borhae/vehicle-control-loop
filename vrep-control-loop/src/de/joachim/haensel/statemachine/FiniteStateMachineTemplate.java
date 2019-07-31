package de.joachim.haensel.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
            if(_action != null)
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
    
    public static Guard TRUE_GUARD = () -> true;
    public static Consumer<EmptyParam> NO_OP_ACTION = dummy -> {};
    public static final EmptyParam EMPTY_PARAMETER = new EmptyParam();

    private States _currentState;
    private States _initialState;
    @SuppressWarnings("rawtypes")
    private HashMap<States, Map<Messages, Map<Guard, ActionTargetStatePair>>> _transitionTable;

    @SuppressWarnings("rawtypes")
    public FiniteStateMachineTemplate()
    {
        _currentState = States.ILLEGAL;
        _transitionTable = new HashMap<States, Map<Messages, Map<Guard, ActionTargetStatePair>>>();
    }
    
    public States getCurrentState()
    {
        return _currentState;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    synchronized public<T> void transition(Messages msg, T parameter)
    {
        States fromState = getCurrentState();
        Map<Messages, Map<Guard,ActionTargetStatePair>> transitions = _transitionTable.get(fromState);
        if(transitions == null)
        {
            // no outgoing transitions from current state (final state? transitiontable not initialized?)
            return;
        }
        Map<Guard, ActionTargetStatePair> guardedActionTargetStatePair = transitions.get(msg);
        if(guardedActionTargetStatePair == null)
        {
            //no transition from current state possible with message msg
            return;
        }
        else
        {
            Set<Guard> guards = guardedActionTargetStatePair.keySet();
            //TODO maybe remove this and make it a flag again
            List<Guard> triggerableGuards = new ArrayList<Guard>();
            ActionTargetStatePair<T> activeActionTargetStatePair = null;
            for (Guard curGuard : guards)
            {
                if(curGuard.isTrue())
                {
                    if(triggerableGuards.isEmpty())
                    {
                        activeActionTargetStatePair = guardedActionTargetStatePair.get(curGuard);
                    }
                    triggerableGuards.add(curGuard);
                }
            }
            if(triggerableGuards.size() == 1)
            {
                activeActionTargetStatePair.runAction(parameter);
                _currentState = activeActionTargetStatePair.getState();
            } 
            else if(triggerableGuards.size() > 1)
            {
                System.out.println("there might be non-determinism here (either in the statemachine instantiation or the statemachine template). Listing debuggable guards:");
                List<DebuggableGuard> debuggableGuards = triggerableGuards.stream().filter(guard -> guard instanceof DebuggableGuard).map(guard -> (DebuggableGuard)guard).collect(Collectors.toList());
                System.out.println(debuggableGuards.stream().map(guard -> guard.guardAsString()).collect(Collectors.joining(", ", "guards: ", "")));
                System.out.println("took first guard");
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public <T, R> void createTransition(States fromState, Messages msg, Guard guard, States toState, Consumer<T> action)
    {
        Map<Messages, Map<Guard, ActionTargetStatePair>> transitionsFromFromState = _transitionTable.get(fromState);
        if(transitionsFromFromState == null)
        {
            // state not present yet
            transitionsFromFromState = new HashMap<>();
            _transitionTable.put(fromState, transitionsFromFromState);
        }
        //state present
        Map<Guard, ActionTargetStatePair> guardedActionTargetStatePair = transitionsFromFromState.get(msg);
        if(guardedActionTargetStatePair == null)
        {
            // state present, no transition for message
            guardedActionTargetStatePair = new HashMap<>();
            transitionsFromFromState.put(msg, guardedActionTargetStatePair);
        }
        //transition with msg present, no guard, no action, no target state
        if(guard == null)
        {
            guard = Guard.TRUE_GUARD;
        }
        ActionTargetStatePair actionTargetStatePair = guardedActionTargetStatePair.get(guard);
        if(actionTargetStatePair == null)
        {
            // guard not present yet
            ActionTargetStatePair<T> newActionTargetPair = new ActionTargetStatePair<>(toState, action);
            guardedActionTargetStatePair.put(guard, newActionTargetPair);
        }
        else
        {
            // transition was entered like this already (same start-state, message and guard)
            return;
        }
        
    }

    public void setInitialState(States initialState)
    {
        _initialState = initialState;
    }

    public void reset()
    {
        _currentState = _initialState;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        Set<States> fromStates = _transitionTable.keySet();
        for (States curFrom : fromStates)
        {
            Map<Messages, Map<Guard, ActionTargetStatePair>> map = _transitionTable.get(curFrom);
            Set<Messages> availableMessages = map.keySet();
            for (Messages curMessage : availableMessages)
            {
                Map<Guard, ActionTargetStatePair> guardedActionTargetStatePair = map.get(curMessage);
                Set<Guard> guards = guardedActionTargetStatePair.keySet();
                for (Guard curGuard : guards)
                {
                    ActionTargetStatePair actionTargetStatePair = guardedActionTargetStatePair.get(curGuard);
                    String actionName = actionTargetStatePair._action == null ? "" : actionTargetStatePair._action.getClass().getName();
                    result.append(curFrom + "  -" + curMessage + "[" + curGuard + "]" + "/{" + actionName + "}-  " + actionTargetStatePair._state); 
                    result.append(System.lineSeparator());
                }
            }
        }
        return result.toString();
    }
}
