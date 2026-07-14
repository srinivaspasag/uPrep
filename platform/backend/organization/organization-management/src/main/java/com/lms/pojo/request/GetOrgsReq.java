package com.lms.pojo.request;

import com.lms.enums.OrganizationStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetOrgsReq {
    public OrganizationStatus status;
    public String query;
}
