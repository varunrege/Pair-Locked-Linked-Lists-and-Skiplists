package edu.vt.ece.PairLocking.list;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OptimisticList<T> implements ListAbstract<T>
{
    /**
     * First list entry
     */
    private Entry head;
    /**
     * Constructor
     */
    public OptimisticList() {
        this.head  = new Entry(Integer.MIN_VALUE);
        this.head.next = new Entry(Integer.MAX_VALUE);
    }
    /**
     * Add an element.
     * @param item element to add
     * @return true iff element was not there already
     */
    public boolean add(T item) {
        System.out.println("Optimistic adding "+item);
        int key = item.hashCode();
        while (true) {
            Entry pred = this.head;
            Entry curr = pred.next;
            while (curr.key <= key) {
                pred = curr; curr = curr.next;
            }
            pred.lock(); curr.lock();
            try {
                if (validate(pred, curr)) {
                    if (curr.key == key) { // present
                        return false;
                    } else {               // not present
                        Entry entry = new Entry(item);
                        entry.next = curr;
                        pred.next = entry;
                        return true;
                    }
                }
            } finally {                // always unlock
                pred.unlock(); curr.unlock();
            }
        }
    }
    /**
     * Remove an element.
     * @param item element to remove
     * @return true iff element was present
     */
    public boolean remove(T item) {
        System.out.println("Optimistic removing "+item);
        int key = item.hashCode();
        while (true) {
            Entry pred = this.head;
            Entry curr = pred.next;
            while (curr.key < key) {
                pred = curr; curr = curr.next;
            }
            pred.lock(); curr.lock();
            try {
                if (validate(pred, curr)) {
                    if (curr.key == key) { // present in list
                        pred.next = curr.next;
                        return true;
                    } else {               // not present in list
                        return false;
                    }
                }
            } finally {                // always unlock
                pred.unlock(); curr.unlock();
            }
        }
    }
    /**
     * Test whether element is present
     * @param item element to test
     * @return true iff element is present
     */
    public boolean contains(T item) {
        System.out.println("Optimistic checking "+item);
        int key = item.hashCode();
        while (true) {
            Entry pred = this.head; // sentinel node;
            Entry curr = pred.next;
            while (curr.key < key) {
                pred = curr; curr = curr.next;
            }
            try {
                pred.lock(); curr.lock();
                if (validate(pred, curr)) {
                    return (curr.key == key);
                }
            } finally {                // always unlock
                pred.unlock(); curr.unlock();
            }
        }
    }
    /**
     * Check that prev and curr are still in list and adjacent
     * @param pred predecessor node
     * @param curr current node
     * @return whther predecessor and current have changed
     */
    private boolean validate(Entry pred, Entry curr) {
        Entry entry = head;
        while (entry.key <= pred.key) {
            if (entry == pred)
                return pred.next == curr;
            entry = entry.next;
        }
        return false;
    }
    /**
     * list entry
     */
    private class Entry {
        /**
         * actual item
         */
        T item;
        /**
         * item's hash code
         */
        int key;
        /**
         * next entry in list
         */
        Entry next;
        /**
         * Synchronizes entry.
         */
        Lock lock;
        private volatile AtomicBoolean m_Flag = new AtomicBoolean(false);
        /**
         * Constructor for usual entry
         * @param item element in list
         */
        Entry(T item) {
            this.item = item;
            this.key = item.hashCode();
            lock = new ReentrantLock();
        }
        /**
         * Constructor for sentinel entry
         * @param key should be min or max int value
         */
        Entry(int key) {
            this.key = key;
            lock = new ReentrantLock();
        }
        /**
         * Lock entry
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
         * Unlock entry
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
