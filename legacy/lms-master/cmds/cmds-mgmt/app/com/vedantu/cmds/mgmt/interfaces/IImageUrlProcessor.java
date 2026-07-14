package com.vedantu.cmds.mgmt.interfaces;
import java.io.IOException;

public interface IImageUrlProcessor {
    public void convertUuidsToImageUrls(boolean permanentImageUrl, String questionSetId);
    public void convertImageUrlToUuids(boolean saveImages, boolean permanentImageUrl, String questionSetId) throws IOException;
}