package com.lms.pojos.responce;

import com.lms.common.pojos.ScheduleInfo;
import com.lms.pojos.requests.splModules.CMDSModuleInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class GetModuleInfoRes extends CMDSModuleSearchIndexDetails {
    public CMDSModuleInfo moduleInfo;
    public Map<String, ScheduleInfo> schedules = new HashMap<String, ScheduleInfo>();
}
