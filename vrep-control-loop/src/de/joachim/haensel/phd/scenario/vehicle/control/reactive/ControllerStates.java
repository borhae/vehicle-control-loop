package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

import de.joachim.haensel.statemachine.States;

/**
 * @author dummy
 *
 */
public enum ControllerStates implements States
{
    DRIVING, IDLE, DRIVING_TO_CLOSEST_KNOWN, HALFWAY_BACK_ON_TRACK, THREE_POINT_TURN;

    @Override
    public String toString()
    {
        switch(this)
        {
            case DRIVING:
                return "driving";
            case DRIVING_TO_CLOSEST_KNOWN:
                return "driving to closest known";
            case HALFWAY_BACK_ON_TRACK:
                return "halfway back";
            case IDLE:
                return "idle";
            case THREE_POINT_TURN:
                return "three point turn";
            default:
                return "unknown state";
        }
    }
}
