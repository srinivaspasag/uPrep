package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UpdateMaxDiscountReq {
    @NotBlank(message = "sectionId should not be null")
    public String sectionId;


    public int maxDiscount=0;
}
