package com.lms.pojos.responce;

import com.lms.common.utils.ImageHTMLUtils;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.interfaces.IReverseImageMapperProcessor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Setter
@Getter
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
