package com.vedantu.content.pojos.tests;

import java.util.List;

import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.content.pojos.EntityAnalyticsBasicInfo;
import com.vedantu.organization.pojos.OrgProgramBasicInfo;

public class EntityAnalyticsScheduleInfo implements IListResponseObj {

	public ScheduleInfo schedule;
	public List<OrgProgramBasicInfo> programs;
	public EntityTopper topper;
	public EntityAnalyticsBasicInfo entity;

	public EntityAnalyticsScheduleInfo() {
	}

	public EntityAnalyticsScheduleInfo(ScheduleInfo schedule,
			List<OrgProgramBasicInfo> programs, EntityTopper topper,
			EntityAnalyticsBasicInfo entity) {
		super();
		this.schedule = schedule;
		this.programs = programs;
		this.topper = topper;
		this.entity = entity;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{schedule:");
		builder.append(schedule);
		builder.append(", programs:");
		builder.append(programs);
		builder.append(", topper:");
		builder.append(topper);
		builder.append(", entity:");
		builder.append(entity);
		builder.append("}");
		return builder.toString();
	}

}
