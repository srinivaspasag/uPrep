package com.lms.common.vedantu.event.errors;

public class TypeNotMatchedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5674281650806598816L;

	public TypeNotMatchedException() {
		super("Invalid EventType ");
	}

}
