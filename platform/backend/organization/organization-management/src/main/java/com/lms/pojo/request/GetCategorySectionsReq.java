package com.lms.pojo.request;

import com.lms.common.vedantu.enums.AccessScope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetCategorySectionsReq extends AbstractOrgListReq {
  //  @NotBlank(message = "name should not be null")
    public String name;

    public Boolean openOnly;         // need to keep it till mobile version releases
    // which can
    // use updated code

    public String id;
    public AccessScope scope;
    public boolean excludeSubscribed;

    public GetCategorySectionsReq() {

        userId = "PUBLIC"; // this api can be used with both userId specify or not specified
        callingUserId = "PUBLIC"; // this api can be used with both userId specify or not specified

    }
}
