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

    private LinkedList<T> _deque;

    public MelkmanDeque()
    {
        _deque = new LinkedList<>();
    }

    private MelkmanDeque(LinkedList<T> clone)
    {
        _deque = clone;
    }

    /**
     * Add element to the end of the deque
     * @param e
     */
    public void push(T e)
    {
        _deque.addLast(e);
    }

    /**
     * Add element to the beginning of deque
     * @param e
     */
    public void insert(T e)
    {
        _deque.addFirst(e);
    }
    
    /**
     * remove the last element in deque
     */
    public void pop()
    {
        _deque.removeLast();
    }
    
    /**
     * remove first element in deque
     */
    public void remove()
    {
        _deque.removeFirst();
    }

    public List<T> asList()
    {
        return new ArrayList<>(_deque);
    }

    public T get_b()
    {
        return _deque.get(0);
    }

    public T get_b_plus1()
    {
        return _deque.get(1);
    }

    public T get_t()
    {
        return _deque.get(_deque.size() - 1);
    }

    public T get_t_minus1()
    {
        return _deque.get(_deque.size() - 2);
    }

    @Override
    public String toString()
    {
        return _deque.toString();
    }

    public void clear()
    {
        _deque.clear();
    }

    public boolean isEmpty()
    {
        return _deque.isEmpty();
    }

    public int size()
    {
        return _deque.size();
    }

    public T get(int index)
    {
        return _deque.get(index);
    }

    public MelkmanDeque<T> copy()
    {
        return new MelkmanDeque<T>((LinkedList<T>)_deque.clone());
    }
}
