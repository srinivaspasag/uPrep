package com.lms.billing.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tax {

    public String desc;
    public float percentage;
    public String name;
    public String category;     // e.g VAT or Service Tax etc

    public int base;         // amount in paisa (if currencyCode==INR)
    public int calculatedTax;

    public Tax() {

    }

    public Tax(String name, String desc, float percentage, String category) {

        super();
        this.name = name;
        this.desc = desc;
        this.percentage = percentage;
        this.category = category;
    }

    public int calculateTax(int base) {

        this.base = base;
        this.calculatedTax = (int) (base * this.percentage) / 100;
        return this.calculatedTax;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{desc:").append(desc).append(", percentage:").append(percentage)
                .append(", name:").append(name).append(", category:").append(category)
                .append(", base:").append(base).append(", calculatedTax:").append(calculatedTax)
                .append("}");
        return builder.toString();
    }

}
