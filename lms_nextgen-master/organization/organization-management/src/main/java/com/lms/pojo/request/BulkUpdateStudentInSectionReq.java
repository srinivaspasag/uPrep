package com.lms.pojo.request;

import com.lms.enums.OrgMappingBulkOperationType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.List;


@Getter
@Setter
public class BulkUpdateStudentInSectionReq extends AbstractOrgScopeReq {

    @NotBlank(message = "fromSectionId should not be null")
    public String fromSectionId;

    // toSectionId is required if operationType!=REMOVE
    public String toSectionId;

   @NotNull
    public OrgMappingBulkOperationType operationType;


    public List<String> targetUserIds;


}
