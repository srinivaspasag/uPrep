package com.lms.common.vedantu.event.api;

import com.lms.common.vedantu.enums.EventType;

import java.util.HashSet;


public class EventRegistrar extends HashSet<EventType> {

	public final static EventRegistrar INSTANCE = new EventRegistrar();
	private static final long serialVersionUID = 1L;

}
