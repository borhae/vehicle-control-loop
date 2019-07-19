package de.joachim.haensel.phd.scenario.experiment.evaluation;

public class KeyValuePair
{
    private String _key;
    private Integer _value;

    public KeyValuePair(String line)
    {
        String[] keyValue = line.split(", ");
        _key = keyValue[0];
        _value = Integer.parseInt(keyValue[1]);
    }

    public String getKey()
    {
        return _key;
    }

    public Integer getValue()
    {
        return _value;
    }
}