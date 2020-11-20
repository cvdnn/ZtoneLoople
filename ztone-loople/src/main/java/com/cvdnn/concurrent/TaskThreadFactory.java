package com.cvdnn.concurrent;

import android.text.TextUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class TaskThreadFactory implements ThreadFactory {
    private static final String TAG = "TaskThreadFactory";

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    public final AtomicInteger ThreadNum = new AtomicInteger(0);
    private final ThreadGroup group;
    private final String mNamePrefix;

    public TaskThreadFactory(String tag) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        mNamePrefix = (!TextUtils.isEmpty(tag) ? tag : "pool") + "-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        final String tdName = mNamePrefix + ThreadNum.getAndIncrement();
        TaskThread td = new TaskThread(tdName, r);

        if (td.isDaemon()) {
            td.setDaemon(false);
        }

        if (td.getPriority() != Thread.NORM_PRIORITY) {
            td.setPriority(Thread.NORM_PRIORITY);
        }

        return td;
    }
}