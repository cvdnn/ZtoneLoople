package com.ztone.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

public final class RunState extends AtomicBoolean {

    /**
     * Creates a new {@code RunState} with the given initial value.
     *
     * @param initialValue the initial value
     */
    public RunState(boolean initialValue) {
        super(initialValue);
    }
}
