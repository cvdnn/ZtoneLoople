package com.cvdnn.concurrent;

import android.log.Log;
import android.reflect.Clazz;

import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public final class TaskPoolExecutor extends ScheduledThreadPoolExecutor {
    private static final String TAG = "TaskPoolExecutor";

    private final int EXCEPTIONAL = Clazz.getFieldValue(FutureTask.class, "EXCEPTIONAL");
    public final AtomicInteger ThreadCount = new AtomicInteger(0);

    public TaskPoolExecutor(String name, int corePoolSize) {
        super(corePoolSize, new TaskThreadFactory(name), new com.cvdnn.concurrent.CallerRunsPolicy());
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);

        // 线程执行监控
        ThreadCount.incrementAndGet();
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        // 线程执行监控
        ThreadCount.decrementAndGet();

        if (r instanceof FutureTask) {
            int state = Clazz.getFieldValue(FutureTask.class, r, "state");
            if (state == EXCEPTIONAL) {
//                CrashReport.postCatchedException(Clazz.getFieldValue(FutureTask.class, r, "outcome"), Thread.currentThread(), true);
                Object obj = Clazz.getFieldValue(FutureTask.class, r, "outcome");
                if (obj instanceof Throwable) {
                    Log.e(TAG, (Throwable) obj);
                }
            }
        }
    }

    @Override
    public TaskThreadFactory getThreadFactory() {
        return (TaskThreadFactory) super.getThreadFactory();
    }
}
