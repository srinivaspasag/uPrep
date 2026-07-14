package com.lms.pojos.search.details;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.interfaces.ILibraryContent;
import com.lms.models.ContentSearchDetails;
import com.lms.models.tests.Assignment;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;


@Getter
@Setter
public class AssignmentSearchIndexDetails extends AbstractTestCommonSearchDetails implements
        ILibraryContent {

    public String cmdsId;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(ConstantsGlobal.CMDS_ID, cmdsId);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        cmdsId = JSONUtils.getString(json, ConstantsGlobal.CMDS_ID);
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        Assignment assignment = (Assignment) mongoModel;
        cmdsId = assignment.cmdsId;
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.ASSIGNMENT, id);
    }

    @Override
    public ContentSearchDetails __getContentSearchDetails() throws JSONException {

        ContentSearchDetails contentDetails = new ContentSearchDetails();
        JSONObject json = toJSON();
        contentDetails.fromJSON(json);

        contentDetails.name = name;

        contentDetails.type = EntityType.ASSIGNMENT;
        if (mode != null) {
            contentDetails.subType = mode.name();
        }
        JSONUtils.removeKeys(json, contentDetails.toJSON());
        if (json != null) {
            contentDetails.setInfo(json.toString());
        }
        return contentDetails;
    }

}
