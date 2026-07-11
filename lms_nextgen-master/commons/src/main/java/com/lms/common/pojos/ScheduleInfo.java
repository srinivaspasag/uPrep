package com.lms.common.pojos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
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

	public ScheduleInfo(Date startTime, Date endTime, Date closeTime) {
		this(startTime, endTime);
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
