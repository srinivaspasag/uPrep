package com.lms.pojos.requests;

import com.lms.enums.AcceptanceState;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecordTeacherResponseReq {
    public String discussionId;
    public String teacherId;
    public AcceptanceState response;
}
