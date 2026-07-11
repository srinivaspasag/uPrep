package com.vedantu.web.datacollector;

import com.vedantu.content.pojos.LinkInfo;

public interface IDataCollector {

    public final String UTF_8 = "UTF-8";

    public ExternalContentInfo getData(String url);

    public String formURL(LinkInfo info);

    public boolean isEmbeddable(String url);
}
