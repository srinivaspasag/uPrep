package com.vedantu.commons.pojos;

public class CostRate {

    public int    value;       // amount in paisa
    public String unit;        // e.g per user per month
    public String currencyCode;

    public CostRate() {

        super();
    }

    public CostRate(int value, String unit, String currencyCode) {

        super();
        this.value = value;
        this.unit = unit;
        this.currencyCode = currencyCode;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{value:").append(value).append(", unit:").append(unit)
                .append(", currencyCode:").append(currencyCode).append("}");
        return builder.toString();
    }

}
