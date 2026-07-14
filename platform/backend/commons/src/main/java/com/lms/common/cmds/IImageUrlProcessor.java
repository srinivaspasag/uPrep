package com.lms.common.cmds;

import java.io.IOException;

public interface IImageUrlProcessor {

        void convertUuidsToImageUrls(boolean permanentImageUrl, String questionSetId);

        void convertImageUrlToUuids(boolean saveImages, boolean permanentImageUrl, String questionSetId) throws IOException;

}
