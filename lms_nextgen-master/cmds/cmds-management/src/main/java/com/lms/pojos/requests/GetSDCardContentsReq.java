package com.lms.pojos.requests;

import javax.validation.constraints.NotBlank;

public class GetSDCardContentsReq extends AbstractGetResourcesReq {
    @NotBlank
    public String id;
}
