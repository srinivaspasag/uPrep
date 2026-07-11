package com.lms.common.vedantu.commons.pojos.requests;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.JSONAware;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;
@Getter
@Setter
public class SrcEntity implements JSONAware, Comparable<SrcEntity>, IListResponseObj {
    private static final long serialVersionUID = 1L;


    public EntityType type;
    @Field("id")
    public String id;

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
    public int compareTo(SrcEntity o) {

        return this.id.compareTo(o.id);
    }





    public static List<String> toIds(Collection<SrcEntity> entities) {

        List<String> ids = new ArrayList<String>();
        for (SrcEntity entity : entities) {
            ids.add(entity.id);
        }
        return ids;
    }

   /* public VedantuBaseMongoModel get() {

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
    }*/

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


    @Override
    public JSONObject toJSON() throws JSONException {
        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {

    }
}
