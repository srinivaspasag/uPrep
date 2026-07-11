package com.vedantu.organization.pojos.utils;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

import com.vedantu.organization.pojos.responses.organizations.AbstractOrgStructureInfo;

public class OrgStructureInfoNameComparator implements Comparator<AbstractOrgStructureInfo> {

    public static final OrgStructureInfoNameComparator INSTANCE = new OrgStructureInfoNameComparator();

    private OrgStructureInfoNameComparator() {

    }

    @Override
    public int compare(AbstractOrgStructureInfo o1, AbstractOrgStructureInfo o2) {

        if (StringUtils.isEmpty(o1.name) || StringUtils.isEmpty(o2.name)) {
            return 0;
        }
        return o1.name.compareToIgnoreCase(o2.name);

    }
}
