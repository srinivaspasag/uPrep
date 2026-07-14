package com.vedantu.organization.models;

import java.util.List;
import java.util.Map;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.ISellableEntity;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.pojos.SellableItemDetails;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.UniqueCodeUtils;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;
import com.vedantu.organization.pojos.OrgProgramSectionBasicInfo;
import com.vedantu.organization.pojos.PackageInfo;

@Entity(value = "orgsections", noClassnameStored = true)
@Indexes({ @Index(value = "orgId, programId, centerId, code", unique = true),
        @Index(value = "orgId, code") })
public class OrgSection extends AbstractOrgStructureModel implements ISellableEntity {

    @Transient
    public transient static final String  FIELD_ACCESS_SCOPE  = "accessScope";
    @Transient
    public transient static final String  FIELD_REVENUE_MODEL = "revenueModel";
    @Transient
    public transient static final String  SIZE                = "size";

    public String                         programId;
    public String                         centerId;
    public String                         accessCode;
    public AccessScope                    accessScope         = AccessScope.CLOSED;
    public RevenueModel                   revenueModel        = RevenueModel.FREE;
    public CostRate                       costRate;

    public long                           size;                                    // this
                                                                                    // is
                                                                                    // a
                                                                                    // library
                                                                                    // size
                                                                                    // which
                                                                                    // can
                                                                                    // be
                                                                                    // exported

    public boolean                        extSupported;
    public Map<String, CostRate>          startingRates;                           // OrgId
                                                                                    // specific
    @Embedded("packagesMap")
    public Map<String, List<PackageInfo>> packagesMap;
    public int                            maxDiscount;
    public List<String>                   descriptionPoints;
    public String                         thumbnail;

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

    @Override
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
    }

    @Override
    public SellableItemDetails _getSellableItemDetails() {

        return new SellableItemDetails(_getCostRate(), _getItemName(), _getSeller(), new SrcEntity(
                EntityType.SECTION, _getStringId()));
    }

}
