package com.vedantu.organization.pojos.responses.members;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.pojos.OrgMemberExtendedInfo;
import com.vedantu.organization.pojos.OrgMemberMappingInfo;

public class AddOrgMemberMappingRes extends ModelBasicInfo {

    public boolean               done;

    // returnOrgProfileWithCourseInfo==true on request params
    public OrgMemberExtendedInfo info;

    public OrgMemberMappingInfo  newlyAddedMapping;

    public AddOrgMemberMappingRes(String id, VedantuRecordState recordState, boolean done) {

        super(id, recordState);
        this.done = done;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{done:").append(done).append(", info:").append(info).append(", id:")
                .append(id).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
