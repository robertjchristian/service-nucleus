package com.liaison.framework;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Process Manager (Stub)
 * <p/>
 * <P>Placeholder for Process Management
 *
 * @author Robert.Christian
 * @version 1.0
 */
public class ProcessManager {

    private static final Counter counter = new Counter();

    protected static int initTransaction() {
        return counter.init();
    }

    protected static int getPid() {
        return counter.get();
    }

}

class Counter extends ThreadLocal<Integer> {

    private static final AtomicInteger counter = new AtomicInteger(0);

    // on init, set incremented pid on thread local integer
    // NOTE:  not sufficient to simply increment per thread (ie in initialValue)
    // since threads are reused several times by the container... so we
    // blow away, increment, and set here...
    protected int init() {
        remove();
        set(counter.getAndIncrement());
        return get();
    }

}