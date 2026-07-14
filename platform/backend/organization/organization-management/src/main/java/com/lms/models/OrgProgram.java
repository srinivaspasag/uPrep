package com.lms.models;

import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.ProgramCategory;
import com.lms.pojo.OrgProgramCenterSections;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Setter
@Getter
@Document(value = "orgprograms" )
@CompoundIndexes({ @CompoundIndex(name = "orgId, departmentId, code", unique = true),
        @CompoundIndex(name = "orgId, courseIds") })
public class OrgProgram extends AbstractOrgStructureModel {

    public String                         departmentId;
    public String                         description;
    public long                           periodStart;
    public long                           periodEnd;
    public List<OrgProgramCenterSections> centersSections;
    public Set<String> courseIds;
    public boolean                        isOffline;
    public ProgramCategory category;
    public boolean                        sharedProgramAccess;

    @Override
    public EntityType _getEntityType() {

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
                if (!forSectionIds.isEmpty()
                        && CollectionUtils.containsAny(c.getSectionIds(), forSectionIds)) {
                    centerIds.add(c.getCenterId());
                    continue;
                } else if (CollectionUtils.isEmpty(forSectionIds)) {

                    centerIds.add(c.getCenterId());
                    if (forSectionIds != null && c.getSectionIds() != null) {
                        forSectionIds.addAll(c.getSectionIds());
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
            if (centerId.equals(c.getCenterId())) {
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

  /*  @Override
    public ModelBasicInfo toBasicInfo() {

        OrgDepartment department = orgDepartmentRepo.finz
                OrgDepartmentDAO.INSTANCE.getById(departmentId);
        return new OrgProgramBasicInfo(_getStringId(), recordState, getName(), code,
                _getEntityType(), departmentId, department.getName(), department.code, courseIds, isOffline);
    }*/

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
