package com.lms.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.event.api.IProcessor;
import com.lms.common.vedantu.event.errors.TypeNotMatchedException;
import com.lms.events.errors.ProcessorNotFoundException;
import com.lms.processors.AddSolutionProcessor;
@Component
public class ProcessorFactory {

	private static final Logger logger = LoggerFactory.getLogger(ProcessorFactory.class);
	private static ProcessorFactory instance;
	private Map<EventType, IProcessor> processors;
	@Autowired
    private AddSolutionProcessor addSolutionProcessor;
	public void setProcessorFactory() throws ClassNotFoundException, NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		processors = new HashMap<EventType, IProcessor>();
		for (EventType eventType : EventType.values()) {
			String processorClassName = eventType.getEventProcessorClass();
			if (!StringUtils.isEmpty(processorClassName)) {
				//Class<?> processorClass = Class.forName(processorClassName.trim());
				//Constructor<?> constructor = processorClass.getConstructor();
				//IProcessor processor = (IProcessor) constructor.newInstance();
				if(eventType == EventType.ADD_SOLUTION) {
				addSolutionProcessor.setAddSolutionProcessor();
				IProcessor processor = addSolutionProcessor;
				processors.put(eventType, processor);
				}
			} else {
				logger.warn("no processorClass defined for : " + eventType);
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
				logger.error("couldn't load processor factory", e);
				throw new RuntimeException(e);
				// TODO: find corresponding method in play.2.x
				// Play.stop();
			}
		}
	}

	public IProcessor getProcessor(EventType type) throws TypeNotMatchedException, ProcessorNotFoundException {
		if (!EventType.isValidEventType(type.name())) {
			throw new TypeNotMatchedException();
		}

		if (!processors.containsKey(type)) {
			throw new ProcessorNotFoundException();
		}
		return processors.get(type);
	}

}
