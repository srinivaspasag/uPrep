package com.lms.models;

import com.lms.common.utils.VedantuStringUtils;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.pojos.ContentSize;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
@Setter
@Getter
public class AbstractContentModel extends VedantuBaseMongoModel {

    @Transient
    public final static String NAME        = "name";
    @Transient
    public final static String DESCRIPTION = "description";
    @Transient
    public final static String SIZE        = "size";

    @Indexed
    public String userId;
    public Scope scope;
    public String name;
    @Indexed
    private String             cName;

    public ContentSize size = new ContentSize();

    @Transient
    protected EntityType contentType;


    public AbstractContentModel() {



    }

    public String getName() {

        return name;

    }

    public void setName(String name) {

        this.cName = VedantuStringUtils.toCanonicalName(name);
        this.name = name;
    }

    public long getExportableSize() {

        return size.getTotalSize();
    }

    public EntityType getContentType() {

        return contentType;
    }

}
