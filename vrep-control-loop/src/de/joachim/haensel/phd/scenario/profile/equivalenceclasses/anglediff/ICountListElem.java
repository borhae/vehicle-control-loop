package de.joachim.haensel.phd.scenario.profile.equivalenceclasses.anglediff;

public interface ICountListElem extends Comparable<ICountListElem>
{
    public static double EPSILON = 0.0000000000000000001;

    public void setNext(ICountListElem elem);
    public ICountListElem next();
    public int getHashRangeIdx();
    public double getNumericalValue();
    public double getNormyValue(); // km/h instead of m/s and degrees instead of radians :)
    public void accept(OCStats stats);
}
