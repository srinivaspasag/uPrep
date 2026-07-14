package com.lms.models;

import com.lms.common.vedantu.content.IIndexable;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.LatexType;
import com.lms.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Document(value = "questions")
@Getter
@Setter
public class Question extends AbstractContentStatsModel implements IIndexable {
    public String content;

    public QuestionType type;
    public String source;
    public Set<String> imgUuids;
    public LatexType latexType;

    public List<String> options;

    public Map<String, List<String>> matrix;

    @Indexed
    public String code;       // optional field for a question/it will be used
    // only in
    // case of off-line test result upload of an
    // organization
    @Indexed
    public String cmdsQId;
    public String answerId;   // added for multiple
    // occurences of question for
    // same cmdsquestion

    public long solutions;   // total no of solutions
    public boolean hasAns;
    public long attempts;
    // not null only if questio has been a part of some challenge in past
    public String challengeId;

    //Not null only for paragraph type questions
    public String paragraphId;

    public Question() {

        super();
        contentType = EntityType.QUESTION;
    }

    public Question(String content, String userId, QuestionType type, String source,
                    Set<String> imgUuids, LatexType latexType, List<String> options) {

        this();
        this.content = content;
        this.userId = userId;
        this.type = type;
        this.source = source;
        this.imgUuids = imgUuids;
        this.latexType = latexType;
        this.options = options;
        this.tags = new HashSet<String>();
        this.scope = Scope.PUBLIC;
    }


    public Set<String> getImgUuids() {

        return imgUuids;
    }

    public void setImgUuids(Set<String> imgUuids) {

        this.imgUuids = imgUuids;
    }

    public void addImgUuid(String imgUUID) {

        if (imgUuids == null) {
            imgUuids = new HashSet<String>();
        }
        imgUuids.add(imgUUID);
    }

    public void addImgUuid(Collection<String> imgUUIDs) {

        if (CollectionUtils.isEmpty(imgUUIDs)) {
            return;
        }
        if (imgUuids == null) {
            imgUuids = new HashSet<String>();
        }
        imgUuids.addAll(imgUUIDs);
    }

    public String getQrQid() {

        return cmdsQId;
    }

    public void setQrQid(String qrQid) {

        this.cmdsQId = qrQid;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{content:").append(content).append(", type:").append(type)
                .append(", source:").append(source).append(", imgUuids:").append(imgUuids)
                .append(", latexType:").append(latexType).append(", options:").append(options)
                .append(", matrix:").append(matrix).append(", code:").append(code)
                .append(", qrQid:").append(cmdsQId).append(", solutions:").append(solutions)
                .append(", hasAns:").append(hasAns).append(", challengeId:").append(challengeId)
                .append(", upVotes:").append(upVotes).append(", views:").append(views)
                .append(", followers:").append(followers).append(", comments:").append(comments)
                .append(", userId:").append(userId).append(", boardIds:").append(boardIds)
                .append(", targetIds:").append(targetIds).append(", difficulty:")
                .append(difficulty).append(", contentSrc:").append(contentSrc).append(", scope:")
                .append(scope).append(", tags:").append(tags).append(", id:").append(id)
                .append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }


}
