package com.vedantu.content.search.details;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.models.Discussion;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class DiscussionSearchIndexDetails extends AbstractBoardSearchEntityTagDetails implements
        JSONAware, IReverseImageMapperProcessor {

    public String content;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.CONTENT, content);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        content = JSONUtils.getString(json, ConstantsGlobal.CONTENT);
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        Discussion diss = (Discussion) mongoModel;
        name = diss.name;
        content = diss.content;
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.DISCUSSION, id);
    }

    @Override
    public UniqueId _getUniqueId() {

        return new UniqueId(ConstantsGlobal.ID, id);
    }

    @Override
    public String toString() {

        return " [name=" + name + ", content=" + content + ", toString()=" + super.toString() + "]";
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public void addImageSrcUrl() {

        content = ImageHTMLUtils.addImageSrcUrl(EntityType.DISCUSSION, content);
    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

    }

    @Override
    public boolean _isIndexable() {

        return StringUtils.isNotEmpty(name);
    }

}
