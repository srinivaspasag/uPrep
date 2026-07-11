package com.vedantu.organization.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.organization.daos.OrgDepartmentDAO;
import com.vedantu.organization.enums.ProgramCategory;
import com.vedantu.organization.pojos.OrgProgramBasicInfo;
import com.vedantu.organization.pojos.OrgProgramCenterSections;

@Entity(value = "orgprograms", noClassnameStored = true)
@Indexes({ @Index(value = "orgId, departmentId, code", unique = true),
        @Index(value = "orgId, courseIds") })
public class OrgProgram extends AbstractOrgStructureModel {

    public String                         departmentId;
    public String                         description;
    public long                           periodStart;
    public long                           periodEnd;
    public List<OrgProgramCenterSections> centersSections;
    public Set<String>                    courseIds;
    public boolean                        isOffline;
    public ProgramCategory                category;
    public boolean                        sharedProgramAccess;

    @Override
    protected EntityType _getEntityType() {

        return EntityType.PROGRAM;
    }

    public OrgProgram() {

        super();
        centersSections = new ArrayList<OrgProgramCenterSections>();
        courseIds = new HashSet<String>();
    }

    public OrgProgram(String orgId, String code, String name, String departmentId,
            String description, long periodStart, long periodEnd) {

        super(orgId, code, name);
        this.departmentId = departmentId;
        this.description = description;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.centersSections = new ArrayList<OrgProgramCenterSections>();
    }

    public OrgProgram(String orgId, String code, String name, String departmentId,
            String description, long periodStart, long periodEnd, boolean isOffline, boolean sharedProgramAccess,
            ProgramCategory category) {

        this(orgId, code, name, departmentId, description, periodStart, periodEnd);
        this.isOffline = isOffline;
        this.sharedProgramAccess = sharedProgramAccess;
        this.category = category;
    }

    public List<String> _getCenterIds(Collection<String> forSectionIds) {

        return _getCenterIds(forSectionIds, false);
    }

    public List<String> _getCenterIds(Collection<String> forSectionIds, boolean allCenterIds) {

        List<String> centerIds = new ArrayList<String>();
        if (null != this.centersSections) {

            for (OrgProgramCenterSections c : this.centersSections) {
                if (null == c) {
                    continue;
                }

                if (allCenterIds) {
                    centerIds.add(c.centerId);
                    if (forSectionIds != null && c.sectionIds != null) {
                        forSectionIds.addAll(c.sectionIds);
                    }
                }
                if (CollectionUtils.isNotEmpty(forSectionIds)
                        && CollectionUtils.containsAny(c.sectionIds, forSectionIds)) {
                    centerIds.add(c.centerId);
                    continue;
                } else if (CollectionUtils.isEmpty(forSectionIds)) {

                    centerIds.add(c.centerId);
                    if (forSectionIds != null && c.sectionIds != null) {
                        forSectionIds.addAll(c.sectionIds);
                    }
                }
            }
        }
        return centerIds;
    }

    public List<ObjectId> _getCentersAsObjectIds() {

        return ObjectIdUtils.toObjectIds(_getCenterIds(null));
    }

    public OrgProgramCenterSections _getOrgProgramCenterSections(String centerId) {

        if (null == this.centersSections) {
            return null;
        }
        OrgProgramCenterSections centerSections = null;
        for (OrgProgramCenterSections c : this.centersSections) {
            if (null == c) {
                continue;
            }
            if (StringUtils.equals(centerId, c.centerId)) {
                centerSections = c;
                break;
            }
        }
        return centerSections;
    }

    public boolean addCourses(Collection<String> courseIds) {

        if (CollectionUtils.isEmpty(courseIds)) {
            return false;
        }
        return this.courseIds.addAll(courseIds);
    }

    public boolean removeCourses(Collection<String> courseIds) {

        if (CollectionUtils.isEmpty(courseIds)) {
            return false;
        }
        return this.courseIds.removeAll(courseIds);
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        OrgDepartment department = OrgDepartmentDAO.INSTANCE.getById(departmentId);
        return new OrgProgramBasicInfo(_getStringId(), recordState, getName(), code,
                _getEntityType(), departmentId, department.getName(), department.code, courseIds, isOffline);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("OrgProgram [departmentId=");
        builder.append(departmentId);
        builder.append(", description=");
        builder.append(description);
        builder.append(", periodStart=");
        builder.append(periodStart);
        builder.append(", periodEnd=");
        builder.append(periodEnd);
        builder.append(", centersSections=");
        builder.append(centersSections);
        builder.append(", courseIds=");
        builder.append(courseIds);
        builder.append("]");
        return builder.toString();
    }

}
