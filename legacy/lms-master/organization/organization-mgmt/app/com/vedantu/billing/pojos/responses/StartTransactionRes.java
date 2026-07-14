package com.vedantu.billing.pojos.responses;

public class StartTransactionRes extends AbstractTransactionRes {

    public String  paymentUrl;
    public boolean needEmail;
    public String  email;
    public String  orderTotal;
    public String  paymentChannel;
    public String  phone;
}
