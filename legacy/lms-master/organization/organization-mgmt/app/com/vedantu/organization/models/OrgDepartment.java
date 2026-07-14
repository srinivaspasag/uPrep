package com.vedantu.organization.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.enums.EntityType;

@Entity(value = "orgdepartments", noClassnameStored = true)
@Indexes(@Index(value = "orgId, code", unique = true))
public class OrgDepartment extends AbstractOrgStructureModel {

	@Override
	protected EntityType _getEntityType() {
		return EntityType.DEPARTMENT;
	}

	public OrgDepartment() {

	}

	public OrgDepartment(String orgId, String code, String name) {
		super(orgId, code, name);
	}

}
