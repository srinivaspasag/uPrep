package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.enums.DisplayOrientation;
import com.lms.pojos.FileInfo;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "documents")
public class Documents extends AbstractFileModel {

    public boolean published;
    public DisplayOrientation orientation = DisplayOrientation.POTRAIT;
    @Indexed
    private String cmdsDocId;

    public Documents() {

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
        return info;
    }

}
