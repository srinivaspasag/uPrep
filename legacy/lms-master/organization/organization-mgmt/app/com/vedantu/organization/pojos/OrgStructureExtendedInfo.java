package com.vedantu.organization.pojos;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.mongo.VedantuRecordState;

public class OrgStructureExtendedInfo extends ModelExtendedInfo {

    public String     code;
    public EntityType type;

    public OrgStructureExtendedInfo(String id, VedantuRecordState recordState, String name,
            String code, EntityType type) {

        super(id, recordState, name, 0, 0);
        this.code = code;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof OrgStructureExtendedInfo)) {
            return false;
        }
        OrgStructureExtendedInfo t = (OrgStructureExtendedInfo) o;
        return null != t && StringUtils.equals(id, t.id) && type == t.type;
    }

    @Override
    public int hashCode() {

        return (id + (null != type ? type.name() : StringUtils.EMPTY)).hashCode();
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
