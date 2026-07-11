package com.vedantu.billing.dao;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.billing.enums.CouponStatus;
import com.vedantu.billing.models.CouponCode;
import com.vedantu.billing.pojos.CouponCodeInfo;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;

public class CouponCodeDAO extends VedantuBasicDAO<CouponCode, ObjectId> {

    private static final ALogger      LOGGER   = Logger.of(CouponCodeDAO.class);

    public static final CouponCodeDAO INSTANCE = new CouponCodeDAO();

    private CouponCodeDAO() {
        super(CouponCode.class);
    }

    public CouponCode getByCodeAndOrgId(String code, String orgId) {

        CouponCode couponCode = getDS().find(CouponCode.class)
                .filter("code", code).filter("orgId", orgId).get();

        return couponCode;
    }

    public ListResponse<CouponCodeInfo> getCouponCodes(String orgId,
            CouponStatus status, int start, int size, String orderBy,
            String sortOrder) throws VedantuException {

        LOGGER.debug("Fetching Coupon codes");
        DBObject couponsQuery = new BasicDBObject();
        DBObject order = MongoManager.getSortQuery(orderBy, sortOrder);
        couponsQuery.put("orgId", orgId);

        ListResponse<CouponCodeInfo> codesQueryInfo = new ListResponse<CouponCodeInfo>();
        VedantuDBResult<CouponCode> couponCodesQueryInfo = CouponCodeDAO.INSTANCE
                .getInfos(couponsQuery, null, start, size, order);
        codesQueryInfo.totalHits = couponCodesQueryInfo.totalHits;

        for (CouponCode couponCode : couponCodesQueryInfo.results) {
            CouponCodeInfo couponCodeInfo = (CouponCodeInfo) couponCode.toBasicInfo();
            codesQueryInfo.list.add(couponCodeInfo);
        }

        return codesQueryInfo;
    }

    public CouponCode getByCode(String code) {
        CouponCode couponCode = getDS().find(CouponCode.class)
                .filter("code", code).get();

        return couponCode;
    }
}
