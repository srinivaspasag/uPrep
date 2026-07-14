package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetCategorySectionReq extends AbstractOrgScopeReq {
    @NotBlank(message = "sectionId should not be null")
    public String sectionId;


    public GetCategorySectionReq() {

        userId = "PUBLIC"; // this api can be used with both userId specify or not specified
        callingUserId = "PUBLIC"; // this api can be used with both userId specify or not specified

    }
}
