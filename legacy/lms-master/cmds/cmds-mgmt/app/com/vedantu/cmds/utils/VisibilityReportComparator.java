package com.vedantu.cmds.utils;

import java.util.Comparator;

import com.vedantu.cmds.pojos.responses.library.VisibilityReport;

public class VisibilityReportComparator implements Comparator<VisibilityReport> {

    public static final VisibilityReportComparator INSTANCE = new VisibilityReportComparator();

    private VisibilityReportComparator() {

    }

    @Override
    public int compare(VisibilityReport o1, VisibilityReport o2) {

        int progCompareResult = o1.programInfo.name.compareToIgnoreCase(o2.programInfo.name);

        if (progCompareResult != 0) {
            return progCompareResult;
        }

        int centerCompareResult = o1.centerInfo.name.compareToIgnoreCase(o2.centerInfo.name);

        if (centerCompareResult != 0) {
            return centerCompareResult;
        }

        int sectionCompareResult = o1.sectionInfo.name.compareToIgnoreCase(o2.sectionInfo.name);

        if (sectionCompareResult != 0) {
            return sectionCompareResult;
        }

        return 0;
    }

}
