package edu.vt.ece.PairLocking.list;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LazyList<T> implements ListAbstract<T>
{
    /**
     * First list Node
     */
    private Node head;
    /**
     * Constructor
     */
    public LazyList() {
        // Add sentinels to start and end
        this.head  = new Node(Integer.MIN_VALUE);
        this.head.next = new Node(Integer.MAX_VALUE);
    }

    /**
     * Check that prev and curr are still in list and adjacent
     */
    private boolean validate(Node pred, Node curr) {
        return  !pred.marked && !curr.marked && pred.next == curr;
    }
    /**
     * Add an element.
     * @param item element to add
     * @return true iff element was not there already
     */
    public boolean add(T item) {
        System.out.println("Lazy List adding "+item);
        int key = item.hashCode();
        while (true) {
            Node pred = this.head;
            Node curr = head.next;
            while (curr.key < key) {
                pred = curr; curr = curr.next;
            }
            pred.lock();
            try {
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.key == key) { // present
                            return false;
                        } else {               // not present
                            Node Node = new Node(item);
                            Node.next = curr;
                            pred.next = Node;
                            return true;
                        }
                    }
                } finally { // always unlock
                    curr.unlock();
                }
            } finally { // always unlock
                pred.unlock();
            }
        }
    }
    /**
     * Remove an element.
     * @param item element to remove
     * @return true iff element was present
     */
    public boolean remove(T item) {
        System.out.println("Lazy List removing "+item);
        int key = item.hashCode();
        while (true) {
            Node pred = this.head;
            Node curr = head.next;
            while (curr.key < key) {
                pred = curr; curr = curr.next;
            }
            pred.lock();
            try {
                curr.lock();
                try {
                    if (validate(pred, curr)) {
                        if (curr.key != key) {    // present
                            return false;
                        } else {                  // absent
                            curr.marked = true;     // logically remove
                            pred.next = curr.next;  // physically remove
                            return true;
                        }
                    }
                } finally {                   // always unlock curr
                    curr.unlock();
                }
            } finally {                     // always unlock pred
                pred.unlock();
            }
        }
    }
    /**
     * Test whether element is present
     * @param item element to test
     * @return true iff element is present
     */
    public boolean contains(T item) {
        System.out.println("Lazy List checking "+item);
        int key = item.hashCode();
        Node curr = this.head;
        while (curr.key < key)
            curr = curr.next;
        return curr.key == key && !curr.marked;
    }
    /**
     * list Node
     */
    private class Node {
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
         * If true, Node is logically deleted.
         */
        boolean marked;
        /**
         * Synchronizes Node.
         */
        Lock lock;
        private volatile AtomicBoolean m_Flag = new AtomicBoolean(false);
        /**
         * Constructor for usual Node
         * @param item element in list
         */
        Node(T item) {      // usual constructor
            this.item = item;
            this.key = item.hashCode();
            this.next = null;
            this.marked = false;
            this.lock = new ReentrantLock();
        }
        /**
         * Constructor for sentinel Node
         * @param key should be min or max int value
         */
        Node(int key) { // sentinel constructor
            this.item = null;
            this.key = key;
            this.next = null;
            this.marked = false;
            this.lock = new ReentrantLock();
        }
        /**
         * Lock Node
         */
        void lock()
        {
//            lock.lock();
            while(m_Flag.get())
            {
                synchronized (this)
                {
                    try
                    {
                        // Wait since there are more threads than allowed to contest
                        wait();
//                        System.out.println("Waiting");
                    }
                    catch (java.lang.InterruptedException e)
                    {
                        System.out.println("Thread Interrupted");
                        e.printStackTrace();
                    }
                }
            }

            if(!m_Flag.getAndSet(true))
            {
                return;
            }
        }
        /**
         * Unlock Node
         */
        void unlock()
        {
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
}
