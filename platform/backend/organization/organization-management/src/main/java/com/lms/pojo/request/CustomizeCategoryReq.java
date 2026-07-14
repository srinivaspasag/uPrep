package com.lms.pojo.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CustomizeCategoryReq {
    @NotBlank(message = "id should not be null")
    public String id;
    public String description;
    public String shortDescription;
    public int priority;
    public String banner;
    public String thumbnail;
    public String iconUUID;
    public String bannerUUID;
}
