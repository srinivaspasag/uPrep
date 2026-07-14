package com.vedantu.billing.managers;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.billing.dao.CouponCodeDAO;
import com.vedantu.billing.enums.CouponType;
import com.vedantu.billing.models.CouponCode;
import com.vedantu.billing.pojos.CouponCodeInfo;
import com.vedantu.billing.pojos.requests.couponcodes.AbstractCouponCodeReq;
import com.vedantu.billing.pojos.requests.couponcodes.AddCouponCodeReq;
import com.vedantu.billing.pojos.requests.couponcodes.DeleteCouponCodeReq;
import com.vedantu.billing.pojos.requests.couponcodes.GetCouponCodeReq;
import com.vedantu.billing.pojos.requests.couponcodes.GetCouponCodesReq;
import com.vedantu.billing.pojos.requests.couponcodes.UpdateCouponCodeReq;
import com.vedantu.billing.pojos.responses.couponcodes.AddCouponCodeRes;
import com.vedantu.billing.pojos.responses.couponcodes.GetCouponCodeRes;
import com.vedantu.billing.pojos.responses.couponcodes.GetCouponCodesRes;
import com.vedantu.billing.pojos.responses.couponcodes.UpdateCouponCodeRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ActionTakenRes;
import com.vedantu.commons.pojos.responses.ListResponse;

public class CouponCodeManager {

    public static CouponCodeManager INSTANCE = new CouponCodeManager();

    private final static ALogger LOGGER = Logger.of(CouponCodeManager.class);

    public AddCouponCodeRes addCouponCode(AddCouponCodeReq request)
            throws VedantuException {
        AddCouponCodeRes response = null;

        if (CouponCodeDAO.INSTANCE.getByCodeAndOrgId(request.code,
                request.orgId) != null) {
            LOGGER.info("Coupon Code already exists");
            throw new VedantuException(VedantuErrorCode.COUPON_ALREADY_EXISTS,
                    "Coupon with code " + request.code + " already exists");
        }

        CouponCode coupon = new CouponCode();
        coupon.code = request.code;
        coupon.orgId = request.orgId;
        coupon.creatorId = request.creatorId;

        setCouponProperties(coupon, request);

        CouponCodeDAO.INSTANCE.save(coupon);
        response = new AddCouponCodeRes(coupon._getStringId());
        return response;
    }

    public UpdateCouponCodeRes updateCouponCode(UpdateCouponCodeReq request)
            throws VedantuException {
        UpdateCouponCodeRes response = null;
        CouponCode coupon = CouponCodeDAO.INSTANCE.getByCodeAndOrgId(request.code,
                request.orgId);
        if (coupon == null) {
            throw new VedantuException(VedantuErrorCode.COUPON_CODE_NOT_FOUND,
                    "Coupon with code " + request.code + " not found");
        }
        setCouponProperties(coupon, request);
        CouponCodeDAO.INSTANCE.save(coupon);
        response = new UpdateCouponCodeRes(coupon._getStringId());
        response.isSuccess = true;
        return response;
    }

    private void setCouponProperties(CouponCode coupon, AbstractCouponCodeReq request) {
        coupon.couponType = request.couponType;

        if (request.couponType.equals(CouponType.FLAT)) {
            coupon.discountValue = request.discountValue;
            coupon.discountPercentage = 0;
            coupon.maxDiscount = 0;
        } else if (request.couponType.equals(CouponType.PERCENTAGE)) {
            coupon.discountPercentage = request.discountPercentage;
            coupon.discountValue = 0;
            coupon.maxDiscount = request.maxDiscount;
        }
        coupon.currencyCode = request.currencyCode;

        coupon.minPurchaseValue = request.minPurchaseValue;

        coupon.expiryTime = request.expiryTime;
        coupon.maxUsageCount = request.maxUsageCount;
    }

    public GetCouponCodesRes getCouponCodes(GetCouponCodesReq request)
            throws VedantuException {
        GetCouponCodesRes response = new GetCouponCodesRes();
        ListResponse<CouponCodeInfo> couponCodes = CouponCodeDAO.INSTANCE
                .getCouponCodes(request.orgId, null, request.start,
                        request.size, request.orderBy, request.sortOrder);
        response.couponCodes = couponCodes;
        return response;
    }

    public GetCouponCodeRes getCouponCode(GetCouponCodeReq request) throws VedantuException {
        GetCouponCodeRes response = new GetCouponCodeRes();
        CouponCode couponCode = CouponCodeDAO.INSTANCE.getByCodeAndOrgId(
                request.code, request.orgId);
        if (couponCode == null) {
            throw new VedantuException(VedantuErrorCode.COUPON_CODE_NOT_FOUND,
                    "Coupon with code " + request.code + " not found");
        }
        response.info = (CouponCodeInfo) couponCode.toBasicInfo();
        return response;
    }

    public ActionTakenRes deleteCouponCode(DeleteCouponCodeReq request)
            throws VedantuException {
        CouponCode couponCode = CouponCodeDAO.INSTANCE.getByCodeAndOrgId(
                request.code, request.orgId);
        if (couponCode == null) {
            throw new VedantuException(VedantuErrorCode.COUPON_CODE_NOT_FOUND,
                    "Coupon with code " + request.code + " not found");
        }
        CouponCodeDAO.INSTANCE.delete(couponCode);
        ActionTakenRes response = new ActionTakenRes();
        response.done = true;
        return response;
    }

}

