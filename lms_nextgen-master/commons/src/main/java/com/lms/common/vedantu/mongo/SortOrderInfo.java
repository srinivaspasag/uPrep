package com.lms.common.vedantu.mongo;

import com.amazonaws.services.ecs.model.SortOrder;
import com.lms.common.vedantu.constants.HardCodedConstants;

public class SortOrderInfo {

    public String field;
    public SortOrder order;

    /**
     * @param order
     * @param field Please use static field from MongoModel
     */
    public SortOrderInfo(SortOrder order, String field) {

        this.field = field.trim();
        this.order = order;
    }

    @Override
    public String toString() {

        return ((order == SortOrder.ASC) ? HardCodedConstants.emptyString : "-") + field;

    }


    @Override
    public int hashCode() {
        return (order.name() + field).toLowerCase().hashCode();
    }

}
