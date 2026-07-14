package com.vedantu.content.models;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.PrePersist;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.enums.FileConversionState;
import com.vedantu.commons.utils.EncryptionUtils;
import com.vedantu.content.pojos.LinkInfo;
import com.vedantu.content.pojos.SrcType.LinkType;

public abstract class AbstractFileModel extends AbstractContentStatsModel {

    @Transient
    public final static String       DESCRIPTION = "description";

    public LinkType                  linkType;
    public String                    url;
    public String                    thumbnail;
    public LinkInfo                  linkInfo;
    /**
     * TODO add source like Vimeo, Youtube add original source Id;
     */

    public String                    originalFileName;
    public String                    extension;                  // final extension

    public boolean                   stored;
    public String                    uuid;

    public String                    description;

    public boolean                   converted;
    public String                    passphrase;

    public List<FileConversionState> states;

    public AbstractFileModel() {
        super();
        states = new ArrayList<FileConversionState>();
    }

    @Override
    @PrePersist
    protected void prePersist() {

        super.prePersist();
        if (passphrase == null || passphrase.length() == 0) {
            passphrase = EncryptionUtils.generatePassphrase();
        }
    }
    
    @Override
    public long getExportableSize() {

        return size.getThumbnail()+ size.getEncrypted();
    }


}
