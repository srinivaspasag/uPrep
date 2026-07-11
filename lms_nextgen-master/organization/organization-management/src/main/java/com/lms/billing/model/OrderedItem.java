package com.lms.billing.model;

import com.lms.billing.enums.ItemCategory;
import com.lms.billing.pojo.CostInfo;
import com.lms.billing.pojo.OrderItemDetails;
import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class OrderedItem {

    public String name;
    public SrcEntity item;
    public SrcEntity seller;   // owner of the item
    public ItemCategory category;

    public CostRate rate;
    public CostInfo costInfo;

    public long orderDate;
    public String desc;
    public int count;
    public OrderItemDetails details;

    public OrderedItem() {

    }

    public OrderedItem(String name, SrcEntity item, SrcEntity seller, ItemCategory category,
                       CostRate rate, String desc, int count, OrderItemDetails details) {

        super();
        this.name = name;
        this.item = item;
        this.seller = seller;
        this.category = category;
        this.rate = rate;
        this.orderDate = System.currentTimeMillis();
        this.desc = desc;
        this.count = count;
        this.details = details;
    }

    public void calculateCost(List<Tax> taxes) {

        this.costInfo = new CostInfo();
        this.costInfo.base = this.rate.getValue() * count;
        if (taxes == null) {
            taxes = new ArrayList<Tax>();
        }
        int toatlTax = 0;
        for (Tax tax : taxes) {
            toatlTax += tax.calculateTax(this.costInfo.base);
        }
        this.costInfo.taxes = taxes;
        this.costInfo.total = this.costInfo.base + toatlTax;
    }

}
