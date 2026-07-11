package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class SyncModuleReq extends AbstractAppCheckReq {

    @NotBlank
    public String  userId;
    @NotBlank
    public String moduleId;
    public List<SrcEntity> entities;
}
