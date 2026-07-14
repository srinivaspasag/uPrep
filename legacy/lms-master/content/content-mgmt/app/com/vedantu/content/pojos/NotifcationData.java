package com.vedantu.content.pojos;

public class NotifcationData {
    public String title;
    public String message;
    public String discussionId;

    public NotifcationData(String title, String message, String discussionId) {
        super();
        this.title = title;
        this.message = message;
        this.discussionId = discussionId;
    }

    @Override
    public String toString() {
        return "NotifcationData [title=" + title + ", message=" + message + ", discussionId="
                + discussionId + "]";
    }
}
