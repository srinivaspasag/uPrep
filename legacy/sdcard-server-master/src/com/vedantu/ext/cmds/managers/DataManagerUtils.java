package com.vedantu.ext.cmds.managers;

import com.vedantu.ext.cmds.db.datamanagers.OrgDataManager;
import com.vedantu.ext.cmds.db.models.Organization;

public class DataManagerUtils {

    public static Organization getOrganization() {

        return getOrganization(null);

    }

    public static Organization getOrganization(String orgId) {

        return OrgDataManager.INSTANCE.getOrganization(null, orgId);

    }
}
