package com.lms.billing.pojo;

import com.lms.common.vedantu.commons.pojos.requests.Location;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddressTo {

    public String name;
    public String contactNo;
    public String email;
    public String address;
    public String pinCode;
    public Location location;

    public AddressTo() {

        super();
    }

    public AddressTo(String name, String contactNo, String email, String address,
                     String pinCode, Location location) {

        super();
        this.name = name;
        this.contactNo = contactNo;
        this.email = email;
        this.address = address;
        this.pinCode = pinCode;
        this.location = location;
    }

    public AddressTo(String name, String contactNo, String email, String address) {

        this(name, contactNo, email, address, null, null);
    }

    @Override
    public String toString() {
        return "AddressTo [name=" + name + ", contactNo=" + contactNo
                + ", email=" + email + ", address=" + address + ", pinCode="
                + pinCode + ", location=" + location + "]";
    }



}