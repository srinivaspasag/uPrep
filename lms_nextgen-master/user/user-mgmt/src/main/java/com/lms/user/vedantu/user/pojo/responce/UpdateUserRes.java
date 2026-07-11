package com.lms.user.vedantu.user.pojo.responce;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserRes {
    public ModelBasicInfo info;
    public String username;

    public UpdateUserRes(ModelBasicInfo info) {
        this.info = info;
    }
}
