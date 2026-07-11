package com.lms.user.vedantu.user.events;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.event.api.IGossipable;
import com.lms.common.vedantu.event.api.JSONAware;

public interface IEventDetails extends JSONAware, IGossipable {

    public SrcEntity __getSrcEntity();
}
