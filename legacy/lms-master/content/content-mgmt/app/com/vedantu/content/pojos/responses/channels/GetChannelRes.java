package com.vedantu.content.pojos.responses.channels;

import com.vedantu.commons.pojos.responses.IListResponseObj;

public class GetChannelRes implements IListResponseObj {

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
