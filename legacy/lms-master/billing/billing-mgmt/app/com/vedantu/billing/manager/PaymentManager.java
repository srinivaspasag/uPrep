package com.vedantu.billing.manager;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.billing.payment.managers.IPaymentManager;
import com.vedantu.billing.payment.managers.PaymentManagersFactory;
import com.vedantu.billing.pojos.responses.OnPaymentReceivedRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class PaymentManager {

    private final static ALogger LOGGER = Logger.of(PaymentManager.class);

    public static OnPaymentReceivedRes onPaymentReceived(String referer,
            Map<String, Object> reqParams) throws VedantuException {

        LOGGER.debug("onPaymentReceived referer:" + referer + ", reqParams: " + reqParams);
        if (StringUtils.isEmpty(referer)) {
            throw new VedantuException(VedantuErrorCode.UNKNOWN_PAYMENT_CHANNEL, "invalid referer "
                    + referer);
        }
	//Expect always from EBS..
        IPaymentManager paymentManager = PaymentManagersFactory.INSTANCE
                .getPaymentManager("EBS");
        //IPaymentManager paymentManager = PaymentManagersFactory.INSTANCE .getPaymentManagerByReferer(referer);
        OnPaymentReceivedRes res = paymentManager.onPaymentReceived(reqParams);
        return res;
    }
}
