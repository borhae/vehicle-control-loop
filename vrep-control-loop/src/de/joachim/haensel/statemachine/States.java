package de.joachim.haensel.statemachine;

public interface States
{
    public static final States ILLEGAL = new States() {public String toString() {return "Illegal";}};
}
