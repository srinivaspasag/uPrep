package com.vedantu.content.pojos.responses;

import com.vedantu.commons.pojos.responses.ListResponse;

public class GetContentLinksRes extends ListResponse<GetContentLinkRes> {

    public long latestContent;
    public long serverTime;

    public GetContentLinksRes() {

        super();
        latestContent = System.currentTimeMillis();
        serverTime = System.currentTimeMillis();
    }

}
