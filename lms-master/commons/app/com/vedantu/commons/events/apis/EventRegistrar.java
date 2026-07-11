package com.vedantu.commons.events.apis;

import java.util.HashSet;

import com.vedantu.commons.enums.EventType;

public class EventRegistrar extends HashSet<EventType>{

	private static final long serialVersionUID = 1L;
	public final static EventRegistrar INSTANCE = new EventRegistrar();
	
}
