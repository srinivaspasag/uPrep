package com.vedantu.organization.pojos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.pojos.utils.OrgStructureBasicInfoNameComparator;

public class OrgProgramBasicInfo extends OrgStructureBasicInfo {

    public String                          departmentId;
    public String                          departmentName;
    public String                          departmentCode;
    public List<OrgProgramCenterBasicInfo> centers = new ArrayList<OrgProgramCenterBasicInfo>();
    public Set<String>                     courseIds;
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
