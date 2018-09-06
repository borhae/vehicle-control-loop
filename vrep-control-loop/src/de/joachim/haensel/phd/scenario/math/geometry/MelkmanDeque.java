package de.joachim.haensel.phd.scenario.math.geometry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A list implementation that translates the terms used in Melkmans hull algorithm into 
 * the ones used in the internal java linked list  
 * @author Joachim Hänsel
 *
 * @param <T>
 */
public class MelkmanDeque<T> 
{
    //Melkman deque to java deque translation table :)
    //push(v) -> addLast(v)
    //pop() -> removeLast()
    //insert(v) -> addFirst(v)
    //remove() -> removeFirst()

    private LinkedList<T> _container;

    public MelkmanDeque()
    {
        _container = new LinkedList<>();
    }

    public void push(T e)
    {
        _container.addLast(e);
    }

    public void insert(T e)
    {
        _container.addFirst(e);
    }
    
    public void pop()
    {
        _container.removeLast();
    }
    
    public void remove()
    {
        _container.removeFirst();
    }

    public List<T> asList()
    {
        return new ArrayList<>(_container);
    }

    public T get_b()
    {
        return _container.get(0);
    }

    public T get_b_plus1()
    {
        return _container.get(1);
    }

    public T get_t()
    {
        return _container.getLast();
    }

    public T get_t_minus1()
    {
        return _container.get(_container.size() - 2);
    }

    @Override
    public String toString()
    {
        return _container.toString();
    }
}
