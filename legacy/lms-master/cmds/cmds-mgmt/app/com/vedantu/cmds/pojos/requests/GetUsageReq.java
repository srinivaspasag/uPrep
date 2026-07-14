package com.vedantu.cmds.pojos.requests;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.pojos.requests.AbstractGetContentReq;

public class GetUsageReq extends AbstractGetContentReq implements Cloneable {

    public EntityType containerType;
    public int        start;
    public int        size;

}
