package com.lms.pojo.responce;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.pojo.OrgMemberExtendedInfo;
import com.lms.pojo.OrgMemberMappingInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddOrgMemberMappingRes extends ModelBasicInfo {
    public boolean               done;

    // returnOrgProfileWithCourseInfo==true on request params
    public OrgMemberExtendedInfo info;

    public OrgMemberMappingInfo newlyAddedMapping;

    public AddOrgMemberMappingRes(String id, VedantuRecordState recordState, boolean done) {

        super(id,recordState);
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
