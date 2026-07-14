package com.lms.pojos.requests;

import com.lms.pojos.requests.splModules.AbstractModuleReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class CreateModuleReq extends AbstractModuleReq {

    @NotBlank(message = "name should not be null")
    public String name;
    @NotBlank(message = "folderId should not be null")
    public String folderId;

}
