package com.lms.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lms.common.vedantu.event.api.ConsumableIterator;
import com.lms.common.vedantu.event.api.CounsumableLatch;
import com.lms.common.vedantu.event.api.IConsumable;
import com.lms.common.vedantu.event.api.IConsumer;
import com.lms.common.vedantu.event.api.IProducer;
import com.lms.utils.daemons.EventThreadManager;
@Component
public class Task<E extends IConsumable> implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Task.class);
    private ExecutorService      EXECUTOR;
    private String               name;
    private IConsumer<E>         consumer;
    private IProducer<E>         producer;
    private int                  nThreads;
    private long                 sleepOnNoProduceMillis;
    private boolean              stopped;

    private Thread               thread;

    public void task(final String name, IProducer<E> producer, IConsumer<E> consumer, int nThreads,
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
            logger.info("[" + name + "] stoping the thread");
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
            logger.debug("Producer produced number of events "+ consumableIterator.size());
            if (!consumableIterator.hasNext()) {
                if (0 == cycle) {
                    logger.info("[" + name + "] no entry found in collection");
                }
                try {
                    if (0 == cycle) {
                        logger.info("[" + name + "] sleeping for " + sleepOnNoProduceMillis + "ms");
                    }
                    Thread.sleep(sleepOnNoProduceMillis);
                    cycle--;
                } catch (InterruptedException e) {
                    logger.error("[" + name + "] exception on thread");
                } finally {
                    if (0 == cycle) {
                        logger.info("[" + name + "] thread woke up again");
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
                                logger.error(
                                        "[" + name + "] caught error on processing consumable : "
                                                + consumable, t);
                            }

                            signal.consumed(isConsumed);
                        }
                    });
                }

                try {
                    logger.debug("[" + name + "] waiting now to finish "
                            + consumableIterator.size() + " events");
                    signal.await();
                } catch (InterruptedException e) {
                    logger.error("[" + name + "] signal wait error", e);
                }

                int sCount = signal.getSuccessfullyConsumedCount();
                logger.info("[" + name + "] successfullyConsumedCount : " + sCount);

                if (sCount == 0) {
                    logger.error("[" + name + "] nothing consumed successfully. Will stop!!");
                    stop();
                }
            }
        }
    }
}

