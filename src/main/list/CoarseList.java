package edu.vt.ece.PairLocking.list;

/*
 * CoarseList.java
 *
 * Created on January 3, 2006, 5:02 PM
 *
 * From "Multiprocessor Synchronization and Concurrent Data Structures",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */

/**
 * List using coarse-grained synchronization.
 * @param T Item type.
 * @author Maurice Herlihy
 */

import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;


public class CoarseList<T> implements ListAbstract<T>
{
    /**
     * First list Node
     */
    private Node head;
    /**
     * Last list Node
     */
    private Node tail;
    /**
     * Synchronizes access to list
     */
//    private Lock lock = new ReentrantLock();
    private volatile AtomicBoolean m_Flag = new AtomicBoolean(false);

    /**
     * Constructor
     */
    public CoarseList()
    {
        // Add sentinels to start and end
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);
        head.next = this.tail;
    }

    /**
     * Add an element.
     *
     * @param item element to add
     * @return true iff element was not there already
     */

    public boolean add(T item)
    {
        System.out.println("CoarseList adding "+item);
        Node pred, curr;
        int key = item.hashCode();

        //lock.lock();
        while(m_Flag.get())
        {
            synchronized (this)
            {
                try
                {
                    // Wait since there are more threads than allowed to contest
                    wait();
//                    System.out.println("Waiting");
                }
                catch (java.lang.InterruptedException e)
                {
                    System.out.println("Thread Interrupted");
                    e.printStackTrace();
                }
            }
        }

        try
        {
            m_Flag.set(true);
//            System.out.println("Entered");

            pred = head;
            curr = pred.next;
            while (curr.key < key)
            {
                pred = curr;
                curr = curr.next;
            }
            if (key == curr.key)
            {
                return false;
            } else
            {
                Node node = new Node(item);
                node.next = curr;
                pred.next = node;
                return true;
            }
        }
        finally
        {
            //lock.unlock();
            m_Flag.set(false);
            System.out.println("Exiting");
            synchronized (this)
            {
                // Wake the first thread that called wait
                notify();
                System.out.println("Notifying");
            }
        }
    }

    /**
     * Remove an element.
     *
     * @param item element to remove
     * @return true iff element was present
     */
    public boolean remove(T item)
    {
        System.out.println("CoarseList removing "+item);
        Node pred, curr;
        int key = item.hashCode();

//        lock.lock();
        while(m_Flag.get())
        {
            synchronized (this)
            {
                try
                {
                    // Wait since there are more threads than allowed to contest
                    wait();
//                    System.out.println("Waiting");
                }
                catch (java.lang.InterruptedException e)
                {
                    System.out.println("Thread Interrupted");
                    e.printStackTrace();
                }
            }
        }

        try
        {
            m_Flag.set(true);
//            System.out.println("Entered");

            pred = this.head;
            curr = pred.next;
            while (curr.key < key)
            {
                pred = curr;
                curr = curr.next;
            }
            if (key == curr.key)
            {  // present
                pred.next = curr.next;
                return true;
            } else
            {
                return false;         // not present
            }
        }
        finally
        {               // always unlock
//            lock.unlock();
            m_Flag.set(false);
            System.out.println("Exiting");
            synchronized (this)
            {
                // Wake the first thread that called wait
                notify();
                System.out.println("Notifying");
            }
        }
    }

    /**
     * Test whether element is present
     *
     * @param item element to test
     * @return true iff element is present
     */
    public boolean contains(T item)
    {
        System.out.println("CoarseList checking "+item);
        Node pred, curr;
        int key = item.hashCode();

//        lock.lock();
        while(m_Flag.get())
        {
            synchronized (this)
            {
                try
                {
                    // Wait since there are more threads than allowed to contest
                    wait();
//                    System.out.println("Waiting");
                }
                catch (java.lang.InterruptedException e)
                {
                    System.out.println("Thread Interrupted");
                    e.printStackTrace();
                }
            }
        }

        try
        {
            m_Flag.set(true);
//            System.out.println("Entered");

            pred = head;
            curr = pred.next;
            while (curr.key < key)
            {
                pred = curr;
                curr = curr.next;
            }
            return (key == curr.key);
        }
        finally
        {               // always unlock
//            lock.unlock();
            m_Flag.set(false);
            System.out.println("Exiting");
            synchronized (this)
            {
                // Wake the first thread that called wait
                notify();
                System.out.println("Notifying");
            }
        }
    }

    /**
     * list Node
     */
    private class Node
    {
        /**
         * actual item
         */
        T item;
        /**
         * item's hash code
         */
        int key;
        /**
         * next Node in list
         */
        Node next;

        /**
         * Constructor for usual Node
         *
         * @param item element in list
         */
        Node(T item)
        {
            this.item = item;
            this.key = item.hashCode();
        }

        /**
         * Constructor for sentinel Node
         *
         * @param key should be min or max int value
         */
        Node(int key)
        {
            this.item = null;
            this.key = key;
        }
    }
}
