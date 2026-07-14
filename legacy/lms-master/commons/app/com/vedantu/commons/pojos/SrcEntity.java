package com.vedantu.commons.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

// commented by Shankar, as it was creating problem in deserializing SrcEntity with
// ObjectMapperUtils

public class SrcEntity implements JSONAware, Serializable, IListResponseObj, Comparable<SrcEntity> {

    private static final long serialVersionUID = 1L;



    public EntityType         type;
    public String             id;

    public SrcEntity() {

    }

    public SrcEntity(EntityType type, String id) {

        this.type = type;
        this.id = id;
    }

    public SrcEntity(SrcEntity toBeCopied) {

        this.type = toBeCopied.type;
        this.id = toBeCopied.id;
    }

    @Override
    public boolean equals(Object o) {

        if (null == o || !(o instanceof SrcEntity)) {
            return false;
        }
        SrcEntity e = (SrcEntity) o;
        return type != null && type == e.type && id != null && id.equals(e.id);
    }

    @Override
    public int hashCode() {

        // commented below by Shankhoneer
        // return type == null ? null : (type.name() + ":" + id).hashCode();
        // if the type is null hash code will be zero.Now in case the type is
        // null there will be no NPE when JVM tries to
        // compute hash
        return type == null ? 0 : (type.name() + ":" + id).hashCode();
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{type:").append(type).append(", id:").append(id).append("}");
        return builder.toString();
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.ID, id);
        if (type != null) {
            json.put(ConstantsGlobal.TYPE, type.name());
        }
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        id = JSONUtils.getString(json, ConstantsGlobal.ID);
        type = EntityType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.TYPE));
    }

    @Override
    public int compareTo(SrcEntity o) {

        return this.id.compareTo(o.id);
    }

    public String validate() {

        if (type == null || ObjectIdUtils.hasInvalidId(id)) {
            return "entity is invalid";
        }
        return null;
    }

    public DBObject toDBObject() {

        return new BasicDBObject(ObjectMapperUtils.convertValue(this, Map.class));
    }

    public static List<String> toIds(Collection<SrcEntity> entities) {

        List<String> ids = new ArrayList<String>();
        for (SrcEntity entity : entities) {
            ids.add(entity.id);
        }
        return ids;
    }

    public VedantuBaseMongoModel get() {

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> srcDAO = EntityTypeDAOFactory.INSTANCE
                .get(this.type);

        if (srcDAO == null) {
            return null;
        }
        VedantuBaseMongoModel baseModel = srcDAO.getById(this.id, VedantuRecordState.ACTIVE);
        if (baseModel == null) {
            // check for delete and return true
            return null;
        }
        return baseModel;
    }

    public static Map<EntityType, ArrayList<SrcEntity>> convertToMap(List<SrcEntity> contents) {

        Map<EntityType, ArrayList<SrcEntity>> mappedEntities = new HashMap<EntityType, ArrayList<SrcEntity>>();
        for (SrcEntity content : contents) {

            if (!mappedEntities.containsKey(content.type)) {
                ArrayList<SrcEntity> entities = new ArrayList<SrcEntity>();

                mappedEntities.put(content.type, entities);
            }

            mappedEntities.get(content.type).add(content);
        }
        return mappedEntities;
    }
}