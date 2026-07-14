package com.vedantu.billing.dao;

import java.util.List;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.vedantu.billing.models.PaymentItem;
import com.vedantu.billing.models.SaleDetails;
import com.vedantu.billing.pojos.SaleDetailsInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.mongo.VedantuBasicDAO;

public class SaleDetailsDAO extends VedantuBasicDAO<SaleDetails, ObjectId> {

    private static final ALogger       LOGGER   = Logger.of(SaleDetailsDAO.class);

    public static final SaleDetailsDAO INSTANCE = new SaleDetailsDAO();

    private SaleDetailsDAO() {
        super(SaleDetails.class);
    }

    public SaleDetails addSaleDetails(SaleDetailsInfo info, String orderId, String pointOfSale,
            String salesPersonId, String targetOrgMemberId, String orgId, String sectionId) {
        SaleDetails saleDetails = new SaleDetails(orgId, targetOrgMemberId, orderId, pointOfSale,
                salesPersonId, sectionId, info.origSaleAmount, info.discountPercentage,
                info.roundOff, info.totalSaleAmount, info.paymentItems);
        saleDetails.calculateTotalSaleAmount();
        save(saleDetails);
        return saleDetails;
    }

    public List<SaleDetails> getAllSaleDetails(String orgId) {

        Query<SaleDetails> querySaleDetails = getDS().find(entityClazz).order("orderId");
        querySaleDetails.filter("orgId", orgId).get();
        List<SaleDetails> salesDetails = querySaleDetails.asList();

        return salesDetails;
    }

    public SaleDetails updateSaleDetails(String saleDetailsId, List<PaymentItem> paymentItems,
            String targetOrgMemberId) throws VedantuException {
        SaleDetails saleDetails = getById(saleDetailsId);
        if (null == saleDetails) {
            LOGGER.error("Sale details not found corresponding to id: " + saleDetailsId);
            throw new VedantuException(VedantuErrorCode.SALE_DETAILS_NOT_FOUND);
        }

        if (!saleDetails.orgMemberId.equals(targetOrgMemberId)) {
            LOGGER.error("Sale details correspond to orgMemberId: " + saleDetails.orgMemberId
                    + " But expected orgMemberId is:" + targetOrgMemberId);
            throw new VedantuException(VedantuErrorCode.SALE_DETAILS_MISMATCH);
        }

        saleDetails.paymentItems = paymentItems;
        save(saleDetails);
        return saleDetails;
    }

}
