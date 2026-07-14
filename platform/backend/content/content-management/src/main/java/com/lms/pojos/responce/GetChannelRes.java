package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;

public class GetChannelRes implements IListResponseObj
{
    public String id;
    public String name;
    public int    contentCount;

    public GetChannelRes(String id, String name, int contentCount) {

        this.id = id;
        this.name = name;
        this.contentCount = contentCount;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", name:").append(name).append(", contentCount:")
                .append(contentCount).append("}");
        return builder.toString();
    }
}
