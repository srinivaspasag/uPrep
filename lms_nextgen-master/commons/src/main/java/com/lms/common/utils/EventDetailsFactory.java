package com.lms.common.utils;

import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.event.api.EventRegistrar;
import com.lms.common.vedantu.event.api.IEventDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EventDetailsFactory {
	private static final Logger logger = LoggerFactory.getLogger(EventDetailsFactory.class);
	private static EventDetailsFactory instance;
	private final Map<EventType, Class<IEventDetails>> detailsMap;

	private EventDetailsFactory() throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {

		detailsMap = new HashMap<EventType, Class<IEventDetails>>();
		for (EventType eventType : EventType.values()) {
			if (EventRegistrar.INSTANCE.size() == 0
					|| EventRegistrar.INSTANCE.contains(eventType)) {
				String detailsClassName = eventType.getEventDetailsClass();
				if (!StringUtils.isEmpty(detailsClassName)) {
					logger.debug("loading class:" + detailsClassName
							+ ", for eventType: " + eventType);
					Class<?> detailsClass = Class.forName(detailsClassName
							.trim());

					detailsMap.put(eventType,
							(Class<IEventDetails>) detailsClass);
				} else {
					logger.warn("no detailsClass defined for : " + eventType);
				}
			} else {
				logger.warn("Can not load event type as its not registered explicitly "
						+ eventType);
				logger.warn("Can not load event type as its not registered explicitly "
						+ eventType);
			}

		}
	}

	public static EventDetailsFactory getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}

	private static synchronized void createInstance() {
		if (instance == null) {
			try {
				instance = new EventDetailsFactory();
			} catch (Exception e) {
				logger.error("couldn't load event details factory factory", e);
			}
		}
	}

	public boolean verify(EventType type, Class<?> detailsClazz) {
		logger.debug("Type " + type + " Class Name " + detailsClazz.getName());

		logger.debug("Registers class " + detailsMap.get(type) + " details class assignable from registered class " + detailsClazz.isAssignableFrom(detailsMap.get(type)));
		return detailsMap.get(type) != null
				&& detailsMap.get(type).isAssignableFrom(detailsClazz);

	}

	public IEventDetails getDetails(EventType type)
			throws Exception {

		IEventDetails details = null;
		if (!EventType.isValidEventType(type.name())) {
			throw new Exception();
		}

		if (detailsMap.get(type) == null) {
			throw new Exception();
		}

		Class<IEventDetails> clazz = detailsMap.get(type);

		try {
			details = clazz.newInstance();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception();
		}
		return details;
	}

}
