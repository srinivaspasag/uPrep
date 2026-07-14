package com.vedantu.organization.pojos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vedantu.organization.pojos.utils.OrgStructureBasicInfoNameComparator;

public class OrgMemberMappingExtendedInfo {

    public List<OrgProgramBasicInfo>         programs = new ArrayList<OrgProgramBasicInfo>();

    private Map<String, OrgProgramBasicInfo> map      = new HashMap<String, OrgProgramBasicInfo>();

    public OrgProgramBasicInfo _getOrAddProgram(OrgStructureBasicInfo o) {

        if (!map.containsKey(o.id)) {
            OrgProgramBasicInfo p = (OrgProgramBasicInfo) o;
            // as the toBasicInfo of program already gives OrgProgramBasicInfo object so we need not
            // recreate it here
            // new OrgProgramBasicInfo(o.id,
            // o.recordState, o.name, o.code, o.type);
            map.put(o.id, p);
            if (!programs.contains(p)) {
                programs.add(p);
            }
        }
        Collections.sort(programs, OrgStructureBasicInfoNameComparator.INSTANCE);
        return map.get(o.id);
    }

}
