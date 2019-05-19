package com.rideaustin.schedulers;

import android.os.Process;
import android.support.annotation.VisibleForTesting;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by rost on 8/12/16.
 */
public class RxSchedulers {

    private static final int NETWORK_CORE_POOL_SIZE = 4;
    private static final int NETWORK_MAX_POOL_SIZE = 20;
    private static final long NET_THREAD_KEEP_ALIVE_S = 10L;

    private static Scheduler network;
    private static Scheduler eventPolling;
    private static Scheduler serializer;
    private static Scheduler computation;
    private static Scheduler main;
    private static Scheduler.Worker mainWorker;

    @VisibleForTesting
    static SchedulersFactory factory;

    public static void init() {
        if (factory == null) {
            factory = new DefaultSchedulersFactory();
        }
        network = factory.createNetworkScheduler();
        eventPolling = factory.createEventPollingScheduler();
        serializer = factory.createSerializerScheduler();
        computation = factory.createComputationScheduler();
        main = factory.createMainScheduler();
        mainWorker = main.createWorker();
    }


    public static Scheduler main() {
        return main;
    }

    public static Scheduler network() {
        return network;
    }

    public static Scheduler eventPolling() {
        return eventPolling;
    }

    public static Scheduler computation() {
        return computation;
    }

    public static Scheduler serializer() {
        return serializer;
    }

    public static Subscription schedule(Runnable runnable) {
        return mainWorker.schedule(runnable::run);
    }

    public static Subscription schedule(Runnable runnable, long delayTime, TimeUnit timeUnit) {
        return mainWorker.schedule(runnable::run, delayTime, timeUnit);
    }

    private static Executor createEventPollingExecutor() {
        return Executors.newSingleThreadExecutor(new PriorityThreadFactory("polling", Process.THREAD_PRIORITY_FOREGROUND));
    }

    private static Executor createNetworkExecutor() {
        final ThreadPoolExecutor networkExecutor = new ThreadPoolExecutor(
                NETWORK_CORE_POOL_SIZE, NETWORK_MAX_POOL_SIZE, NET_THREAD_KEEP_ALIVE_S, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new PriorityThreadFactory("net-thread-pool", Process.THREAD_PRIORITY_BACKGROUND)
        );
        networkExecutor.allowCoreThreadTimeOut(true);
        return networkExecutor;
    }

    private static Executor createComputationExecutor() {
        int size = Math.max(Runtime.getRuntime().availableProcessors(), 4);
        return Executors.newFixedThreadPool(size);
    }

    private static class PriorityThreadFactory implements ThreadFactory {
        private final String name;
        private final int priority;
        private int threadId = -1;

        public PriorityThreadFactory(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public Thread newThread(final Runnable r) {
            threadId++;
            return new Thread(name + "-" + threadId) {
                @Override
                public void run() {
                    Process.setThreadPriority(priority);
                    r.run();
                }
            };
        }
    }

    @VisibleForTesting
    public interface SchedulersFactory {

        Scheduler createMainScheduler();

        Scheduler createComputationScheduler();

        Scheduler createSerializerScheduler();

        Scheduler createEventPollingScheduler();

        Scheduler createNetworkScheduler();
    }

    public static class DefaultSchedulersFactory implements SchedulersFactory {

        @Override
        public Scheduler createMainScheduler() {
            return AndroidSchedulers.mainThread();
        }

        @Override
        public Scheduler createComputationScheduler() {
            return Schedulers.from(createComputationExecutor());
        }

        @Override
        public Scheduler createSerializerScheduler() {
            return Schedulers.from(Executors.newSingleThreadExecutor());
        }

        @Override
        public Scheduler createEventPollingScheduler() {
            return Schedulers.from(createEventPollingExecutor());
        }

        @Override
        public Scheduler createNetworkScheduler() {
            return Schedulers.from(createNetworkExecutor());
        }
    }
}
