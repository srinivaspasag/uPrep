package com.vedantu.billing.payment.managers;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.ccavenue.security.AesCryptUtil;
import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.enums.OrderState;
import com.vedantu.billing.enums.TransactionStatus;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.models.Transaction;
import com.vedantu.billing.pojos.responses.OnPaymentReceivedRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.user.models.User;

public class CCAvenuePaymentManager extends AbstractPaymentManager {

    private static final ALogger          LOGGER                         = Logger.of(CCAvenuePaymentManager.class);
    private static final String           FIELD_MERCHANT_ID              = "merchant_id";
    private static final String           FIELD_ACCESS_CODE              = "access_code";

    public static final String            PAYMENT_CHANNEL                = "CCAvenue";

    private static final String           MERCHANT_PARAM_VALUE_SAPERATOR = ",";

    private static String                 CHARGING_URL                   = null;
    private static String                 MERCHANT_ID                    = null;
    private static String                 access_code                    = null;
    private static String                 enc_key                        = null;
    private static String                 redirect_url                   = null;
    // static {
    // CHARGING_URL = "https://secure.ccavenue.com/transaction/transaction.do";
    // MERCHANT_ID = "29501";
    // access_code = "88ILZZHB0YH2M2BZ";
    // enc_key = "C6D49E570A409A1A11171C5228E5D678";
    // redirect_url = "https://qa.vedantu.com/org/success";
    // }

    private static CCAvenuePaymentManager INSTANCE                       = null;

    public static final CCAvenuePaymentManager getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CCAvenuePaymentManager();
        }
        return INSTANCE;
    }

    private CCAvenuePaymentManager() {

        super();
        CHARGING_URL = Play.application().configuration().getString("ccavenue.charging.url");
        MERCHANT_ID = Play.application().configuration().getString("ccavenue.merchant_id");
        access_code = Play.application().configuration().getString("ccavenue.access_code");
        enc_key = Play.application().configuration().getString("ccavenue.enc_key");
        redirect_url = Play.application().configuration().getString("ccavenue.redirect_url");
    }

    @Override
    protected String getChargingRequestUrl(String transactionId, long orderId, User user,
            int amount, String currencyCode, DeviceType deviceType, String billingEmail,String billingPhone)
            throws VedantuException {

        Map<String, Object> httpParams = new HashMap<String, Object>();
        // required params
        httpParams.put("order_id", orderId);
        httpParams.put("currency", currencyCode);
        // as the amount is in paisa
        float amountValue = amount / 100;

        httpParams.put("amount", amountValue);

        String name = user._getFullName();
        httpParams.put("billing_name", name);
        httpParams.put("billing_email", billingEmail);
        httpParams.put("billing_address",
                Play.application().configuration().getString("ccavenue.billing.address.default"));
        httpParams.put("billing_city",
                Play.application().configuration().getString("ccavenue.billing.city.default"));
        httpParams.put("billing_state",
                Play.application().configuration().getString("ccavenue.billing.state.default"));
        httpParams.put("billing_zip",
                Play.application().configuration().getString("ccavenue.billing.zip.default"));
        httpParams.put("billing_country",
                Play.application().configuration().getString("ccavenue.billing.country.default"));
        httpParams.put("billing_tel",
                Play.application().configuration().getString("ccavenue.billing.tel.default"));

        httpParams.put("delivery_name", name);

        httpParams.put("integration_type", "iframe_normal");
        httpParams.put("language", "EN");
        httpParams.put(FIELD_MERCHANT_ID, MERCHANT_ID);

        httpParams.put("redirect_url", redirect_url);
        httpParams.put("cancel_url", redirect_url);

        // optional params
        httpParams.put("merchant_param1", "transactionId" + MERCHANT_PARAM_VALUE_SAPERATOR
                + transactionId + "#userId" + MERCHANT_PARAM_VALUE_SAPERATOR + user._getStringId()
                + "#deviceType" + MERCHANT_PARAM_VALUE_SAPERATOR + deviceType);

        return getPaymentRedirectUrl(httpParams);
    }

    @Override
    public OnPaymentReceivedRes onPaymentReceived(Map<String, Object> httpResParams)
            throws VedantuException {

        String encResp = (String) httpResParams.get("encResp");
        if (StringUtils.isEmpty(encResp)) {
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, "invalid response");
        }

        AesCryptUtil aesUtil = new AesCryptUtil(enc_key);

        String responseParams = aesUtil.decrypt(encResp);

        Map<String, Object> resParamMap = toParamsMap(responseParams, "&", "=");
        LOGGER.debug("response params : " + responseParams);
        String orderId = (String) resParamMap.get("order_id");

        String paymentChannelTransactionId = (String) resParamMap.get("tracking_id");

        String order_status = (String) resParamMap.get("order_status");

        TransactionStatus transactionStatus = null;
        OrderState orderState = null;

        if (order_status.equalsIgnoreCase("Success")) {
            orderState = OrderState.CONFIRMED;
            transactionStatus = TransactionStatus.SUCCESS;
        } else if (order_status.equalsIgnoreCase("Aborted")) {
            orderState = OrderState.CANCELLED;
            transactionStatus = TransactionStatus.CANCELLED;
        } else {
            orderState = OrderState.CANCELLED;
            transactionStatus = TransactionStatus.FAILED;
        }

        String paymentMethod = (String) resParamMap.get("payment_mode");

        String paymentInstrument = (String) resParamMap.get("card_name");

        String amount = (String) resParamMap.get("amount");

        int amountPaid = (int) (Float.parseFloat(amount) * 100);

        if (transactionStatus != TransactionStatus.SUCCESS) {
            amountPaid = 0;
        }

        Order order = OrderDAO.INSTANCE.getOrderById(Long.parseLong(orderId.trim()));
        order.orderState = orderState;

        Map<String, Object> merchantParamMap = toParamsMap(
                (String) resParamMap.get("merchant_param1"), "#", MERCHANT_PARAM_VALUE_SAPERATOR);

        LOGGER.debug("merchantParamMap : " + merchantParamMap);

        String transactionId = (String) merchantParamMap.get("transactionId");

        Transaction transaction = updateTransactionStatus(order.orderId, transactionId,
                paymentChannelTransactionId, paymentInstrument, paymentMethod, transactionStatus,
                String.valueOf(System.currentTimeMillis()), resParamMap, amountPaid);

        return updateOrderAndGetPaymentReceivedRes(order, transaction);
    }

    @Override
    public String getCallbackUrl() {

        return null;
    }

    private String getPaymentRedirectUrl(Map<String, Object> httpParams) {

        StringBuilder sb = new StringBuilder();
        sb.append(CHARGING_URL);
        sb.append("?command=initiateTransaction");
        sb.append("&");
        sb.append(FIELD_MERCHANT_ID);
        sb.append("=");
        sb.append(MERCHANT_ID);

        StringBuilder encReqBuilder = new StringBuilder();
        for (Entry<String, Object> entry : httpParams.entrySet()) {
            encReqBuilder.append(entry.getKey());
            encReqBuilder.append("=");
            encReqBuilder.append(entry.getValue());
            encReqBuilder.append("&");
        }
        sb.append("&");
        sb.append("encRequest");
        sb.append("=");
        AesCryptUtil aesUtil = new AesCryptUtil(enc_key);
        String encRequest = aesUtil.encrypt(encReqBuilder.toString());
        sb.append(encRequest);
        sb.append("&");
        sb.append(FIELD_ACCESS_CODE);
        sb.append("=");
        sb.append(access_code);

        String url = sb.toString();
        LOGGER.debug("payment channel [" + PAYMENT_CHANNEL + "] payment url :  " + url);
        return url;
    }

    private Map<String, Object>
            toParamsMap(String encResponse, String seprator1, String separator2) {

        Map<String, Object> responseParams = new HashMap<String, Object>();
        for (String keyValue : StringUtils.split(encResponse, seprator1)) {
            if (StringUtils.isEmpty(keyValue)) {
                continue;
            }
            String[] keyValueArray = keyValue.split(separator2);
            if (keyValueArray.length != 2) {
                continue;
            }
            responseParams.put(keyValueArray[0], keyValueArray[1]);
        }

        return responseParams;
    }

