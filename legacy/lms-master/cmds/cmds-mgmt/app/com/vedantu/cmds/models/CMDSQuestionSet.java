package com.vedantu.cmds.models;

import java.util.List;
import java.util.Set;

import play.Logger;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.managers.CMDSLibraryManager;
import com.vedantu.cmds.pojos.content.question.CMDSQuestionSetInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionStatus;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.pojos.tests.Metadata;

@Entity(value = "cmdsquestionsets", noClassnameStored = true)
public class CMDSQuestionSet extends AbstractBoardEntityTagModel {

    @Transient
    public final static String QUESTION_IDS="questionIds";
    
    public List<String>   questionIds;

    public String         type;
    public QuestionStatus status;

    public int            numberOfQuestionsComplete;
    public boolean        published;

    public Metadata       metadata;
    public String         fileName;

    public CMDSQuestionSet() {

        scope = Scope.ORG;
        this.contentType = EntityType.CMDSQUESTIONSET;
    }

    public CMDSQuestionSet(String name, List<String> questionIds, String userId, String type,
            String status, Set<String> tags, Difficulty difficulty, Metadata metadata) {

        this.name = name;
        this.questionIds = questionIds;
        this.userId = userId;
        this.type = type;
        this.status = QuestionStatus.valueOfKey(status);
        // this.numberOfQuestionsComplete = 0;
        // this.assignedBy = "";
        this.metadata = metadata;
        this.difficulty = difficulty;
        scope = Scope.ORG;
        this.completed = true;
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        // TODO Auto-generated method stub
        Logger.debug("Creating basic info for questionSet Id  in values: " + id + "  "
                + id.toString() + " numbers to complete" + numberOfQuestionsComplete + contentSrc);

        SrcEntity contentEntity = new SrcEntity(EntityType.CMDSQUESTIONSET, id.toString());
        Logger.debug("ProgramsAddedTo : " + id);

        //
        int programsAddedTo = CMDSLibraryManager.getAllProgramsAddedTo(contentEntity,
                CmdsContentLinkType.ADDED);
        Logger.debug("ProgramsAddedTo : " + programsAddedTo);
        String orgId = null;
        if (contentSrc != null) {
            orgId = contentSrc.id;

        }
        Logger.debug("OrgId : " + orgId);
        CMDSQuestionSetInfo info = new CMDSQuestionSetInfo(_getStringId(), name,
                EntityType.CMDSQUESTIONSET, orgId, timeCreated, lastUpdated, userId,
                programsAddedTo, published, completed, true, recordState, status, numberOfQuestionsComplete,
                metadata);
        Logger.debug("numberOfQuestionToComplete : " + numberOfQuestionsComplete);
        //
        // Logger.debug("Created basic info for folder  : " + info);
        return info;
    }

}
