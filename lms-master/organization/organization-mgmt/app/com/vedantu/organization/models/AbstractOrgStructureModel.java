package com.vedantu.organization.models;

import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;

public abstract class AbstractOrgStructureModel extends VedantuBaseMongoModel {

    @Transient
    public final static String CODE = "code";

    public String              orgId;
    public String              code;
    private String             name;
    public String              desc;
    private String             cName;

    public AbstractOrgStructureModel() {

        super();
    }

    public AbstractOrgStructureModel(String orgId, String code, String name) {

        super();
        this.orgId = orgId;
        this.code = code;
        this.setName(name);
    }

    public ModelBasicInfo toBasicInfo() {

        return new OrgStructureBasicInfo(_getStringId(), recordState, name, code, _getEntityType());
    }

    protected abstract EntityType _getEntityType();

    public String getName() {

        return name != null ? name.trim() : "";
    }

    public void setName(String name) {

        this.cName = VedantuStringUtils.toCanonicalName(name);
        this.name = name;
    }

    public String getcName() {

        return cName;
    }

    public void setcName(String cName) {

        this.cName = VedantuStringUtils.toCanonicalName(cName);
    }

}
