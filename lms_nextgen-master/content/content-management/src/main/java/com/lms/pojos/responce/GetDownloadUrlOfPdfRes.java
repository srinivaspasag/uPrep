package com.lms.pojos.responce;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetDownloadUrlOfPdfRes {

    public String  url;

    public GetDownloadUrlOfPdfRes(String url) {

        super();
        this.url = url;
    }

}
