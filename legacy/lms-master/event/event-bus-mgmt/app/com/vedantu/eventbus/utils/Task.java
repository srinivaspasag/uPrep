package com.vedantu.eventbus.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.events.task.apis.ConsumableIterator;
import com.vedantu.events.task.apis.CounsumableLatch;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IConsumer;
import com.vedantu.events.task.apis.IProducer;

public class Task<E extends IConsumable> implements Runnable {

    private static final ALogger LOGGER = Logger.of(Task.class);
    private ExecutorService      EXECUTOR;
    private String               name;
    private IConsumer<E>         consumer;
    private IProducer<E>         producer;
    private int                  nThreads;
    private long                 sleepOnNoProduceMillis;
    private boolean              stopped;

    private Thread               thread;

    public Task(final String name, IProducer<E> producer, IConsumer<E> consumer, int nThreads,
            long sleepOnNoProduceInMillis) {

        this.name = name;
        this.producer = producer;
        this.consumer = consumer;
        this.sleepOnNoProduceMillis = sleepOnNoProduceInMillis;
        this.nThreads = nThreads;
        this.stopped = false;
        this.thread = new Thread(this);
        start();
    }

    public synchronized void start() {

        if (null == EXECUTOR || EXECUTOR.isShutdown()) {
            EXECUTOR = Executors.newFixedThreadPool(nThreads);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public synchronized void stop() {

        if (null != EXECUTOR && !EXECUTOR.isShutdown()) {
            LOGGER.info("[" + name + "] stoping the thread");
            EXECUTOR.shutdown();
            this.stopped = true;
        }
    }

    public boolean isRunning() {

        return !stopped;
    }

    private static final long MAX_MILLIS_FOR_NO_PRODUCE_LOGS = 100000L;

    public void run() {

        final long maxCycles = MAX_MILLIS_FOR_NO_PRODUCE_LOGS / sleepOnNoProduceMillis;
        long cycle = maxCycles;
        while (!stopped) {
            ConsumableIterator<E> consumableIterator = producer.produce();
            LOGGER.debug("Producer produced number of events "+ consumableIterator.size());
            if (!consumableIterator.hasNext()) {
                if (0 == cycle) {
                    LOGGER.info("[" + name + "] no entry found in collection");
                }
                try {
                    if (0 == cycle) {
                        LOGGER.info("[" + name + "] sleeping for " + sleepOnNoProduceMillis + "ms");
                    }
                    Thread.sleep(sleepOnNoProduceMillis);
                    cycle--;
                } catch (InterruptedException e) {
                    LOGGER.error("[" + name + "] exception on thread");
                } finally {
                    if (0 == cycle) {
                        LOGGER.info("[" + name + "] thread woke up again");
                        cycle = maxCycles;
                    }
                }
            } else {
                cycle = maxCycles;
                final CounsumableLatch signal = new CounsumableLatch(consumableIterator.size());
                while (consumableIterator.hasNext()) {
                    final E consumable = consumableIterator.next();
                    EXECUTOR.submit(new Runnable() {

                        @Override
                        public void run() {

                            boolean isConsumed = false;
                            try {
                                consumer.consume(consumable);
                                isConsumed = true;
                            } catch (Throwable t) {
                                LOGGER.error(
                                        "[" + name + "] caught error on processing consumable : "
                                                + consumable, t);
                            }

                            signal.consumed(isConsumed);
                        }
                    });
                }

                try {
                    LOGGER.debug("[" + name + "] waiting now to finish "
                            + consumableIterator.size() + " events");
                    signal.await();
                } catch (InterruptedException e) {
                    LOGGER.error("[" + name + "] signal wait error", e);
                }

                int sCount = signal.getSuccessfullyConsumedCount();
                LOGGER.info("[" + name + "] successfullyConsumedCount : " + sCount);

                if (sCount == 0) {
                    LOGGER.error("[" + name + "] nothing consumed successfully. Will stop!!");
                    stop();
                }
            }
        }
    }
}
