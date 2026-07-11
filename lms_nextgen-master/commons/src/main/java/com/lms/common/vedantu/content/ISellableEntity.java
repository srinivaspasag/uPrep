package com.lms.common.vedantu.content;

import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.commons.pojos.requests.SellableItemDetails;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;

public interface ISellableEntity {
    SrcEntity DEFAULT_SELLER = new SrcEntity(EntityType.ORGANIZATION, "VEDANTU");

    SrcEntity _getSeller();

    CostRate _getCostRate();

    String _getItemName();

    SellableItemDetails _getSellableItemDetails();
}
