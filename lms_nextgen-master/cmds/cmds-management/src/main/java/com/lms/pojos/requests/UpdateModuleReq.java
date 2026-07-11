package com.lms.pojos.requests;

import com.lms.pojos.requests.splModules.AbstractModuleReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class UpdateModuleReq extends AbstractModuleReq {

    @NotBlank(message = "id should not be null")
    public String id;
    public String name;
    public String prerequsiteModuleId;
    public List<String> updateList = new ArrayList<String>();

}