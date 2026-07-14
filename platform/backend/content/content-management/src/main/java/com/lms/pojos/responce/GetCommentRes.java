package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.CommentType;
import com.lms.interfaces.IReverseImageMapperProcessor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.IOException;
import java.util.Set;

@Setter
@Getter
public class GetCommentRes extends AbstractContentUserActionRes implements
        IListResponseObj, IReverseImageMapperProcessor {

    public String content;
    @Indexed
    public SrcEntity parent;
    public CommentType type;
    public boolean hasMedia;

    public SrcEntity base;
    public SrcEntity root;

    public GetCommentRes(String id, int upVotes, int views, int followers,
                         int comments, long timeCreated, long lastUpdated, boolean voted,
                         String content, SrcEntity parent, CommentType type,
                         boolean hasMedia, SrcEntity base, SrcEntity root, Scope scope,
                         Set<String> tags) {
        super(id, upVotes, views, followers, comments, timeCreated,
                lastUpdated, voted, scope, tags);
        this.content = content;
        this.parent = parent;
        this.type = type;
        this.hasMedia = hasMedia;
        this.base = base;
        this.root = root;
    }

    @Override
    public String toString() {
        return "GetCommentRes [content=" + content + ", parent=" + parent
                + ", type=" + type + ", hasMedia=" + hasMedia + ", base="
                + base + ", root=" + root + ", toString()=" + super.toString()
                + "]";
    }

    @Override
    public String _getEntityId() {
        return id;
    }

    @Override
    public void addImageSrcUrl() {
       // content = ImageHTMLUtils.addImageSrcUrl(EntityType.COMMENT, content);

    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException {

    }

}
