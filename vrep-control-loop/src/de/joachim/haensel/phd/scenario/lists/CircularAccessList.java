package de.joachim.haensel.phd.scenario.lists;

import de.joachim.haensel.phd.scenario.math.geometry.MelkmanDeque;
import de.joachim.haensel.phd.scenario.math.geometry.Position2D;

public class CircularAccessList 
{
    private MelkmanDeque<Position2D> _baseDeque;

    public CircularAccessList(MelkmanDeque<Position2D> inputDeque)
    {
        _baseDeque = inputDeque;
    }

    public Position2D get(int idx)
    {
        return _baseDeque.get(idx % _baseDeque.size());
    }

    public int size()
    {
        return _baseDeque.size();
    }
}
