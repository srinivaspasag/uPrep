package com.vedantu.events.errors;

public class DetailsNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6156353531771310179L;

	public DetailsNotFoundException() {
		super("No details found");
	}

}
