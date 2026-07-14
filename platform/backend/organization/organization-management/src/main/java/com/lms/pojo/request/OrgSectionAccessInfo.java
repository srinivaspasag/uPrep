package com.lms.pojo.request;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.enums.RevenueModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Currency;
@Setter
@Getter
public class OrgSectionAccessInfo {

    public AccessScope accessScope;
    public RevenueModel revenueModel;
    public String              id;
    public CostRate costRate;

    public OrgSectionAccessInfo() {

        super();
    }

    public void validate() throws VedantuException {

        if (accessScope == null || revenueModel == null || id == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                    "missing parmeters accessScope:" + accessScope + ", revenueModel:"
                            + revenueModel + ", id:" + id);
        }

        if (revenueModel.equals(RevenueModel.PAID) && costRate == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_COST_RATE,
                    "invalid cost rate or currency code");
        }

        if (costRate != null) {
            try {
                Currency currency = costRate.getCurrencyCode().isEmpty()? null : Currency
                        .getInstance(costRate.getCurrencyCode());
                if (currency == null) {
                    throw new VedantuException(VedantuErrorCode.INVALID_CURRENCY_CODE,
                            "invalid currency");

                }
            } catch (Throwable e) {
                throw new VedantuException(VedantuErrorCode.INVALID_CURRENCY_CODE,
                        "invalid currency");
            }
        }

    }
}