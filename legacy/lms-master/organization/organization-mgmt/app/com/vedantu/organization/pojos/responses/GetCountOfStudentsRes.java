package com.vedantu.organization.pojos.responses;

import java.util.Map;

import com.vedantu.organization.pojos.StudentsProgramDuration;

public class GetCountOfStudentsRes {
    public Map<String, StudentsProgramDuration> countResponse;
    public long onlyAppUsers;
    public long freeUsers;
    public long noProgramsUsers;
}
