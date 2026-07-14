package com.vedantu.commons.pojos;

import java.util.Date;

public class ScheduleInfo {

	public Date startTime;
	public Date endTime;
	public Date closeTime;

	public ScheduleInfo() {
		super();
	}

	public ScheduleInfo(Date startTime, Date endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public ScheduleInfo(Date startTime, Date endTime, Date closeTime){
	    this(startTime,endTime);
	    this.closeTime = closeTime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{startTime:");
		builder.append(startTime);
		builder.append(", endTime:");
		builder.append(endTime);
		builder.append("}");
		return builder.toString();
	}

}
