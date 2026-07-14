package com.vedantu.organization.pojos.utils;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

import com.vedantu.organization.pojos.OrgStructureBasicInfo;

public class OrgStructureBasicInfoNameComparator implements Comparator<OrgStructureBasicInfo> {

    public static final OrgStructureBasicInfoNameComparator INSTANCE = new OrgStructureBasicInfoNameComparator();

    private OrgStructureBasicInfoNameComparator() {

    }

    @Override
    public int compare(OrgStructureBasicInfo o1, OrgStructureBasicInfo o2) {

        if (StringUtils.isEmpty(o1.name) || StringUtils.isEmpty(o2.name)) {
            return 0;
        }
        return o1.name.compareToIgnoreCase(o2.name);

    }
}
