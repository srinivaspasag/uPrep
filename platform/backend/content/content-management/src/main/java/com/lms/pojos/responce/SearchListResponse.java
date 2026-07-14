package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;

import java.util.HashMap;
import java.util.Map;

public class SearchListResponse<T extends IListResponseObj> extends
        ListResponse<T> {

    public Map<String, Object> facet = new HashMap<String, Object>();
}
