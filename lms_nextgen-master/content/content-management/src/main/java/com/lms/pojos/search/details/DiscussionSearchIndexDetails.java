package com.lms.pojos.search.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.JSONAware;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.models.Discussion;
import com.lms.pojos.UniqueId;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.IOException;

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

      //  content = ImageHTMLUtils.addImageSrcUrl(EntityType.DISCUSSION, content);
    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException {

    }

    @Override
    public boolean _isIndexable() {

        return !StringUtils.isEmpty(name);
    }

}
