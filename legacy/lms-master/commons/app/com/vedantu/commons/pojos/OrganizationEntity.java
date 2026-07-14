package com.vedantu.commons.pojos;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.utils.ObjectIdUtils;

@SuppressWarnings("serial")
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
		if (CollectionUtils.isNotEmpty(centers)) {
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
