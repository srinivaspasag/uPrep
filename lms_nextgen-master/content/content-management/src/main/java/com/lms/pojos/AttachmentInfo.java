package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachmentInfo {
    public SrcEntity entity;
    public ModelBasicInfo info;
}
