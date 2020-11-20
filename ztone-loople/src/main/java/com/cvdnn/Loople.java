package com.cvdnn;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.cvdnn.concurrent.TaskPoolExecutor;
import com.cvdnn.exception.OnMainThreadException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Loople {
    public static final String TAG = "Loople";

    public static final MainHandle Main = new MainHandle(Looper.getMainLooper());
    public static final TaskHandle Task = new TaskHandle();

    public static class MainHandle extends Handler {

        public MainHandle(Looper looper) {
            super(looper);
        }

        public void asserts() {
            if (as()) {
                throw new OnMainThreadException("ERROR: Cannot execute in main thread!");
            }
        }

        public boolean as() {
            return Thread.currentThread() == Looper.getMainLooper().getThread();
        }

        /**
         * 验证是否需要在主线程中执行
         *
         * @param o
         * @param methodName
         * @param parameterTypes
         * @param <O>
         *
         * @return
         */
        public <O> boolean demand(@NonNull O o, @NonNull String methodName, Class<?>... parameterTypes) {

            return demand(o, null, methodName, parameterTypes);
        }

        /**
         * 验证是否需要在主线程中执行
         *
         * @param o
         * @param methodName
         * @param parameterTypes
         * @param <O>
         *
         * @return
         */
        public <O> boolean demand(@NonNull O o, Class<?> clazz, @NonNull String methodName, Class<?>... parameterTypes) {
            boolean result = false;

            if (o != null) {
                try {
                    Class<?> cls = clazz == null ? o.getClass() : clazz;
                    Method method = cls.getMethod(methodName, parameterTypes);
                    if (method != null) {
                        MainThread ann = method.getAnnotation(MainThread.class);
                        if (ann != null) {
                            result = true;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

            return result;
        }
    }

    public static class TaskHandle {
        public final TaskPoolExecutor Pool = new TaskPoolExecutor(TAG, 32);

        @SuppressLint("NewApi")
        public CompletableFuture allOf(List<Runnable> runnables) {
            return CompletableFuture.allOf(runnables
                    .stream()
                    .map(r -> CompletableFuture.runAsync(r, Pool))
                    .toArray(CompletableFuture[]::new)
            );
        }

        public final Future<?> schedule(Runnable r) {
            return Pool.submit(r);
        }

        public final <V> Future<V> schedule(Callable<V> c) {
            return Pool.submit(c);
        }

        public final Future<?> schedule(Runnable r, long delayMillis) {
            return Pool.schedule(r, delayMillis, MILLISECONDS);
        }

        public final <V> Future<V> schedule(Callable<V> c, long delayMillis) {
            return Pool.schedule(c, delayMillis, MILLISECONDS);
        }

        /**
         * 固定时间切片执行任务
         *
         * @param r
         * @param delayMillis
         */
        public final void slice(Runnable r, long delayMillis) {
            Pool.scheduleAtFixedRate(r, 0, delayMillis, MILLISECONDS);
        }

        /**
         * 固定时间切片执行任务
         *
         * @param r
         * @param initTime
         * @param delayMillis
         */
        public final void slice(Runnable r, long initTime, long delayMillis) {
            Pool.scheduleAtFixedRate(r, initTime, delayMillis, MILLISECONDS);
        }

        /**
         * 固定等待执行时间
         *
         * @param r
         * @param delayMillis
         */
        public final void chain(Runnable r, long delayMillis) {
            Pool.scheduleWithFixedDelay(r, 0, delayMillis, MILLISECONDS);
        }

        /**
         * 固定等待执行时间
         *
         * @param r
         * @param initTime
         * @param delayMillis
         */
        public final void chain(Runnable r, long initTime, long delayMillis) {
            Pool.scheduleWithFixedDelay(r, initTime, delayMillis, MILLISECONDS);
        }

        public final Future<?> cancel(Future<?>... futures) {
            if (futures != null) {
                try {
                    for (Future<?> f : futures) {
                        if (f != null) {
                            f.cancel(true);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

            return null;
        }

        public final void shutdown() {
            try {
                Pool.shutdownNow();
            } catch (Exception e) {
                // do nothing
            }
        }

        private TaskHandle() {
        }
    }

    public static void logPoolNum() {
        int count = Task.Pool.ThreadCount.get();
        int coreSize = Task.Pool.getCorePoolSize();
        int threadNum = Task.Pool.getThreadFactory().ThreadNum.get();

        String msg = String.format("TaskPool: %d / %d", count, threadNum);
        if (count == coreSize) {
//            CrashReport.postCatchedException(new RuntimeException("TaskPool: " + count));
            Log.e(TAG, msg);
        } else {
            Log.d(TAG, msg);
        }
    }
}
