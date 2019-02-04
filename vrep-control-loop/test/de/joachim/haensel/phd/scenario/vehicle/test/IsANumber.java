package de.joachim.haensel.phd.scenario.vehicle.test;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsANumber extends TypeSafeMatcher<Double>
{

    @Override
    public void describeTo(Description description)
    {
        description.appendText("a valid number");
    }

    @Override
    protected boolean matchesSafely(Double number)
    {
        return !number.isNaN();
    }
    
    public static Matcher<Double> isANumber()
    {
        return new IsANumber();
    }
}
