package com.vedantu.billing.pojos.requests;

import com.vedantu.billing.enums.ItemCategory;
import com.vedantu.billing.enums.OrderState;
import com.vedantu.commons.pojos.SrcEntity;

public class GetSellOrdersReq extends GetBuyOrdersReq {

    public ItemCategory itemCategory;
    public OrderState   orderState;
    public SrcEntity    item;

}
