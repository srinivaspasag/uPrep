package com.vedantu.organization.pojos.responses.organizations;

import java.util.List;
import java.util.Map;

import com.vedantu.commons.pojos.CostRate;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;
import com.vedantu.organization.pojos.PackageInfo;

public class OrgSectionInfo extends AbstractOrgStructureInfo {

    public AccessScope                    accessScope;
    public RevenueModel                   revenueModel;
    public String                         desc;
    public CostRate                       costRate;
    public long                           memberCount;
    public long                           size;
    public boolean                        sdOnly;
    public String                         orgId;
    public Map<String, CostRate>          startingRates;
    public Map<String, List<PackageInfo>> packagesMap;
    public List<String>                   descriptionPoints;
    public String                         thumbnail;

    public OrgSectionInfo(String id, String name, String code, VedantuRecordState recordState,
            AccessScope accessScope, RevenueModel revenueModel, String desc, CostRate costRate,
            long size, boolean sdOnly, String orgId, Map<String, CostRate> startingRates,
            Map<String, List<PackageInfo>> packagesMap, List<String> descriptionPoints,String thumbnail) {

        super(id, name, code, recordState);
        this.accessScope = accessScope;
        this.revenueModel = revenueModel;
        this.desc = desc;
        this.costRate = costRate;
        this.size = size;
        this.sdOnly = sdOnly;
        this.orgId = orgId;
        this.startingRates = startingRates;
        this.packagesMap = packagesMap;
        this.descriptionPoints = descriptionPoints;
        this.thumbnail= thumbnail;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{accessScope:").append(accessScope).append(", revenueModel:")
                .append(revenueModel).append(", desc:").append(desc).append(", costRate:")
                .append(costRate).append(", id:").append(id).append(", name:").append(name)
                .append(", code:").append(code).append(", recordState:").append(recordState)
                .append("}");
        return builder.toString();
    }

}
