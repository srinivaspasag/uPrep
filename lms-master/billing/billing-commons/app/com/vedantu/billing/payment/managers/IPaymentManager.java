package com.vedantu.billing.payment.managers;

import java.util.Map;

import com.vedantu.billing.pojos.responses.OnPaymentReceivedRes;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.DeviceType;

public interface IPaymentManager {

    public String getVedantuTransactionId(String userId, long orderId, String paymentChannel,
            DeviceType deviceType, int amount, String currencyCode);

    public OnPaymentReceivedRes onPaymentReceived(Map<String, Object> transactionInfo)
            throws VedantuException;

    public String getCallbackUrl();

    public String getChargingRequestUrl(String transactionId, String userId, String item_sku,
            String callbackUrl, String billingEmail, String billingPhone) throws VedantuException;
}
