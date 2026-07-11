package com.vedantu.content.pojos.tests;

import java.util.List;

import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.organization.pojos.OrgProgramBasicInfo;

public class EntityScheduleInfo implements IListResponseObj {

	public ScheduleInfo schedule;
	public List<OrgProgramBasicInfo> programs;
}
