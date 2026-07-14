package com.lms.common;


import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.*;

public class AsynExecutorService {
    private static AsynExecutorService INSTANCE;
    @SuppressWarnings("rawtypes")
    public final CompletionService COMPLETION_SERVICE;
    @Value("${asyn.executor.pool.size}")
    public String poolSize;

    private <T extends Object> AsynExecutorService() {
        final int executureMaxPoolSize = Integer.parseInt(poolSize);
        final boolean enqueueFairly = true;
        final BlockingQueue<Runnable> workQueue = new SynchronousQueue<Runnable>(
                enqueueFairly);
        final RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        final ExecutorService EXECUTOR = new ThreadPoolExecutor(0,
                executureMaxPoolSize, 60L, TimeUnit.SECONDS, workQueue,
                rejectedExecutionHandler);
        COMPLETION_SERVICE = new ExecutorCompletionService<T>(EXECUTOR);
    }

    public static AsynExecutorService getInstance() {
        if (INSTANCE == null) {
            createInstance();
        }
        return INSTANCE;
    }

    private static synchronized void createInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AsynExecutorService();
        }
    }
}
