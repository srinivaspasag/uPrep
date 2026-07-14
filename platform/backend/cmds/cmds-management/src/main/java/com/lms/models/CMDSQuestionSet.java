package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.Difficulty;
import com.lms.enums.QuestionStatus;
import com.lms.pojos.tests.Metadata;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Document(value = "cmdsquestionsets")
public class CMDSQuestionSet extends AbstractBoardEntityTagModel {

    @Transient
    public final static String QUESTION_IDS = "questionIds";

    public List<String> questionIds;

    public String type;
    public QuestionStatus status;

    public int numberOfQuestionsComplete;
    public boolean published;

    public Metadata metadata;
    public String fileName;

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

     /*   // TODO Auto-generated method stub

        SrcEntity contentEntity = new SrcEntity(EntityType.CMDSQUESTIONSET, id.toString());

        //
        int programsAddedTo = CMDSLibraryManager.getAllProgramsAddedTo(contentEntity,
                CmdsContentLinkType.ADDED);
        logger.debug("ProgramsAddedTo : " + programsAddedTo);
        String orgId = null;
        if (contentSrc != null) {
            orgId = contentSrc.id;

        }
        logger.debug("OrgId : " + orgId);
        CMDSQuestionSetInfo info = new CMDSQuestionSetInfo(_getStringId(), name,
                EntityType.CMDSQUESTIONSET, orgId, timeCreated, lastUpdated, userId,
                programsAddedTo, published, completed, true, recordState, status, numberOfQuestionsComplete,
                metadata);
        logger.debug("numberOfQuestionToComplete : " + numberOfQuestionsComplete);
        //
        // Logger.debug("Created basic info for folder  : " + info);
        return info;*/
        return null;
    }

}
