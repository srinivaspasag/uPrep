package com.lms.pojos.response.messages;

public class MarkConversationRes {
    public boolean marked;
    public String id;

    public MarkConversationRes(boolean marked, String id) {
        super();
        this.marked = marked;
        this.id = id;
    }
}
