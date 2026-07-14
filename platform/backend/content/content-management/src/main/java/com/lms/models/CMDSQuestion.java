package com.lms.models;

import com.lms.cmds.HintInfo;
import com.lms.cmds.SolutionInfo;
import com.lms.common.cmds.IImageUrlProcessor;
import com.lms.common.pojos.ILatexProcessor;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.content.IIndexable;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.Difficulty;
import com.lms.enums.QuestionStatus;
import com.lms.enums.QuestionType;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.pojos.QuestionFormat;
import com.lms.pojos.tests.Metadata;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(value = "cmdsquestions")
@CompoundIndexes(@CompoundIndex(name = "contentSrc.id, boardIds, type, recordState"))
public class CMDSQuestion extends AbstractBoardEntityTagModel implements ILatexProcessor,
        IImageUrlProcessor, IIndexable, IReverseImageMapperProcessor {

    public SolutionInfo solutionInfo;
    public QuestionFormat questionBody;
    public QuestionType type;
    public String questionSetName;
    public String source;
    public String questionSetId;
    @Indexed
    public String globalQid;
    public boolean published;

    public QuestionStatus status;
    public HintInfo hints;
    public String origRefNo;
    public long publishedOn;
    public SrcEntity publishedBy;
    public String paragraphId;
    public List<String> paraIds = new ArrayList<String>();
    public Set<String> sharedToOrgIds = new HashSet<String>();
    public Set<String> sharedCMDSQuesIds = new HashSet<String>();
    public String parentQId;

    // ///////

    // there should be an uploadType LMS/UGQ
    // boards tags here
    public CMDSQuestion() {

        scope = Scope.ORG;
        contentType = EntityType.CMDSQUESTION;
    }

    public CMDSQuestion(QuestionFormat q, QuestionType type, SolutionInfo solutionInfo,
                        String userId, String questionSetName, String assignedTo, String status,
                        Metadata metadata) {

        super();

        this.questionBody = q;
        this.type = type;
        this.solutionInfo = solutionInfo;
        this.userId = userId;
        this.questionSetName = questionSetName;
        this.status = QuestionStatus.valueOfKey(status);
        this.source = metadata != null ? metadata.origRefName : "";
        this.questionSetId = "";
        this.difficulty = metadata != null ? metadata.difficulty : Difficulty.UNKNOWN;
        this.boardIds = new HashSet<String>();
        if (metadata != null && CollectionUtils.isNotEmpty(metadata.brdIds)) {
            this.boardIds.addAll(metadata.brdIds);
        }
        if (metadata != null && CollectionUtils.isNotEmpty(metadata.targetIds)) {
            this.targetIds.addAll(metadata.targetIds);
        }
        this.hints = new HintInfo();
        scope = Scope.ORG;

    }

    public void addHint(String hint) {

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Question [solutionInfo:");
        builder.append(solutionInfo);
        builder.append(", questionBody:");
        builder.append(questionBody);
        builder.append(", type:");
        builder.append(type);
        builder.append(", userId:");
        builder.append(userId);
        builder.append(", questionSetName:");
        builder.append(questionSetName);
        builder.append(", assignedTo:");
        builder.append(", status:");
        builder.append(status);
        builder.append(", source:");
        builder.append(source);
        builder.append(", questionSetId:");
        builder.append(questionSetId);
        builder.append(", metadata:");
        // builder.append(metadata);
        builder.append("]");
        return builder.toString();
    }

    // //
    @Override
    public void addHook() {

        if (solutionInfo != null) {
            solutionInfo.addHook();
        }
        if (questionBody != null) {
            questionBody.addHook();
        }
        if (hints != null) {
            hints.addHook();
        }
    }

    // //
    @Override
    public void convertUuidsToImageUrls(boolean permanentImageUrl, String questionSetId) {

        if (solutionInfo != null) {
            solutionInfo.convertUuidsToImageUrls(permanentImageUrl, questionSetId);
        }
        if (questionBody != null) {
            questionBody.convertUuidsToImageUrls(permanentImageUrl, questionSetId);
        }
        if (hints != null) {
            hints.convertUuidsToImageUrls(permanentImageUrl, questionSetId);
        }
    }

    @Override
    public void convertImageUrlToUuids(boolean saveImages, boolean permanentImageUrl,
                                       String questionSetId) throws IOException {

        if (solutionInfo != null) {
            solutionInfo.convertImageUrlToUuids(saveImages, permanentImageUrl, questionSetId);
        }
        if (questionBody != null) {
            questionBody.convertImageUrlToUuids(saveImages, permanentImageUrl, questionSetId);
        }
        if (hints != null) {
            hints.convertImageUrlToUuids(saveImages, permanentImageUrl, questionSetId);
        }
    }


    @Override
    public void addImageSrcUrl() {

        if (solutionInfo != null) {
            solutionInfo.addImageSrcUrl();
        }
        if (questionBody != null) {
            questionBody.addImageSrcUrl();
        }
        if (hints != null) {
            hints.addImageSrcUrl();
        }

    }

    @Override
    public void removeImageSrc(boolean moveImages) throws IOException, EntityFileStorageException {

        if (solutionInfo != null) {
            solutionInfo.removeImageSrc(moveImages);
        }
        if (questionBody != null) {
            questionBody.removeImageSrc(moveImages);
        }
        if (hints != null) {
            hints.removeImageSrc(moveImages);
        }

    }

    public boolean _hasHints() {

        return this.hints != null && CollectionUtils.isNotEmpty(this.hints.hints);
    }

    public boolean _hasSolutions() {

        return this.solutionInfo != null && CollectionUtils.isNotEmpty(this.solutionInfo.solutions);
    }

    public boolean _hasOptions() {

        return this.solutionInfo != null && this.solutionInfo.optionBody != null
                && CollectionUtils.isNotEmpty(this.solutionInfo.optionBody.newOptions);
    }


    @Override
    public long getExportableSize() {
        if (size != null) {
            return size.getTotalSize();
        }
        return 0;
    }

}
