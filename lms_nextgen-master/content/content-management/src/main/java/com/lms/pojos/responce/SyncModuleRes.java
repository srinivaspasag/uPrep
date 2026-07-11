package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SyncModuleRes {

    public String userId;
    public String moduleId;
    public List<SrcEntity> entities;
}
