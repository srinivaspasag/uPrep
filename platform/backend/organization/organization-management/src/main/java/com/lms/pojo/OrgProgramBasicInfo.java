package com.lms.pojo;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.util.OrgStructureBasicInfoNameComparator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Setter
@Getter
public class OrgProgramBasicInfo extends OrgStructureBasicInfo {

    public String                          departmentId;
    public String                          departmentName;
    public String                          departmentCode;
    public List<OrgProgramCenterBasicInfo> centers = new ArrayList<OrgProgramCenterBasicInfo>();
    public Set<String> courseIds;
    public boolean                         isOffline;

    public OrgProgramBasicInfo(String id, VedantuRecordState recordState, String name, String code,
                               EntityType type, String departmentId, String departmentName, String departmentCode,
                               Set<String> courseIds, boolean isOffline) {

        super(id, recordState, name, code, type);
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.courseIds = courseIds;
        this.isOffline = isOffline;
    }

    private Map<String, OrgProgramCenterBasicInfo> map = new HashMap<String, OrgProgramCenterBasicInfo>();

    public OrgProgramBasicInfo(String id, VedantuRecordState recordState, String name, String code, EntityType type) {
        super(id, recordState, name, code, type);
    }

    public OrgProgramBasicInfo(OrgStructureBasicInfo o) {
        super(o.getId().toString(), o.getRecordState(), o.getName(), o.getCode(), o.getType());
    }


    public OrgProgramCenterBasicInfo _getOrAddProgramCenter(OrgStructureBasicInfo o) {

        if (!map.containsKey(o.id)) {
            OrgProgramCenterBasicInfo c = new OrgProgramCenterBasicInfo(o.id, o.recordState,
                    o.name, o.code, o.type);
            map.put(o.id, c);
            if (!centers.contains(c)) {
                centers.add(c);
            }
        }
        Collections.sort(centers, OrgStructureBasicInfoNameComparator.INSTANCE);
        return map.get(o.id);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{departmentId:").append(departmentId).append(", departmentName:")
                .append(departmentName).append(", departmentCode:").append(departmentCode)
                .append(", centers:").append(centers).append(", map:").append(map)
                .append(", name:").append(name).append(", code:").append(code).append(", type:")
                .append(type).append(", id:").append(id).append(", recordState:")
                .append(recordState).append("}");
        return builder.toString();
    }

}