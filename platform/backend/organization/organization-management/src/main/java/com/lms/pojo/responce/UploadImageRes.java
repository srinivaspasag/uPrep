package com.lms.pojo.responce;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadImageRes {

    public String imgHtml;
    public String uuid;
    public boolean uploaded;
    public String filePath;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{imgHtml:").append(imgHtml).append(", uuid:")
                .append(uuid).append(", uploaded:").append(uploaded)
                .append("}");
        return builder.toString();
    }

}
