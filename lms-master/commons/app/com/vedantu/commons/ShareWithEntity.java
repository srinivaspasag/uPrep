package com.vedantu.commons;

import org.apache.commons.collections.CollectionUtils;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.OrganizationEntity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.ObjectIdUtils;

@SuppressWarnings("serial")
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
        if (CollectionUtils.isNotEmpty(centers)) {
            for (SrcEntity center : centers) {
                if (center.type != EntityType.CENTER || ObjectIdUtils.hasInvalidId(center.id)) {
                    return "incorrect center provided";
                }
            }
        }

        return null;
    }

}
