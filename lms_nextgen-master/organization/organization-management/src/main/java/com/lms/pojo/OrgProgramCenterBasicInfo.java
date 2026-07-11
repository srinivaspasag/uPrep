package com.lms.pojo;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.util.OrgStructureBasicInfoNameComparator;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Setter
@Getter
public class OrgProgramCenterBasicInfo extends OrgStructureBasicInfo {

    public List<OrgProgramSectionBasicInfo> sections = new ArrayList<OrgProgramSectionBasicInfo>();

    public OrgProgramCenterBasicInfo(String id, VedantuRecordState recordState,
                                     String name, String code, EntityType type) {

        super(id, recordState, name, code, type);
    }

    private Map<String, OrgProgramSectionBasicInfo> map = new HashMap<String, OrgProgramSectionBasicInfo>();

//    public OrgProgramSectionBasicInfo getOrAddProgramSection(
//            OrgStructureBasicInfo o) {
//
//        if (!map.containsKey(o.id)) {
//
//            map.put(o.id, (OrgProgramSectionBasicInfo) o);
//            if (!sections.contains(o)) {
//                sections.add((OrgProgramSectionBasicInfo) o);
//            }
//        }
//        Collections.sort(sections, OrgStructureBasicInfoNameComparator.INSTANCE);
//        return map.get(o.id);
//    }

    //to be tested
    public OrgProgramSectionBasicInfo _getOrAddProgramSection(
            OrgStructureBasicInfo o) {

        if (!map.containsKey(o.id)) {
            OrgProgramSectionBasicInfo c = new OrgProgramSectionBasicInfo(o.id, o.recordState,
                    o.name, o.code, o.type);
            map.put(o.id, c);
            if (!sections.contains(c)) {
                sections.add(c);
            }
        }
        Collections.sort(sections, OrgStructureBasicInfoNameComparator.INSTANCE);
        return map.get(o.id);
    }
    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{sections:");
        builder.append(sections);
        builder.append(", name:");
        builder.append(name);
        builder.append(", code:");
        builder.append(code);
        builder.append(", type:");
        builder.append(type);
        builder.append(", id:");
        builder.append(id);
        builder.append(", recordState:");
        builder.append(recordState);
        builder.append("}");
        return builder.toString();
    }

}