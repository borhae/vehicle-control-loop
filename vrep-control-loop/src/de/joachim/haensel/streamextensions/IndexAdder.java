package de.joachim.haensel.streamextensions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class IndexAdder<T>
{
    private int _index;
    private T _value;

    private IndexAdder(int index, T value)
    {
        _index = index;
        _value = value;
    }

    public int index()
    {
        return _index;
    }

    public T value()
    {
        return _value;
    }
    
    public static <T> Function<T, IndexAdder<T>> indexed()
    {
        Function<T, IndexAdder<T>> result = 
                new Function<T, IndexAdder<T>>() 
        {
            AtomicInteger idxGenerator = new AtomicInteger(0);
            
            @Override
            public IndexAdder<T> apply(T t)
            {
                return new IndexAdder<T>(idxGenerator.incrementAndGet(), t);
            }
        };
        return result;
    }
}
