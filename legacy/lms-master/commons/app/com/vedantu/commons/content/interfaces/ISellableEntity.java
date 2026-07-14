package com.vedantu.commons.content.interfaces;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.pojos.SellableItemDetails;
import com.vedantu.commons.pojos.SrcEntity;

public interface ISellableEntity {

    public static final SrcEntity DEFAULT_SELLER = new SrcEntity(EntityType.ORGANIZATION, "VEDANTU");

    public SrcEntity _getSeller();

    public CostRate _getCostRate();

    public String _getItemName();

    public SellableItemDetails _getSellableItemDetails();
}
