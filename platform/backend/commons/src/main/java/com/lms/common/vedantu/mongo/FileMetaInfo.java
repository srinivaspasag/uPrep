package com.lms.common.vedantu.mongo;



import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;


@Document(collection = "filemetainfo")
@Getter
@Setter
public class FileMetaInfo extends VedantuBaseMongoModel {

    public Map<String, String> tags;
    @Indexed
    public String              fileId;
    public String              name;
    public EntityType type;
    public long                size;

    public FileMetaInfo() {

        super();
        this.fileId = null;
        tags = new HashMap<String, String>();
        size = -1;

    }

    public FileMetaInfo(String fileId) {

        super();
        this.fileId = fileId;
        tags = new HashMap<String, String>();
        size = -1;

    }

    public void add(String key, String value) {

        tags.put(key, value);
    }

    public void add(Map<String, String> tags) {

        if (tags != null) {
            tags.putAll(tags);
        }
    }

    public String getFileId() {

        return fileId;
    }

    public void setFileId(String fileId) {

        this.fileId = fileId;
    }

    public Map<String, String> getTags() {

        return tags;
    }

    public void setTags(Map<String, String> tags) {

        this.tags = tags;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public long getSize() {

        return size;
    }

    public void setSize(long size) {

        this.size = size;
    }

}

