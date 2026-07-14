package com.lms.common;

import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.OrganizationEntity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;

public class ShareWithEntity extends OrganizationEntity {
    public ShareWithEntity() {

        super();
    }

    public ShareWithEntity(SrcEntity orgEntity) {

        super(orgEntity);
    }

    public ShareWithEntity(EntityType type, String id) {

        super(type, id);
    }

    public String validate() {

        if (type == null || type != EntityType.ORGANIZATION || type != EntityType.PROGRAM
                || type != EntityType.SECTION || ObjectIdUtils.hasInvalidId(id)) {
            return "invalid org entity";
        }
        if (!centers.isEmpty()) {
            for (SrcEntity center : centers) {
                if (center.type != EntityType.CENTER || ObjectIdUtils.hasInvalidId(center.id)) {
                    return "incorrect center provided";
                }
            }
        }

        return null;
    }
}
