package com.lms.models;

import com.lms.common.utils.VedantuStringUtils;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.RevenueModel;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.pojo.OrgStructureBasicInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

@Setter
@Getter
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
