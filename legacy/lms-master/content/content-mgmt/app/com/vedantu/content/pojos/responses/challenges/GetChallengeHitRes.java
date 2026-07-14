package com.vedantu.content.pojos.responses.challenges;

import java.io.IOException;

import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.image.ImageHTMLUtils;

public class GetChallengeHitRes implements IReverseImageMapperProcessor {

    public String hint;

    @Override
    public void addImageSrcUrl() {

        hint = ImageHTMLUtils.addImageSrcUrl(EntityType.QUESTION, hint);
    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

    }
}
