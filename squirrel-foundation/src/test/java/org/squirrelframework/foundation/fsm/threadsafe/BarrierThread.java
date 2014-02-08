package org.squirrelframework.foundation.fsm.threadsafe;

import java.util.concurrent.CyclicBarrier;

public class BarrierThread extends Thread {
    private CyclicBarrier entryBarrier;
    private CyclicBarrier exitBarrier;

    public BarrierThread(Runnable runnable, String name, CyclicBarrier entryBarrier, CyclicBarrier exitBarrier) {
        super(runnable, name);
        this.entryBarrier = entryBarrier;
        this.exitBarrier = exitBarrier;
    }
    
    @Override
    public void run() {
        try {
            entryBarrier.await();
            super.run();
            exitBarrier.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
