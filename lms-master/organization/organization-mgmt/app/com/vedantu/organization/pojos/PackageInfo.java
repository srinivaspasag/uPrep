package com.vedantu.organization.pojos;

import com.google.code.morphia.annotations.Embedded;
import com.vedantu.commons.pojos.CostRate;

@Embedded
public class PackageInfo implements Comparable<PackageInfo> {

    public int      numDays;
    public String   displayDays; // e.g. 1 week for 7 days.
    public CostRate costRate;
    public String   savingsTxt;

    public PackageInfo() {
        super();
    }

    public PackageInfo(int numDays, CostRate costRate) {
        super();
        this.numDays = numDays;
        this.displayDays = getDisplayDays(numDays);
        this.costRate = costRate;
        this.savingsTxt = "";
    }

    private String getDisplayDays(int days) {
        String displayDays = days + " Days";
        if (days % 365 == 0) {
            int years = days / 365;
            if (years == 1) {
                displayDays = "1 Year";
            } else {
                displayDays = years + " Years";
            }
        } else if (days % 30 == 0) {
            int months = days / 30;
            if (months == 1) {
                displayDays = "1 Month";
            } else {
                displayDays = months + " Months";
            }
        } else if (days % 7 == 0) {
            int weeks = days / 7;
            if (weeks == 1) {
                displayDays = "1 Week";
            } else {
                displayDays = weeks + " Weeks";
            }
        }
        return displayDays;
    }

    public PackageInfo(int numDays, int value, String unit, String currencyCode) {
        this(numDays, new CostRate(value, unit, currencyCode));
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{days:").append(numDays).append(", costRate:")
                .append(costRate.toString()).append("}");
        return builder.toString();
    }

    @Override
    public int compareTo(PackageInfo other) {
        return other.numDays - this.numDays; // For Descending order
    }
}
