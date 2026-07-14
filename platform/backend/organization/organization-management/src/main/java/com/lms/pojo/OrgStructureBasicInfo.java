package com.lms.pojo;


import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.OrgCenter;
import com.lms.models.OrgProgram;
import com.lms.models.OrgSection;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrgStructureBasicInfo extends ModelBasicInfo {

    public String name;
    public String code;
    public EntityType type;

    public OrgStructureBasicInfo(String id, VedantuRecordState recordState,
                                 String name, String code, EntityType type) {
        super(id, recordState);
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public OrgStructureBasicInfo(OrgSection orgSection) {
        super(orgSection.getId().toString(), orgSection.getRecordState());
        this.name = name;
        this.code = code;
        this.type = type;
    }

    public OrgStructureBasicInfo(OrgCenter orgCenter) {
        super(orgCenter.getId().toString(), orgCenter.getRecordState());
        this.name = orgCenter.getName();
        this.code = orgCenter.getCode();
        this.type = orgCenter._getEntityType();


    }

    public OrgStructureBasicInfo(OrgProgram orgProgram) {
        super(orgProgram.getId().toString(), orgProgram.getRecordState());
        this.name = orgProgram.getName();
        this.code = orgProgram.getCode();
        this.type = orgProgram._getEntityType();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OrgStructureBasicInfo)) {
            return false;
        }
        OrgStructureBasicInfo t = (OrgStructureBasicInfo) o;
        return null != t && id != null && id.equals(t.id) && type == t.type;
    }

    @Override
    public int hashCode() {
        return (id + (null != type ? type.name() : ""))
                .hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{name:");
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
