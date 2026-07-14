package com.lms.interfaces;

import java.io.IOException;

public interface IReverseImageMapperProcessor {
    void addImageSrcUrl();

    void removeImageSrc(boolean moveImages) throws IOException;
}
