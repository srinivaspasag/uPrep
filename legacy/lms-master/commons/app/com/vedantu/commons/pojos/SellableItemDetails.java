package com.vedantu.commons.pojos;

public class SellableItemDetails {

    public CostRate  costRate;
    public String    itemName;
    public SrcEntity seller;
    public SrcEntity item;

    public SellableItemDetails() {

        super();
    }

    public SellableItemDetails(CostRate costRate, String itemName, SrcEntity seller, SrcEntity item) {

        super();
        this.costRate = costRate;
        this.itemName = itemName;
        this.seller = seller;
        this.item = item;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{costRate:").append(costRate).append(", itemName:").append(itemName)
                .append(", seller:").append(seller).append(", item:").append(item).append("}");
        return builder.toString();
    }

}
