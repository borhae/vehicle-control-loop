package de.joachim.haensel.phd.scenario.vehicle.control.reactive;

import de.joachim.haensel.statemachine.States;

public enum ControllerStates implements States
{
    DRIVING, IDLE, DRIVING_TO_CLOSEST_KNOWN
}
