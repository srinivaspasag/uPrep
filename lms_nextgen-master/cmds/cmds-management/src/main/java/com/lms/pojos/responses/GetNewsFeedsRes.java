package com.lms.pojos.responses;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;

public class GetNewsFeedsRes extends ListResponse<IListResponseObj> {

    public String firstId;
    public String lastId;

}
