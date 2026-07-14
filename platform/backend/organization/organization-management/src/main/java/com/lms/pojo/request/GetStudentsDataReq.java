package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetStudentsDataReq {
    Long startDate;
    Long endDate;
    String orgId;
}
