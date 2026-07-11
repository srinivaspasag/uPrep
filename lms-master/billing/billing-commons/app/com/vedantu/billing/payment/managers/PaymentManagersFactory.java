package com.vedantu.billing.payment.managers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class PaymentManagersFactory {

    private Map<String, IPaymentManager>       paymentManagersMap = new HashMap<String, IPaymentManager>();

    public static final PaymentManagersFactory INSTANCE           = new PaymentManagersFactory();

    private PaymentManagersFactory() {

        super();
        paymentManagersMap.put(PayZippyPaymentManager.PAYMENT_CHANNEL,
                PayZippyPaymentManager.getInstance());
        paymentManagersMap.put(CCAvenuePaymentManager.PAYMENT_CHANNEL,
                CCAvenuePaymentManager.getInstance());
        paymentManagersMap.put(VedantuTransactionManager.PAYMENT_CHANNEL,
                VedantuTransactionManager.getInstance());
        paymentManagersMap.put(EBSPaymentManager.PAYMENT_CHANNEL, EBSPaymentManager.getInstance());


        paymentManagersMap.put("payzipp.com", PayZippyPaymentManager.getInstance());
        paymentManagersMap.put("secure.ccavenue.com", CCAvenuePaymentManager.getInstance());
        paymentManagersMap.put("test.ccavenue.com", CCAvenuePaymentManager.getInstance());
        paymentManagersMap.put("support.ebs.in", EBSPaymentManager.getInstance());
        paymentManagersMap.put("secure.ebs.in", EBSPaymentManager.getInstance());
    }

    public IPaymentManager getPaymentManager(String paymentChannel) throws VedantuException {

        if (paymentManagersMap.get(paymentChannel) == null) {
            throw new VedantuException(VedantuErrorCode.UNKNOWN_PAYMENT_CHANNEL,
                    "no payment manager found for paymentChannel:" + paymentChannel);
        }
        return paymentManagersMap.get(paymentChannel);
    }

    public IPaymentManager getPaymentManagerByReferer(String referer) throws VedantuException {

        URL url = null;
        try {
            url = new URL(referer);
            url.getHost();
        } catch (MalformedURLException e) {
            throw new VedantuException(VedantuErrorCode.UNKNOWN_PAYMENT_CHANNEL,
                    "invalid refered :" + referer);
        }

        if (paymentManagersMap.get(url.getHost()) == null) {
            throw new VedantuException(VedantuErrorCode.UNKNOWN_PAYMENT_CHANNEL,
                    "no payment manager found for referer: " + referer);
        }
        return paymentManagersMap.get(url.getHost());
    }

}
