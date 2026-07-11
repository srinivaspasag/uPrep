package com.lms.billing.pojo;

import com.lms.billing.model.Tax;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
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
