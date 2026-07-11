package com.vedantu.content.pojos.responses;

public class GetContentDownloadLinkRes {

    public boolean allowed;
    public String  url;
    public String  passphrase;
    public String  encLevel;

    public GetContentDownloadLinkRes(boolean allowed, String url) {

        super();
        this.allowed = allowed;
        this.url = url;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{allowed:").append(allowed).append(", url:").append(url).append("}");
        return builder.toString();
    }

}
