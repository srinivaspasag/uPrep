package com.lms.common.vedantu.mongo;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vedantuuniquecodes")
@CompoundIndexes(@CompoundIndex(name = "type,code", unique = true))
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