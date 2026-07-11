package com.vedantu.cmds.pojos;

import com.vedantu.commons.pojos.SellableItemDetails;

public class ConsumableItem extends SellableItems {

    public long                 verifiedTime;
    public boolean              verified;

    private SellableItemDetails itemSellableDetails;

    public SellableItemDetails __getItemSellableDetails() {

        return itemSellableDetails;
    }

    public void __setItemSellableDetails(SellableItemDetails itemSellableDetails) {

        this.itemSellableDetails = itemSellableDetails;
    }

}
