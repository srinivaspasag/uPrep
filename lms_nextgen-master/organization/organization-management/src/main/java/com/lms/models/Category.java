package com.lms.models;

import com.lms.common.utils.VedantuStringUtils;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;


@Document(value = "categories")
@CompoundIndexes({@CompoundIndex(name = "orgId,name", unique = true),
        @CompoundIndex(name = "orgId,cName", unique = true)})
public class Category extends VedantuBaseMongoModel {

    public String name;
    public String orgId;
    public Set<String> sectionIds;
    public String cName;
    public String description;
    public String shortDescription;
    public String thumbnail;
    public String banner;
    public int priority;

    public Category() {

        super();
    }

    public Category(String orgId, String name, Set<String> sectionIds) {

        super();
        this.orgId = orgId;
        setName(name);
        this.sectionIds = sectionIds;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name.trim();
        this.cName = VedantuStringUtils.toCanonicalName(name.trim());
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:").append(name).append(", orgId:").append(orgId)
                .append(", sectionIds:").append(sectionIds).append(", cName:").append(cName)
                .append(", id:").append(id).append(", timeCreated:").append(timeCreated)
                .append(", lastUpdated:").append(lastUpdated).append(", recordState:")
                .append(recordState).append("}");
        return builder.toString();
    }

}