package com.vedantu.billing.models;

import java.util.Map;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.billing.enums.TransactionStatus;
import com.vedantu.billing.enums.TransactionType;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "transactions", noClassnameStored = true)
public class Transaction extends VedantuBaseMongoModel {

    @Transient
    public static final String PAYMENT_CHANNEL_THIRD_PATY = "THIRD_PARTY";
    // user who started this transaction
    public String              userId;

    public DeviceType          deviceType;
    public TransactionType     type;
    public TransactionStatus   status;
    public String              paymentChannel;                            // which service (e.g
                                                                           // PayPal,
                                                                           // PayZippy,
                                                                           // ZackPay)
    public String              paymentMethod;
    public String              paymentInstrument;                         // CREDIT = Credit Card,
                                                                           // DEBIT =
                                                                           // Debit
                                                                           // Card, EMI =
                                                                           // Credit Card EMI,NET =
                                                                           // Net Banking
    public long                orderId;

    public boolean             consumed;                                  // this will be update
                                                                           // when the
                                                                           // action
                                                                           // for which transaction
                                                                           // was
                                                                           // performed
                                                                           // is completed

    // TODO: item need to be removed
    public SrcEntity           item;
    public String              item_sku;
    public String              callbackUrl;
    public int                 amount;                                    // amount in paisa
                                                                           // (currencyCode==INR)
    public int                 amountPaid;                                // amountPaid in paisa
                                                                           // (currencyCode==INR)
    public String              currencyCode;

    public String              transactionTime;                           // IST

    public String              paymentChannelTransactionId;

    public Map<String, Object> transactionInfo;

    public String              ipAddress;

    // below two fields will be used when a payment is not made through vedantu system and the user
    // is added to a paid program directly, it can be
    // made through some external seller (i.e Flipkart,Amazon, Sanpdeal etc)
    public String              pointOfSale;
    public String              sellerReferenceNo;

    public Transaction() {

        super();
    }

    public Transaction(String userId, long orderId, String paymentChannel, DeviceType deviceType,
            TransactionType type, int amount, String currencyCode) {

        super();
        this.userId = userId;
        this.orderId = orderId;
        this.paymentChannel = paymentChannel;
        this.deviceType = deviceType;
        this.type = type;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.status = TransactionStatus.PENDING;
    }
}
