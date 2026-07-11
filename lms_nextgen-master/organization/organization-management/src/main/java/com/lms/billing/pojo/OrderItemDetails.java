package com.lms.billing.pojo;

import com.lms.common.vedantu.commons.pojos.requests.Interval;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderItemDetails {
    public Interval period;
}
