package edu.vt.ece.PairLocking.list;

public interface ListAbstract<T>
{
    boolean add(T item);
    boolean remove(T item);
    boolean contains(T item);
}
