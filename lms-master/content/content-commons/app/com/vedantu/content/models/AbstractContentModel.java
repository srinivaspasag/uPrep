package com.vedantu.content.models;

import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.content.pojos.ContentSize;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class AbstractContentModel extends VedantuBaseMongoModel {

    @Transient
    public final static String NAME        = "name";
    @Transient
    public final static String DESCRIPTION = "description";
    @Transient
    public final static String SIZE        = "size";

    @Indexed
    public String              userId;
    public Scope               scope;
    public String              name;
    @Indexed
    private String             cName;

    public ContentSize         size   = new ContentSize();

    @Transient
    protected EntityType       contentType;

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
