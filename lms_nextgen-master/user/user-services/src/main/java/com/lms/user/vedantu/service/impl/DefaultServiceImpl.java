package com.lms.user.vedantu.service.impl;

import com.lms.user.vedantu.request.Guest;
import com.lms.common.validation.Validation;
import com.lms.user.vedantu.service.DefaultSevice;
import org.springframework.stereotype.Service;

@Service
public class DefaultServiceImpl implements DefaultSevice {

    @Override
    public String sayHelloFormally(Guest guest) {
        StringBuilder builder = new StringBuilder("Hello ");
        if (Validation.isObjectEmpty(guest)) {
            guest = new Guest();
            guest.setFirstName("guest");
        }
        if (Validation.isStringNotEmpty(guest.getFirstName())) {
            builder.append(guest.getFirstName()).append(" ");
        }
        if (Validation.isStringNotEmpty(guest.getLastName())) {
            builder.append(guest.getLastName());
        }
        builder.append("!");
        return builder.toString();
    }
}
