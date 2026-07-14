package com.vedantu.mongo.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "vedantuuniquecodes", noClassnameStored = true)
@Indexes(@Index(value = "type,code", unique = true))
public class VedantuUniqueCode extends VedantuBaseMongoModel {

    public String type;
    public String code;

    public VedantuUniqueCode(String type, String code) {

        super();
        this.type = type;
        this.code = code;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{type:").append(type).append(", code:").append(code).append(", id:")
                .append(id).append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
