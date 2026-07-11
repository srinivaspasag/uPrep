package com.vedantu.mongo;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.mongo.MongoManager.SortOrder;

public class SortOrderInfo {

    public String    field;
    public SortOrder order;

    /**
     * 
     * @param order
     * @param field
     *            Please use static field from MongoModel
     */
    public SortOrderInfo(SortOrder order, String field) {

        this.field = field.trim();
        this.order = order;
    }

    @Override
    public String toString() {

        return ((order == SortOrder.ASC) ? StringUtils.EMPTY : "-") + field;

    }

    
    @Override
    public int hashCode() {
        return (order.name()+field).toLowerCase().hashCode();
    }

}
