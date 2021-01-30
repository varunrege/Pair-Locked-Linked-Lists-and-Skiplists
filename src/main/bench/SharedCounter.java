package edu.vt.ece.PairLocking.bench;

import edu.vt.ece.PairLocking.list.ListAbstract;
import java.lang.Math;

/**
 *
 * @author Mohamed M. Saad
 */
public class SharedCounter extends Counter
{
    // Constants
    private  static final int m_Size = 100;
    private static final int m_Add = 0;
    private static final int m_Remove = 1;
    private static final int m_Contains = 2;

    private ListAbstract m_list;
    private int m_ContainsPercentage;
    ThreadLocal<Integer> m_FunctionCallCount = new ThreadLocal<Integer>() {
        protected Integer initialValue()
        {
            return 0;
        }
    };

    public SharedCounter(int c, ListAbstract n_list, int containPercentage)
    {
        super(c);
        this.m_list = n_list;
        this.m_ContainsPercentage = containPercentage;
//        m_FunctionCallCount =
        this.m_FunctionCallCount.set(0);
    }

    @Override
    public int getAndIncrement()
    {
        // Function decider
        int callCount = this.m_FunctionCallCount.get();
        this.m_FunctionCallCount.set( ++callCount);
        // Number to be added
        Integer Number = (int)(Math.random() * m_Size);

        int temp = -1;

        switch (FunctionDecider())
        {
            case m_Remove:
            {
                this.m_list.remove(Number);
                temp = super.getAndIncrement();
//                System.out.println("Removing "+Number);
                break;
            }
            case m_Contains:
            {
                this.m_list.contains(Number);
                temp = super.getAndIncrement();
//                System.out.println("Checking for "+Number);
                break;
            }
            case m_Add:
            default:
            {
                this.m_list.add(Number);
                temp = super.getAndIncrement();
//                System.out.println("Adding "+Number);
                break;
            }
        }
        return temp;
    }

    private int FunctionDecider()
    {
        int currentValue = m_FunctionCallCount.get()%10;
        int perTen = m_ContainsPercentage/10;

        //System.out.println("CurrentValue "+currentValue+" PerTen "+perTen);
        if(currentValue >= 10-perTen)
            return m_Contains;
        else if (currentValue%2 == 0)
        {
            return m_Add;
        }
        else
            return m_Remove;
    }
}