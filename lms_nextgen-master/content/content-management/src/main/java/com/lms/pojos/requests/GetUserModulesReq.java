package com.lms.pojos.requests;


import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class GetUserModulesReq extends AbstractAppCheckReq
{

    @NotBlank
    public String  userId;
    @NotBlank
    public List<String> moduleIds;
}
