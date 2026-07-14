package com.vedantu.content.pojos.responses;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.content.models.ModuleScheduleInfo;

public class GetModuleScheduleRes {
    public Map<String, ModuleScheduleInfo> schedules = new HashMap<String, ModuleScheduleInfo>();
}
