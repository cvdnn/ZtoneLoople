package com.cvdnn.concurrent;

public final class TaskThread extends Thread {
    private static final String TAG = "TaskThread";

    public TaskThread(String name, Runnable target) {
        super((System.getSecurityManager() != null) ? System.getSecurityManager().getThreadGroup() : Thread.currentThread().getThreadGroup(), target, name, 0);
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void run() {
        super.run();
    }
}
