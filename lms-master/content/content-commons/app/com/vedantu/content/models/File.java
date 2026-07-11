package com.vedantu.content.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.pojos.FileInfo;

@Entity(value = "files", noClassnameStored = true)
public class File extends AbstractFileModel implements IIndexable {

    @Indexed
    private String cmdsFileId;
    public boolean published;

    public File() {

        super();
        contentType = EntityType.FILE;

    }

    final public String getCMDSFileId() {

        return cmdsFileId;
    }

    final public void setCMDSFileId(String cmdsFileId) {

        this.cmdsFileId = cmdsFileId;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(" thumbnail : ").append(thumbnail);
        builder.append(" originalFileName : ").append(originalFileName);
        builder.append(" description : ").append(description);
        builder.append(" cmdsDocId : ").append(cmdsFileId);
        builder.append(" published : ").append(published);
        builder.append(" extension : ").append(extension);
        builder.append(" linkType : ").append(linkType);
        builder.append(" url : ").append(url);

        builder.append(" stored : ").append(stored);
        builder.append(" uuid : ").append(uuid);

        return builder.toString();
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        // String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;
        FileInfo info = new FileInfo(_getStringId(), name, recordState);
        return (ModelBasicInfo) info;
    }

}
