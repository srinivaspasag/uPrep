package com.vedantu.cmds.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Indexes;
import com.mongodb.BasicDBObject;
import com.vedantu.cmds.managers.CMDSQuestionManager;
import com.vedantu.cmds.mgmt.interfaces.ICMDSModel;
import com.vedantu.cmds.mgmt.interfaces.IImageUrlProcessor;
import com.vedantu.cmds.mgmt.interfaces.ILatexProcessor;
import com.vedantu.cmds.models.event.search.details.CMDSQuestionSearchIndexDetails;
import com.vedantu.cmds.pojos.content.question.CMDSQuestionInfo;
import com.vedantu.cmds.pojos.content.question.HintInfo;
import com.vedantu.cmds.pojos.content.question.QuestionFormat;
import com.vedantu.cmds.pojos.content.solution.metadata.SolutionInfo;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionStatus;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.pojos.tests.Metadata;
import com.vedantu.mongo.VedantuRecordState;

@Entity(value = "cmdsquestions", noClassnameStored = true)
@Indexes(@Index("contentSrc.id, boardIds, type, recordState"))
public class CMDSQuestion extends AbstractBoardEntityTagModel implements ILatexProcessor,
        IImageUrlProcessor, IIndexable, IReverseImageMapperProcessor, ICMDSModel {

    @Embedded
    public SolutionInfo   solutionInfo;
    public QuestionFormat questionBody;
    public QuestionType   type;
    public String         questionSetName;
    public String         source;
    public String         questionSetId;
    @Indexed
    public String         globalQid;
    public boolean        published;

    public QuestionStatus status;
    public HintInfo       hints;
    public String         origRefNo;
    public long           publishedOn;
    public SrcEntity      publishedBy;
    public String         paragraphId;
    public List<String>   paraIds = new ArrayList<String>();
    public Set<String>    sharedToOrgIds = new HashSet<String>();
    public Set<String>    sharedCMDSQuesIds = new HashSet<String>();
    public String         parentQId;

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
        this.source = metadata != null ? metadata.origRefName : new String();
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
    public ModelBasicInfo toBasicInfo() {

        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(ConstantsGlobal._ID, id);

        String orgId = (contentSrc != null) ? contentSrc.id : StringUtils.EMPTY;
        CMDSQuestionInfo info = new CMDSQuestionInfo(_getStringId(), null, orgId, timeCreated,
                lastUpdated, userId, 0, published, completed, true, globalQid, recordState,this.getExportableSize());
        info.status = status;
        info.detail = new CMDSQuestionSearchIndexDetails();
        // this.convertUuidsToImageUrls(recordState == VedantuRecordState.ACTIVE ? true : false,
        // questionSetId);
        if (recordState == VedantuRecordState.ACTIVE) {
            this.addImageSrcUrl();
        }
        info.detail.fromMongoModel(this);

        CMDSQuestionManager.INSTANCE.annotateExtraInfo(null, orgId, Arrays.asList(info.detail));

        return info;
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

        if (this.hints != null && CollectionUtils.isNotEmpty(this.hints.hints)) {
            return true;
        }
        return false;
    }

    public boolean _hasSolutions() {

        if (this.solutionInfo != null && CollectionUtils.isNotEmpty(this.solutionInfo.solutions)) {
            return true;
        }
        return false;
    }

    public boolean _hasOptions() {

        if (this.solutionInfo != null && this.solutionInfo.optionBody != null
                && CollectionUtils.isNotEmpty(this.solutionInfo.optionBody.newOptions)) {
            return true;
        }
        return false;
    }

    @Override
    public String getGlobalId() {

        return globalQid;
    }

    @Override
    public long getExportableSize(){
        if( size != null){
            return size.getTotalSize();
        }
        return 0;
    }

}
