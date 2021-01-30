package edu.vt.ece.PairLocking.list;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FineList<T> implements ListAbstract<T>
{
    /**
     * First list entry
     */
    private Node head;
    /**
     * Constructor
     */
    public FineList()
    {
        // Add sentinels to start and end
        head      = new Node(Integer.MIN_VALUE);
        Node tail = new Node(Integer.MAX_VALUE);
        head.next = tail;
//        while (!head.next.compareAndSet(null, tail, false, false)) ;
        System.out.println("The Modified Fine List object created");
    }
    /**
     * Add an element.
     * @param item element to add
     * @return true iff element was not there already
     */
    public boolean add(T item)
    {
//        System.out.println("Fine List adding "+item);
        int key = item.hashCode();

        // Initialize the window object
        Window tmpWin = new Window(head,head.next);

        while(true)
        {
            if(tmpWin.pairLock())
            {
                try
                {
                    // Pair Locking
                    while (tmpWin.curr.key < key)
                    {
                        // If we're not there, unlock the nodes and try again
                        tmpWin.pairUnlock();

                        //
                        tmpWin.pred = tmpWin.curr;
                        tmpWin.curr = tmpWin.curr.next;

                        // Try to acquire the locks: The individual node locks are timeout locks
                        // The function PairLock function will return False if timeout occurs for either of locks
                        // In that case we need to retry lock acquisition for both nodes again
                        while(true)
                        {
                            if(tmpWin.pairLock())
                            {
                                break;
                            }
                        }
                    }
                    if (tmpWin.curr.key == key)
                    {
                        return false;
                    }

                    // If we got this far, it means we can add
                    Node newNode = new Node(item);
                    newNode.next = tmpWin.curr;
                    tmpWin.pred.next = newNode;
//                    System.out.println("Add Successful");
                    return true;
                }
                finally
                {
                    tmpWin.pairUnlock();
                }
            }
        }
    }
    /**
     * Remove an element.
     * @param item element to remove
     * @return true iff element was present
     */
    public boolean remove(T item)
    {
//        System.out.println("Fine List Removing "+item);
        int key = item.hashCode();

        // Initialize the window object
        Window tmpWin = new Window(head, head.next);

        while(true)
        {
            if(tmpWin.pairLock())
            {
                try
                {
                    // Pair Locking
                    while (tmpWin.curr.key < key)
                    {
                        // If we're not there, unlock the nodes and try again
                        tmpWin.pairUnlock();

                        //
                        tmpWin.pred = tmpWin.curr;
                        tmpWin.curr = tmpWin.curr.next;

                        // Try to acquire the locks: The individual node locks are timeout locks
                        // The function PairLock function will return False if timeout occurs for either of locks
                        // In that case we need to retry lock acquisition for both nodes again
                        while(true)
                        {
                            if(tmpWin.pairLock())
                            {
                                break;
                            }
                        }
                    }
                    if (tmpWin.curr.key == key)
                    {
                        tmpWin.pred.next = tmpWin.curr.next;
//                        System.out.println("Remove Successful");
                        return true;
                    }

                    // If we got this far, it means we didn't find the node
                    return false;
                }
                finally
                {
                    tmpWin.pairUnlock();
                }
            }
        }
    }

    public boolean contains(T item)
    {
//        System.out.println("Fine List Checking "+item);
        int key = item.hashCode();

        // Initialize the window object
        Window tmpWin = new Window(head, head.next);

        while(true)
        {
            if(tmpWin.pairLock())
            {
                try
                {
                    // Pair Locking
                    while (tmpWin.curr.key < key)
                    {
                        // If we're not there, unlock the nodes and try again
                        tmpWin.pairUnlock();

                        //
                        tmpWin.pred = tmpWin.curr;
                        tmpWin.curr = tmpWin.curr.next;

                        // Try to acquire the locks: The individual node locks are timeout locks
                        // The function PairLock function will return False if timeout occurs for either of locks
                        // In that case we need to retry lock acquisition for both nodes again
                        while(true)
                        {
                            if(tmpWin.pairLock())
                            {
                                break;
                            }
                        }
                    }
                    // Return whether we got it or not
                    return (tmpWin.curr.key == key);
                }
                finally
                {
                    tmpWin.pairUnlock();
                }
            }
        }
    }
    /**
     * list Node
     */
    private class Node
    {
        T item;    // Item
        int key;   // Key
        Node next;

//        AtomicMarkableReference<Node> next;

        Lock lock; // Lock

        private volatile AtomicBoolean m_Flag = new AtomicBoolean(false);

        //Constructor
        Node(T item)
        {
            this.item = item;
            this.key = item.hashCode();
            this.lock = new ReentrantLock();
        }
        /**
         * Constructor for sentinel Node
         * @param key should be min or max int value
         */
        Node(int key)
        {
            this.item = null;
            this.key = key;
            this.lock = new ReentrantLock();
        }
        /**
         * Lock Node
         */
        boolean lock()
        {
            try
            {
                return lock.tryLock(200, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
//            System.out.println("Acquired");
            return false;
        }
        /**
         * Unlock Node
         */
        void unlock()
        {
            lock.unlock();
            m_Flag.set(false);
//            System.out.println("Exiting");
        }
    }

    /**
     * Pair of adjacent list entries.
     */
    class Window
    {
        public Node pred;
        public Node curr;

        Window(Node pred, Node curr)
        {
            this.pred = pred;
            this.curr = curr;
        }

        // Get a pair lock
        boolean pairLock()
        {
            while(true)
            {
                if (pred.lock())
                {
                    if (curr.lock())
                    {
                        break;
                    }
                    else
                    {
                        pred.unlock();
                    }
                }
            }
//            System.out.println("PairLock Acquired");
            return true;
        }
        // Unlock the pair
        void pairUnlock()
        {
            pred.unlock();
            curr.unlock();
//            System.out.println("PairLock Released");
        }
    }
}
