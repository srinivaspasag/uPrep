package com.lms.events.errors;

public class ProcessorNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6156353531771310176L;

	public ProcessorNotFoundException() {
		super("No processor found");
	}

}
