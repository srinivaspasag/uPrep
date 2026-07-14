package pojos;

import java.io.Serializable;

public class OrgTnCInfo implements Serializable {

    private String orgId;
    private String latestTnCVersion;
    private boolean needsTnCAcceptance;
    private String adminUserId;
    private String status;

    public OrgTnCInfo(String orgId, String latestTnCVersion, boolean needsTnCAcceptance,
            String adminUserId, String status) {

        super();
        this.orgId = orgId;
        this.latestTnCVersion = latestTnCVersion;
        this.needsTnCAcceptance = needsTnCAcceptance;
        this.adminUserId = adminUserId;
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

    public String getOrgId() {

        return orgId;
    }

    public String getLatestTnCVersion() {

        return latestTnCVersion;
    }

    public boolean getNeedsTnCAcceptance() {

        return needsTnCAcceptance;
    }

    public String getAdminUserId() {

        return adminUserId;
    }

}
