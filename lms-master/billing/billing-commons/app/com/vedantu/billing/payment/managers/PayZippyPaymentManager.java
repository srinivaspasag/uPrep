package com.vedantu.billing.payment.managers;

import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.payzippy.sdk.ChargingRequest;
import com.payzippy.sdk.ChargingRequestBuilder;
import com.payzippy.sdk.ChargingResponse;
import com.payzippy.sdk.Constants;
import com.payzippy.sdk.utils.RequestUtil;
import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.dao.TransactionDAO;
import com.vedantu.billing.enums.TransactionStatus;
import com.vedantu.billing.models.Transaction;
import com.vedantu.billing.pojos.responses.OnPaymentReceivedRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.user.models.User;

public class PayZippyPaymentManager extends AbstractPaymentManager {

    private static final ALogger LOGGER          = Logger.of(PayZippyPaymentManager.class);
    private static final String  CHARGING_URL    = "https://www.payzippy.com/payment/api/charging/v1";
    public static final String   PAYMENT_CHANNEL = "PayZippy";

    private static final String  SECRET_KEY      = "test_flipkart";
    // Play.application().configuration().getString("payzippy.secretKey");

    private static final String  MERCHANT_ID     = "test_flipkart";
    // Play.application().configuration()
    // .getString("payzippy.merchantId");
    private static final String  MERCHANT_KEY_ID = "payment";                                         // Play.application().configuration().getString("payzippy.merchantKeyId");

    ChargingRequestBuilder       reqBuilder;

    private PayZippyPaymentManager() {

        super();
        reqBuilder = ChargingRequest.getBuilder();
    }

    public static PayZippyPaymentManager getInstance() {

        return new PayZippyPaymentManager();
    }

    /**
     *
     * @return redirect URL for payment processing
     * @throws Exception
     */

    @Override
    public String getChargingRequestUrl(String transactionId, long orderId, User user,
            int amount/* amount in paisa */, String currencyCode, DeviceType deviceType,
            String billingEmail,String billingPhone) throws VedantuException {

        addCommonReqParams();
        reqBuilder.setBuyerEmailId(user.email);

        reqBuilder.setMerchantTransactionId(transactionId);
        reqBuilder.setTransactionType("SALE");
        reqBuilder.setTransactionAmount(String.valueOf(amount));
        reqBuilder.setCurrency(currencyCode);
        reqBuilder.setUiMode(Constants.uiModeRequirements.get(0));

        addOptionalParams(user, deviceType == null ? DeviceType.WEB.name() : deviceType.name());

        ChargingRequest chargeReq = null;
        try {
            chargeReq = reqBuilder.build(SECRET_KEY);
        } catch (Exception e) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, e.getMessage());
        }

        return RequestUtil.getUrl(chargeReq.getRequestParams(), CHARGING_URL);
    }

    private void addCommonReqParams() {

        reqBuilder.setMerchantId(MERCHANT_ID);
        reqBuilder.setHashMethod(Constants.hashMethodRequirements.get(1));
        reqBuilder.setMerchantKeyId(MERCHANT_KEY_ID);

    }

    private void addOptionalParams(User user, String deviceType) {

        reqBuilder.putParams(Constants.BUYER_UNIQUE_ID, user._getStringId());
        reqBuilder.putParams(Constants.SOURCE, deviceType);
        reqBuilder.putParams(Constants.BILLING_NAME, user._getFullName());
        reqBuilder.putParams(Constants.IS_USER_LOGGED_IN, String.valueOf(true));
        reqBuilder.putParams(Constants.CALLBACK_URL, getCallbackUrl());

    }

    @Override
    public String getCallbackUrl() {

        return "http://localhost:19012/payzippy/receivePayment";
    }

    @Override
    public OnPaymentReceivedRes onPaymentReceived(Map<String, Object> responseParams)
            throws VedantuException {

        ChargingResponse response = new ChargingResponse(responseParams);

        String errorMsg = null;
        if (!response.isValidResponse(SECRET_KEY)) {
            errorMsg = response.getTransactionResponseMessage();
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, errorMsg);
        }
        Transaction transaction = null;
        try {
            transaction = updateTransactionStatus(0, response.getMerchantTransactionId(),
                    response.getPayzippyTransactionId(),
                    (String) responseParams.get("payment_instrument"), response.getPaymentMethod(),
                    TransactionStatus.valueOf(response.getTransactionStatus()),
                    response.getTransactionTime(), responseParams, 0);

            if (response.isFraud()) {
                errorMsg = response.getFraudDetails();
                LOGGER.error(errorMsg);
                throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, errorMsg);
            }

            if (!response.isSuccess()) {
                errorMsg = response.getTransactionResponseMessage();
                LOGGER.error(errorMsg);
                throw new VedantuException(VedantuErrorCode.TRANSACTION_FAILED, errorMsg);
            }
        } finally {
            if (transaction != null && response.isSuccess()) {
                TransactionDAO.INSTANCE.save(transaction);
                // TODO: complete this
                // InvoiceManager.updatePayment(transaction.orderId, transaction.paymentChannel,
                // transaction._getStringId(), response.getTransactionAmount());
            }

        }
        return updateOrderAndGetPaymentReceivedRes(
                OrderDAO.INSTANCE.getOrderById(transaction.orderId), transaction);
    }
}
