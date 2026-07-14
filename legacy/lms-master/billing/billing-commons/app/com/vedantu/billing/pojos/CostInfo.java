package com.vedantu.billing.pojos;

import java.util.List;

import com.vedantu.billing.models.Tax;

public class CostInfo {

    public int       base; // e.g amount in paisa (if currencyCode==INR)
    public List<Tax> taxes;
    public int       total;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{base:").append(base).append(", taxes:").append(taxes).append(", total:")
                .append(total).append("}");
        return builder.toString();
    }

}
