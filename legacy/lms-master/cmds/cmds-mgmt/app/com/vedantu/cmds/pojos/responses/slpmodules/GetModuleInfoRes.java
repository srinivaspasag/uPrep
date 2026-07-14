package com.vedantu.cmds.pojos.responses.slpmodules;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.cmds.models.event.search.details.CMDSModuleSearchIndexDetails;
import com.vedantu.cmds.pojos.requests.slpmodules.CMDSModuleInfo;
import com.vedantu.commons.pojos.ScheduleInfo;


public class GetModuleInfoRes extends CMDSModuleSearchIndexDetails {
    public CMDSModuleInfo  moduleInfo;
    public Map<String, ScheduleInfo> schedules = new HashMap<String, ScheduleInfo>();
}
