package com.lms.pojo.responce;

import com.lms.pojo.StudentsProgramDuration;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class GetCountOfStudentsRes {
    public Map<String, StudentsProgramDuration> countResponse;
    public long onlyAppUsers;
    public long freeUsers;
    public long noProgramsUsers;
}
