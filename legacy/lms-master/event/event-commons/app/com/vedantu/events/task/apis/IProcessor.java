package com.vedantu.events.task.apis;

import com.vedantu.events.task.enums.Status;

/**
 * Implementations of this class should be stateless
 * 
 * @author ujjawal
 * 
 */
public interface IProcessor {
	static final String EVENTS = "events_";

	public Status process(IConsumable consumable);
	
}
