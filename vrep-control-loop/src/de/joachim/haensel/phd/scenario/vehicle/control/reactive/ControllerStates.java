package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

import de.joachim.haensel.statemachine.States;

/**
 * @author dummy
 *
 */
public enum ControllerStates implements States
{
    DRIVING, IDLE, DRIVING_TO_CLOSEST_KNOWN, HALFWAY_BACK_ON_TRACK, FAILED;

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
            case FAILED:
                return "failed";
            default:
                return "unknown state";
        }
    }
}
