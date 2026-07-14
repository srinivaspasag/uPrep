package com.vedantu.commons;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import play.Play;

public class AsynExecutorService {
	private static AsynExecutorService INSTANCE;

	@SuppressWarnings("rawtypes")
	public final CompletionService COMPLETION_SERVICE;

	private <T extends Object> AsynExecutorService() {
		final int executureMaxPoolSize = Integer.parseInt(Play.application()
				.configuration().getString("asyn.executor.pool.size", "5"));
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
