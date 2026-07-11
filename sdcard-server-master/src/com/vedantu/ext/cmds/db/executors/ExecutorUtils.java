package com.vedantu.ext.cmds.db.executors;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 20, 1, TimeUnit.SECONDS,
                                                       new LinkedBlockingQueue<Runnable>());

    public static void executeTask(Runnable task) {

        executor.execute(task);
    }

    public static void stopExecutor() {

        executor.shutdown();
    }
}
