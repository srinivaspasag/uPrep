package com.lms.pojo.request;

import com.lms.pojo.PackageInfo;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class UpdatePackageInfoReq {
    @NotBlank(message = "orgId should not be null")
    public String orgId;

    @NotBlank(message = "sectionId should not be null")
    public String sectionId;


    public List<PackageInfo> packagesList;

}
