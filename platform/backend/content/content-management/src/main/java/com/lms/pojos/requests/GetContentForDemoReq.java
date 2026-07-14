package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetContentForDemoReq {
    public String programId;
    public String parentId;
    public String type;
}
