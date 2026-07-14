package com.lms.models;

import com.lms.common.utils.UniqueCodeUtils;
import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.commons.pojos.requests.SellableItemDetails;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.content.ISellableEntity;
import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.RevenueModel;
import com.lms.pojo.OrgProgramSectionBasicInfo;
import com.lms.pojo.PackageInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;


@Document(value = "orgsections")
@Setter
@Getter
public class OrgSection extends AbstractOrgStructureModel implements ISellableEntity {

    @Transient
    public transient static final String FIELD_ACCESS_SCOPE = "accessScope";
    @Transient
    public transient static final String FIELD_REVENUE_MODEL = "revenueModel";
    @Transient
    public transient static final String SIZE = "size";

    public String programId;
    public String centerId;
    public String accessCode;
    public AccessScope accessScope = AccessScope.CLOSED;
    public RevenueModel revenueModel = RevenueModel.FREE;
    public CostRate costRate;

    public long size;                                    // this
    // is
    // a
    // library
    // size
    // which
    // can
    // be
    // exported

    public boolean extSupported;
    public Map<String, CostRate> startingRates;                           // OrgId
    // specific
    public Map<String, List<PackageInfo>> packagesMap;
    public int maxDiscount;
    public List<String> descriptionPoints;
    public String thumbnail;

    @Override
    protected EntityType _getEntityType() {

        return EntityType.SECTION;
    }

    public OrgSection() {

        super();
    }

    public OrgSection(String orgId, String code, String name, String desc, String programId,
                      String centerId, List<String> descriptionPoints, String thumbnail) {

        super(orgId, code, name);
        this.programId = programId;
        this.centerId = centerId;
        this.desc = desc;
        // generate accessKey for this section
        this.accessCode = UniqueCodeUtils.generateUniqueCode(_getEntityType().name());
        this.descriptionPoints = descriptionPoints;
        this.thumbnail = thumbnail;

    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        OrgProgramSectionBasicInfo info = new OrgProgramSectionBasicInfo(_getStringId(),
                recordState, getName(), code, EntityType.SECTION);
        info.desc = desc;
        info.accessScope = accessScope;
        info.code = code;
        info.costRate = costRate;
        info.revenueModel = this.revenueModel;
        info.size = this.size;
        info.sdOnly = this.extSupported;
        info.orgId = this.orgId;
        info.startingRates = this.startingRates;
        info.packagesMap = this.packagesMap;
        info.maxDiscount = this.maxDiscount;
        return info;

    }

    @Override
    public SrcEntity _getSeller() {

        return new SrcEntity(EntityType.ORGANIZATION, orgId);
    }

    @Override
    public CostRate _getCostRate() {

        return costRate;
    }

   /* @Override
    public String _getItemName() {

        StringBuilder sb = new StringBuilder();

        try {
            OrgProgram program = OrgProgramDAO.INSTANCE.getProgramById(orgId, programId);
            sb.append(program.getName());
            sb.append(", ");
            OrgCenter center = OrgCenterDAO.INSTANCE.getCenterById(orgId, centerId);
            sb.append(center.getName());
            sb.append(", ");
            sb.append(getName());
        } catch (VedantuException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }*/

    @Override
    public SellableItemDetails _getSellableItemDetails() {

        return new SellableItemDetails(_getCostRate(), _getItemName(), _getSeller(), new SrcEntity(
                EntityType.SECTION, _getStringId()));
    }

    @Override
    public String _getItemName() {
        // TODO Auto-generated method stub
        return null;
    }


}
