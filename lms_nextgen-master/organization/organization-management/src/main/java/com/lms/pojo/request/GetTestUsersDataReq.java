package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetTestUsersDataReq {
    Long startDate;
    Long endDate;
    String orgId;
}
