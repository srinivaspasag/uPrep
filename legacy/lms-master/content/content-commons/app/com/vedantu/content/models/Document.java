package com.vedantu.content.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.commons.enums.DisplayOrientation;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.pojos.FileInfo;

@Entity(value = "documents", noClassnameStored = true)
public class Document extends AbstractFileModel implements IIndexable {

    @Indexed
    private String            cmdsDocId;
    public boolean            published;

    public DisplayOrientation orientation = DisplayOrientation.POTRAIT;

    public Document() {

        super();
        this.contentType = EntityType.DOCUMENT;
    }

    final public String getCMDSDocId() {

        return cmdsDocId;
    }

    final public void setCMDSDocId(String cmdsDocId) {

        this.cmdsDocId = cmdsDocId;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(" thumbnail : ").append(thumbnail);
        builder.append(" originalFileName : ").append(originalFileName);
        builder.append(" description : ").append(description);
        builder.append(" cmdsDocId : ").append(cmdsDocId);
        builder.append(" published : ").append(published);
        builder.append(" extension : ").append(extension);
        builder.append(" linkType : ").append(linkType);
        builder.append(" url : ").append(url);
        builder.append(" converted : ").append(converted);
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
