package com.lms.pojo.responce;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UploadOrgPicRes {
    public boolean done;
    public String thumbnail;

    public UploadOrgPicRes(boolean done, String thumbnail) {
        super();
        this.done = done;
        this.thumbnail = thumbnail;
    }
}
