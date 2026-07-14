package com.lms.common.vedantu.commons.pojos.requests;

import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.enums.EntityType;

import java.util.List;

public class OrganizationEntity extends SrcEntity {
    public List<SrcEntity> centers;

    public OrganizationEntity(){
        super();
    }
    public OrganizationEntity( SrcEntity entity ) {
        super( entity);
    }
    public OrganizationEntity(EntityType type, String id) {
        super( type, id);
    }

    public String validate() {
        if (type == null || ( type != EntityType.ORGANIZATION
                && type != EntityType.PROGRAM && type != EntityType.SECTION
        ) || ObjectIdUtils.hasInvalidId(id)) {
            return "invalid org entity";
        }
        if (!centers.isEmpty()) {
            for (SrcEntity center : centers) {
                if (center.type != EntityType.CENTER
                        || ObjectIdUtils.hasInvalidId(center.id)) {
                    return "incorrect center provided";
                }
            }
        }

        return null;
    }
}
