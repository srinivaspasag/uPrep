package com.vedantu.events.utils.daemons;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.google.gson.Gson;
import com.vedantu.commons.enums.EventType;
import com.vedantu.eventbus.events.Events;
import com.vedantu.eventbus.utils.Consumer;
import com.vedantu.eventbus.utils.Producer;
import com.vedantu.eventbus.utils.Task;
import com.vedantu.events.task.apis.IConsumer;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.apis.IProducer;

public class EventThreadManager {

    private static final ALogger         LOGGER                          = Logger.of(EventThreadManager.class);

    private static EventThreadManager    instance;
    private final int                    defaultSleepOnNoProduceInMillis = 5000;
    private final int                    defaultNThreads                 = 1;
    private final int                    defaultBatchSize                = 10;
    private Map<EventType, Integer>      nThreads;
    private Map<EventType, Task<Events>> tasks;
    private Map<EventType, Integer>      batchSizes;
    private Map<EventType, Integer>      sleepOnNoProduceInMillis;

    private EventThreadManager() {

        super();
        loadAllTasks();
        loadBatchSizes();
        loadNumberOfThreadsForEventType();
        loadSleepOnNoProduceMillies();
    }

    public static EventThreadManager getInstance() {

        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    private static synchronized void createInstance() {

        if (instance == null) {
            instance = new EventThreadManager();
        }
    }

    public Set<EventType> startAll() {

        Set<EventType> startedEventTypes = new HashSet<EventType>();
        for (Entry<EventType, Task<Events>> eTask : tasks.entrySet()) {
            EventType eType = eTask.getKey();
            boolean started = start(eType);
            if (started) {
                startedEventTypes.add(eType);
            }
        }
        return startedEventTypes;
    }

    public Set<EventType> stopAll() {

        Set<EventType> stoppedEventTypes = new HashSet<EventType>();
        for (Entry<EventType, Task<Events>> eTask : tasks.entrySet()) {
            EventType eType = eTask.getKey();
            boolean stopped = stop(eType);
            if (stopped) {
                stoppedEventTypes.add(eType);
            }
        }
        return stoppedEventTypes;
    }

    @SuppressWarnings("unchecked")
    public boolean start(EventType eType) {

        if (null == eType || EventType.UNKNOWN == eType) {
            LOGGER.warn("will not start task for eventType : " + eType);
            return false;
        }

        boolean started = false;

        Task<Events> task = tasks.get(eType);

        boolean isRunning = task == null ? false : task.isRunning();
        if (!isRunning) {
            IConsumer<Events> consumer = new Consumer();
            final int batchSize = batchSizes.get(eType) != null ? batchSizes.get(eType)
                    : defaultBatchSize;
            IProducer<Events> producer = new Producer(eType, batchSize);
            final int numThreads = nThreads.get(eType) != null ? nThreads.get(eType)
                    : defaultNThreads;
            final int sleepTimeInMillis = sleepOnNoProduceInMillis.get(eType) != null ? sleepOnNoProduceInMillis
                    .get(eType) : defaultSleepOnNoProduceInMillis;
            task = new Task<Events>(eType.name(), producer, consumer, numThreads, sleepTimeInMillis);
            tasks.put(eType, task);
            LOGGER.info("a new thread has been started for : [eventType:" + eType.name()
                    + ", batchSize:" + batchSize + ", noThreads:" + numThreads + "]");
            started = true;
        } else {
            LOGGER.info("a thread for " + eType.name() + " is already running");
        }

        return started;
    }

    public boolean stop(EventType eType) {

        if (null == eType || EventType.UNKNOWN == eType) {
            LOGGER.warn("will not stop task for eventType : " + eType);
            return false;
        }

        boolean stopped = false;

        Task<Events> task = tasks.get(eType);

        boolean isRunning = task == null ? false : task.isRunning();
        if (isRunning) {
            LOGGER.info("stopping thread for : " + eType.name());
            task.stop();
            stopped = true;
        } else {
            LOGGER.info("no thread is running for " + eType.name());
        }

        return stopped;
    }

    public boolean status(EventType eType) {

        if (null == eType || EventType.UNKNOWN == eType) {
            LOGGER.warn("will not check status for task for eventType : " + eType);
            return false;
        }
        Task<Events> task = tasks.get(eType);
        boolean isRunning = task == null ? false : task.isRunning();
        if (isRunning) {
            LOGGER.info("thread is running for : " + eType.name());
        } else {
            LOGGER.info("no thread is running for: " + eType.name());
        }
        return isRunning;
    }

    private void loadAllTasks() {

        tasks = new HashMap<EventType, Task<Events>>();
        for (EventType eventType : EventType.values()) {
            if (EventType.UNKNOWN != eventType) {
                tasks.put(eventType, null);
            }
        }
    }

    private void loadBatchSizes() {

        batchSizes = new HashMap<EventType, Integer>();
        loadProperty("task.batch.size", batchSizes, defaultBatchSize);
        LOGGER.info(batchSizes + " : " + new Gson().toJson(batchSizes));
    }

    private void loadSleepOnNoProduceMillies() {

        sleepOnNoProduceInMillis = new HashMap<EventType, Integer>();
        loadProperty("task.sleep.time", sleepOnNoProduceInMillis, defaultSleepOnNoProduceInMillis);
        LOGGER.info(sleepOnNoProduceInMillis + " : " + sleepOnNoProduceInMillis);
    }

    private void loadNumberOfThreadsForEventType() {

        nThreads = new HashMap<EventType, Integer>();
        loadProperty("task.thread.number", nThreads, defaultNThreads);
        LOGGER.info(nThreads + " : " + new Gson().toJson(nThreads));
    }

    @SuppressWarnings("unchecked")
    private <N extends Number> void loadProperty(final String propertyName,
            final Map<EventType, N> valueMap, final N defaultValue) {

        try {
            String propertyValueUnitsStr = Play.application().configuration()
                    .getString(propertyName);
            if (StringUtils.isEmpty(propertyValueUnitsStr)) {
                LOGGER.warn("found null " + propertyName);
                return;
            }

            String[] propertyValueUnits = propertyValueUnitsStr.split(";");

            for (String propertyValueUnit : propertyValueUnits) {
                String eTypeStr = StringUtils.substringBefore(propertyValueUnit, ":");
                EventType eventType = EventType.getEventTypeByValue(eTypeStr);

                if (EventType.UNKNOWN == eventType) {
                    LOGGER.error("unknown eventType : " + eTypeStr + " from [" + propertyValueUnit
                            + "]");
                } else {
                    String propertyNValueStr = StringUtils.substringAfter(propertyValueUnit, ":");
                    N bSize = defaultValue;
                    try {
                        if (NumberUtils.isNumber(propertyNValueStr)) {
                            ;
                            if (bSize instanceof Long) {
                                bSize = (N) (Long) NumberUtils.toLong(propertyNValueStr);
                            } else if (bSize instanceof Integer) {
                                bSize = (N) (Integer) NumberUtils.toInt(propertyNValueStr);
                            }
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.error("can not cast " + propertyNValueStr + " to int.", e);
                        LOGGER.warn("will set batchSize to default (" + bSize + ") for " + eTypeStr);
                    }
                    valueMap.put(eventType, bSize);
                }
            }
        } catch (Exception e) {
            LOGGER.error("can not load " + propertyName + " property from application.conf file", e);
        }
        return;
    }

    public static void main(String[] args) {

        for (EventType e : EventType.values()) {
            System.out.print(";" + e.name());
        }
        String a = "LOGOUT:10;LOGIN:10";
        String[] bSizes = a.split(";");
        for (String b : bSizes) {
            int index = b.indexOf(":");
            String eTypeStr = b.substring(0, index);
            int bSize = Integer.parseInt(b.substring(index + 1));
            System.out.println(EventType.getEventTypeByValue(eTypeStr) + ", " + bSize);

        }
        try {
            System.out.println();
            Constructor<?> constructor = Class.forName("processors.LoginProcessor")
                    .getConstructor();
            IProcessor processor = (IProcessor) constructor.newInstance();
            System.out.println(processor.getClass());
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e) {

            e.printStackTrace();
        } catch (SecurityException e) {

            e.printStackTrace();
        } catch (InstantiationException e) {

            e.printStackTrace();
        } catch (IllegalAccessException e) {

            e.printStackTrace();
        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        } catch (InvocationTargetException e) {

            e.printStackTrace();
        }

    }
}