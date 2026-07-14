package com.vedantu.organization.pojos.responses.organizations;

import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.ProgramCategory;

public class OrgProgramInfo extends AbstractOrgStructureInfo {
    public long            periodEnd;
    public boolean         isOffline;
    public boolean         sharedProgramAccess;
    public String          orgId;
    public ProgramCategory category;

    public OrgProgramInfo(String id, String name, String code, VedantuRecordState recordState,
            long periodEnd) {
        super(id, name, code, recordState);
        this.periodEnd = periodEnd;
    }

    public OrgProgramInfo(String id, String name, String code, VedantuRecordState recordState,
            long periodEnd, boolean isOffline, ProgramCategory category, boolean sharedProgramAccess) {
        this(id, name, code, recordState, periodEnd);
        this.isOffline = isOffline;
        this.category = category;
        this.sharedProgramAccess = sharedProgramAccess;
    }

}
