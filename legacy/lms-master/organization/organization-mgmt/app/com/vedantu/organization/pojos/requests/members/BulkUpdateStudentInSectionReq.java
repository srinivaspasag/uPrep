package com.vedantu.organization.pojos.requests.members;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.enums.OrgMappingBulkOperationType;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class BulkUpdateStudentInSectionReq extends AbstractOrgScopeReq {

    @Required
    public String                      fromSectionId;

    // toSectionId is required if operationType!=REMOVE
    public String                      toSectionId;

    @Required
    public OrgMappingBulkOperationType operationType;

    // these values should be provide in case if one want to move/copy or remove specific set of
    // students, when operationType==REMOVE, targetUserIds is compulsory, and for now this operation
    // is only allowed to super_admin
    public List<String>                targetUserIds;

    @Override
    public String validate() {

        String val = super.validate();
        if (val != null) {
            return val;
        }

        if (operationType == OrgMappingBulkOperationType.REMOVE
                && CollectionUtils.isEmpty(targetUserIds)) {
            return "for operationType " + operationType + ", targetUserIds must be provided";
        }

        if (operationType != OrgMappingBulkOperationType.REMOVE && StringUtils.isEmpty(toSectionId)) {
            return "for operationType " + operationType + ", toSectionId must be provided";
        }

        return null;
    }

}
