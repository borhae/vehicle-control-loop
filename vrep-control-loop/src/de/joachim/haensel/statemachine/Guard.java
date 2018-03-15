package de.joachim.haensel.statemachine;

public interface Guard
{
    public static Guard TRUE_GUARD = () -> {return true;};
    public boolean isTrue();
}
