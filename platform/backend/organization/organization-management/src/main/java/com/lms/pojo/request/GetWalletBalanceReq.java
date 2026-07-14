package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetWalletBalanceReq {
    public String userId;
    public String orgId;
    public long    orderId;
    public int    lpCredits;
}
