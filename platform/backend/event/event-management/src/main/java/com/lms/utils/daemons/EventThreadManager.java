package com.lms.utils.daemons;

import com.google.gson.Gson;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.event.api.IConsumer;
import com.lms.common.vedantu.event.api.IProcessor;
import com.lms.common.vedantu.event.api.IProducer;
import com.lms.events.Events;
import com.lms.utils.Consumer;
import com.lms.utils.Producer;
import com.lms.utils.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
@Component
public class EventThreadManager {

	private static final Logger logger = LoggerFactory.getLogger(EventThreadManager.class);

	private static EventThreadManager instance;
	private final int defaultSleepOnNoProduceInMillis = 5000;
	private final int defaultNThreads = 1;
	private final int defaultBatchSize = 10;
	private Map<EventType, Integer> nThreads;
	private Map<EventType, Task<Events>> tasks;
	private Map<EventType, Integer> batchSizes;
	private Map<EventType, Integer>      sleepOnNoProduceInMillis;
	@Autowired
    private Producer producer;
	@Autowired
	private Task<Events> task;
	@Autowired
	private Consumer consumer;
	@Value("${task.batch.size}")
	String batchSize;
	@Value("${task.thread.number}")
	String threadNumber;
	@Value("${task.sleep.time}")
	String sleepTime;
	public void eventThreadManager() {

		//super();
		if (tasks == null) {
			loadAllTasks();
			loadBatchSizes();
			loadNumberOfThreadsForEventType();
			loadSleepOnNoProduceMillies();
		}
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
			logger.warn("will not start task for eventType : " + eType);
			return false;
		}

		boolean started = false;

		Task<Events> task1 = tasks.get(eType);

		boolean isRunning = task1 != null && task1.isRunning();
		if (!isRunning) {
			IConsumer<Events> consumer1 = consumer;
			final int batchSize = batchSizes.get(eType) != null ? batchSizes.get(eType) : defaultBatchSize;
			producer.producer(eType, batchSize);
			IProducer<Events> producer1 = producer;
			final int numThreads = nThreads.get(eType) != null ? nThreads.get(eType) : defaultNThreads;
			final int sleepTimeInMillis = sleepOnNoProduceInMillis.get(eType) != null
					? sleepOnNoProduceInMillis.get(eType)
					: defaultSleepOnNoProduceInMillis;
			task.task(eType.name(), producer1, consumer1, numThreads, sleepTimeInMillis);		
			task1 = task; //new Task<Events>(eType.name(), producer1, consumer, numThreads, sleepTimeInMillis);
			tasks.put(eType, task1);
			logger.info("a new thread has been started for : [eventType:" + eType.name() + ", batchSize:" + batchSize
					+ ", noThreads:" + numThreads + "]");
			started = true;
		} else {
			logger.info("a thread for " + eType.name() + " is already running");
		}

		return started;
	}

	public boolean stop(EventType eType) {

		if (null == eType || EventType.UNKNOWN == eType) {
			logger.warn("will not stop task for eventType : " + eType);
			return false;
		}

		boolean stopped = false;

		Task<Events> task = tasks.get(eType);

		boolean isRunning = task != null && task.isRunning();
		if (isRunning) {
			logger.info("stopping thread for : " + eType.name());
			task.stop();
			stopped = true;
		} else {
			logger.info("no thread is running for " + eType.name());
		}

		return stopped;
	}

	public boolean status(EventType eType) {

		if (null == eType || EventType.UNKNOWN == eType) {
			logger.warn("will not check status for task for eventType : " + eType);
			return false;
		}
		Task<Events> task = tasks.get(eType);
		boolean isRunning = task != null && task.isRunning();
		if (isRunning) {
			logger.info("thread is running for : " + eType.name());
		} else {
			logger.info("no thread is running for: " + eType.name());
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
		loadProperty(batchSize, batchSizes, defaultBatchSize);
		logger.info(batchSizes + " : " + new Gson().toJson(batchSizes));
	}

	private void loadSleepOnNoProduceMillies() {

		sleepOnNoProduceInMillis = new HashMap<EventType, Integer>();
		loadProperty(sleepTime, sleepOnNoProduceInMillis, defaultSleepOnNoProduceInMillis);
		logger.info(sleepOnNoProduceInMillis + " : " + sleepOnNoProduceInMillis);
	}

	private void loadNumberOfThreadsForEventType() {

		nThreads = new HashMap<EventType, Integer>();
		loadProperty(threadNumber, nThreads, defaultNThreads);
		logger.info(nThreads + " : " + new Gson().toJson(nThreads));
	}

	@SuppressWarnings("unchecked")
	private <N extends Number> void loadProperty(final String propertyName, final Map<EventType, N> valueMap,
			final N defaultValue) {

		try {
	 String propertyValueUnitsStr = propertyName; //= Play.application().configuration().getString(propertyName);
			if (StringUtils.isEmpty(propertyValueUnitsStr)) {
				logger.warn("found null " + propertyName);
				return;
			}

			String[] propertyValueUnits = propertyValueUnitsStr.split(";");

			for (String propertyValueUnit : propertyValueUnits) {
				//String eTypeStr = StringUtils.substringBefore(propertyValueUnit, ":");
				String[] split = propertyValueUnit.split(":");
				String eTypeStr =  split[0];
				EventType eventType = EventType.getEventTypeByValue(eTypeStr);

				if (EventType.UNKNOWN == eventType) {
					logger.error("unknown eventType : " + eTypeStr + " from [" + propertyValueUnit + "]");
				} else {
					//String propertyNValueStr = StringUtils.substringAfter(propertyValueUnit, ":");
					
					String propertyNValueStr = split[1];
					N bSize = defaultValue;
					try {
						Boolean isNumaric = propertyNValueStr.chars().allMatch(Character::isDigit);
						if (isNumaric) {
							if (bSize instanceof Long) {
								bSize = (N) Long.valueOf(propertyNValueStr);
							} else if (bSize instanceof Integer) {
								bSize = (N) Integer.valueOf(propertyNValueStr);
							}
						}
					} catch (NumberFormatException e) {
						logger.error("can not cast " + propertyNValueStr + " to int.", e);
						logger.warn("will set batchSize to default (" + bSize + ") for " + eTypeStr);
					}
					valueMap.put(eventType, bSize);
				}
			}
		} catch (Exception e) {
			logger.error("can not load " + propertyName + " property from application.conf file", e);
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
			Constructor<?> constructor = Class.forName("processors.LoginProcessor").getConstructor();
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