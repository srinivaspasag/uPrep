package com.vedantu.organization.pojos.requests.organizations;

import java.util.Currency;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.CostRate;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;

public class OrgSectionAccessInfo {

    public AccessScope  accessScope;
    public RevenueModel revenueModel;
    public String              id;
    public CostRate            costRate;

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
                Currency currency = StringUtils.isEmpty(costRate.currencyCode) ? null : Currency
                        .getInstance(costRate.currencyCode);
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
