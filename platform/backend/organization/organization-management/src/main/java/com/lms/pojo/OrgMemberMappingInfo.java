package com.lms.pojo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;
@Setter
@Getter
public class OrgMemberMappingInfo {

    public String      programId;
    public String      centerId;
    public String      sectionId;
    public long        endTime;

    public Set<String> courseIds = new HashSet<String>();

    // this will be populated only if the section/class was paid
    public String      orderId;
    public String      saleDetailsId;
    public long        timeJoined;

    public OrgMemberMappingInfo() {

        super();
    }

    public OrgMemberMappingInfo(String programId, String centerId, String sectionId,
                                Set<String> courseIds) {

        super();
        this.programId = programId;
        this.centerId = centerId;
        this.sectionId = sectionId;
        this.courseIds = courseIds;
    }

    public boolean addCourses(Set<String> courseIds) {

        if (CollectionUtils.isEmpty(courseIds)) {
            return false;
        }
        if (null == this.courseIds) {
            this.courseIds = new HashSet<String>();
        }
        return this.courseIds.addAll(courseIds);
    }

    public boolean removeCourses(Set<String> courseIds) {

        if (CollectionUtils.isEmpty(this.courseIds) || CollectionUtils.isEmpty(courseIds)) {
            return false;
        }
        return this.courseIds.removeAll(courseIds);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("OrgMemberMappingInfo [programId=");
        builder.append(programId);
        builder.append(", centerId=");
        builder.append(centerId);
        builder.append(", sectionId=");
        builder.append(sectionId);
        builder.append(", courseIds=");
        builder.append(courseIds);
        builder.append("]");
        return builder.toString();
    }

}