//    public static void main(String[] args) throws VedantuException, MalformedURLException {
//
//        CCAvenuePaymentManager manager = new CCAvenuePaymentManager();
//        String url = manager.getChargingRequestUrl("5301fd4744ae90d0284a9c33", 41, new User(),
//                100 / 100, "INR", DeviceType.WEB, null);
//        System.out.println("url : " + url);
//
//        System.out
//                .println(new AesCryptUtil(enc_key)
//                        .decrypt("85107a65707f8f25c7a95d5baca588968c298162c079a32a374a20510868ac103f906608d7cbcb6893d03329217772f01af93774a847d0bc11ed44a655aa1a8953b5751799661d4881bf8426063d203e98b6b0ae594dae9fc1d4b9b4c169a2f19ed0caa976c6e59338cb68a1d7a7e3b3d57364266472caf6eaf99cdad25e4c8e01be73e0003802f5609248e2763ac09705ae0471ddf85eb2bd22cce9aafbf151b04b9075907183b074a32bf105aca0c5eb58b332945c2177a021785e4c0d7ba6d95650c70f31697544f9e128646ed75ef2b623a8188f1180bb6edf454b693e9a0d22bed2e279118b09f707ce228094e1299ef24325900a14b761c8265a25d891ae900f53e6f299c3f255e8521923c941d649ce12af35f56014dcac6c0c46c3571ea3bcc83ffae0a701809c67e237c571e88575220bc6daabd63700dedb5f74dd1e99c62dad566202a67fc7c34ce24b3f8419578cc404d05fe3dd9ea044fafd2618ce2a7c35e3fddc83d6c2e8e1dc8bbc91c7b7fa7184f30816a04426dca80221585fd51de0fd459a1cd33e91d092f2f574a0d594cc3a4aba0b2224392ae161fe1d46f03d98be1bc3ed877d543b460d5f30bc2fb8d953d7773c362836f6f79c08bad539101a093caf8e0f09d025a8714fcc685f44d58c2f737239bb64755bb0e58d58e7be38a97c97fe6c70e31626db4d"));
//
//    }

}
