package com.lms.user.vedantu.user.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UploadProfilePicRes {

    public boolean done;
    public String thumbnail;

    public UploadProfilePicRes(boolean done, String thumbnail) {
        super();
        this.done = done;
        this.thumbnail = thumbnail;
    }

}
