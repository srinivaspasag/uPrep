package com.lms.pojos;

import com.lms.billing.pojo.AddressTo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactDetails {
    public String email;
    public String phoneNumber;
    public AddressTo billingAddress;
    public AddressTo shipmentAddress;
}
