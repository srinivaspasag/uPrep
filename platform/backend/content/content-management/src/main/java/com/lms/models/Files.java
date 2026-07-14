package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.content.IIndexable;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.pojos.FileInfo;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "files")
public class Files extends AbstractFileModel implements IIndexable {

    public boolean published;
    @Indexed
    private String cmdsFileId;

    public Files() {

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
        return info;
    }

}

