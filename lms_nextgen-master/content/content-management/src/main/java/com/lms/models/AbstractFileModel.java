package com.lms.models;

import com.lms.common.utils.EncryptionUtils;
import com.lms.enums.FileConversionState;
import com.lms.enums.SrcType;
import com.lms.pojos.LinkInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public abstract class AbstractFileModel extends AbstractContentStatsModel {

    @Transient
    public final static String DESCRIPTION = "description";

    public SrcType.LinkType linkType;
    public String url;
    public String thumbnail;
    public LinkInfo linkInfo;
    /**
     * TODO add source like Vimeo, Youtube add original source Id;
     */

    public String originalFileName;
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
