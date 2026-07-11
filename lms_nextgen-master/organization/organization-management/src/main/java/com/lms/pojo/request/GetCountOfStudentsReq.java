package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetCountOfStudentsReq {
    public long startDate;
    public long endDate;
    public String orgId;
}
