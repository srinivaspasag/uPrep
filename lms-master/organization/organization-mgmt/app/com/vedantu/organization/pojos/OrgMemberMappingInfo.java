package com.vedantu.organization.pojos;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof OrgMemberMappingInfo)) {
            return false;
        }
        OrgMemberMappingInfo t = (OrgMemberMappingInfo) o;
        return StringUtils.equals(programId, t.programId)
                && StringUtils.equals(centerId, t.centerId)
                && StringUtils.equals(sectionId, t.sectionId);
    }

    @Override
    public int hashCode() {

        return (StringUtils.defaultIfEmpty(programId, StringUtils.EMPTY)
                + StringUtils.defaultIfEmpty(centerId, StringUtils.EMPTY) + StringUtils
                    .defaultIfEmpty(sectionId, StringUtils.EMPTY)).hashCode();
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
