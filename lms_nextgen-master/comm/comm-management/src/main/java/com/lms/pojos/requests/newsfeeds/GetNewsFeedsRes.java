package com.lms.pojos.requests.newsfeeds;


import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetNewsFeedsRes extends ListResponse<IListResponseObj> {
    public String firstId;
    public String lastId;
}
