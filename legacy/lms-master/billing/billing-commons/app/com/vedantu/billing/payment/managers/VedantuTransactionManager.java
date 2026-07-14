package com.vedantu.billing.payment.managers;

import java.util.Map;

import com.vedantu.billing.pojos.responses.OnPaymentReceivedRes;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.user.models.User;

public class VedantuTransactionManager extends AbstractPaymentManager {

    public static final String               PAYMENT_CHANNEL = "Google Wallet";
    private static VedantuTransactionManager INSTANCE        = null;

    public static final VedantuTransactionManager getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new VedantuTransactionManager();
        }
        return INSTANCE;
    }

    private VedantuTransactionManager() {

        super();
    }

    @Override
    public OnPaymentReceivedRes onPaymentReceived(Map<String, Object> transactionInfo)
            throws VedantuException {

        return null;
    }

    @Override
    public String getCallbackUrl() {

        return null;
    }

    @Override
    protected String getChargingRequestUrl(String transactionId, long orderId, User user,
            int amount, String currencyCode, DeviceType deviceType, String billingEmail,String billingPhone)
            throws VedantuException {

        // TODO Auto-generated method stub
        return null;
    }

}
