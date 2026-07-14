package com.lms.models;

import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
@Setter
@Getter
@Document(value = "orgdepartments")
@CompoundIndexes(@CompoundIndex(name = "orgId, code", unique = true))
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