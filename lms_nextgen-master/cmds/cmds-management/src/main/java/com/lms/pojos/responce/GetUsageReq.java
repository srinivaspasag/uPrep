package com.lms.pojos.responce;


import com.lms.common.vedantu.enums.EntityType;
import com.lms.pojos.requests.AbstractGetContentReq;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetUsageReq extends AbstractGetContentReq implements Cloneable {

    public EntityType containerType;
    public int start;
    public int size;

}