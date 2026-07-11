package com.lms.common.vedantu.event.api;

import com.lms.common.vedantu.enums.Status;

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
