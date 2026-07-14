package com.vedantu.commons.events.apis;

import com.vedantu.commons.pojos.SrcEntity;

public interface IEventDetails extends JSONAware, IGossipable {

    public SrcEntity __getSrcEntity();

}