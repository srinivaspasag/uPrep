package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetContentLinksRes extends ListResponse<GetContentLinkRes> {

    public long latestContent;
    public long serverTime;

    public GetContentLinksRes() {

        super();
        latestContent = System.currentTimeMillis();
        serverTime = System.currentTimeMillis();
    }

}
