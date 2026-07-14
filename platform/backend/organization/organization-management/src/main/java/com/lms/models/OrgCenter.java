package com.lms.models;

import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "orgcenters")
@CompoundIndexes(@CompoundIndex(name = "orgId, code", unique = true))
@Setter
@Getter
public class OrgCenter extends AbstractOrgStructureModel {

	@Override
	public EntityType _getEntityType() {
		return EntityType.CENTER;
	}

	public OrgCenter() {
		super();
	}

	public OrgCenter(String orgId, String code, String name) {
		super(orgId, code, name);
	}

}
