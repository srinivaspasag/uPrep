package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.SellableItemDetails;

public class ConsumableItem extends SellableItems {
    public long verifiedTime;
    public boolean verified;

    private SellableItemDetails itemSellableDetails;

    public SellableItemDetails __getItemSellableDetails() {

        return itemSellableDetails;
    }

    public void __setItemSellableDetails(SellableItemDetails itemSellableDetails) {

        this.itemSellableDetails = itemSellableDetails;
    }

}
