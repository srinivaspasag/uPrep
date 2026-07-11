package com.vedantu.eventbus.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.enums.EventType;
import com.vedantu.eventbus.errors.ProcessorNotFoundException;
import com.vedantu.events.errors.TypeNotMatchedException;
import com.vedantu.events.task.apis.IProcessor;

public class ProcessorFactory {

	private static final ALogger LOGGER = Logger.of(ProcessorFactory.class);
	private static ProcessorFactory instance;
	private Map<EventType, IProcessor> processors;

	private ProcessorFactory() throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		super();
		processors = new HashMap<EventType, IProcessor>();
		for (EventType eventType : EventType.values()) {
			String processorClassName = eventType.getEventProcessorClass();
			if (StringUtils.isNotEmpty(processorClassName)) {
				Class<?> processorClass = Class.forName(processorClassName
						.trim());
				Constructor<?> constructor = processorClass.getConstructor();
				IProcessor processor = (IProcessor) constructor.newInstance();
				processors.put(eventType, processor);
			} else {
				LOGGER.warn("no processorClass defined for : " + eventType);
			}

		}
	}

	public static ProcessorFactory getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			try {
				instance = new ProcessorFactory();
			} catch (Exception e) {
				LOGGER.error("couldn't load processor factory", e);
				throw new RuntimeException(e);
				// TODO: find corresponding method in play.2.x
				// Play.stop();
			}
		}
	}

	public IProcessor getProcessor(EventType type)
			throws TypeNotMatchedException, ProcessorNotFoundException {
		if (!EventType.isValidEventType(type.name())) {
			throw new TypeNotMatchedException();
		}

		if (!processors.containsKey(type)) {
			throw new ProcessorNotFoundException();
		}
		return processors.get(type);
	}

}
