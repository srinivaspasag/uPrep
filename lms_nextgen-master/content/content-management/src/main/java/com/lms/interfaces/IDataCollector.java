package com.lms.interfaces;

import com.lms.pojos.LinkInfo;
import com.lms.web.ExternalContentInfo;

public interface IDataCollector {

    public final String UTF_8 = "UTF-8";

    public ExternalContentInfo getData(String url);

    public String formURL(LinkInfo info);

    public boolean isEmbeddable(String url);
}
