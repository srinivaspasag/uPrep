package com.vedantu.content.models.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.enums.Scope;
import com.vedantu.content.commons.interfaces.IIndexable;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.models.AbstractContentStatsModel;
import com.vedantu.content.pojos.tests.BoardQus;
import com.vedantu.content.pojos.tests.Marks;
import com.vedantu.content.pojos.tests.SimplifiedBoardNames;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.content.pojos.tests.TestQuestionSet;

public abstract class AbstractTestCommonModel extends AbstractContentStatsModel implements
        IIndexable {

    @Transient
    public static final String   DESC   = "desc";

    @Transient
    private static final ALogger LOGGER = Logger.of(AbstractTestCommonModel.class);
    public String                desc;
    public int                   qusCount;
    public int                   actualQusCount;
    public long                  duration;
    public int                   totalMarks;

    public List<TestMetadata>    metadata;
    public List<SimplifiedBoardNames> simplifiedBoardNames;

    public TestType              type;
    public TestMode              mode;

    //Unique code for test inside an organization
    public String                code;

    public long                  attempts;
    public boolean               published;
    public boolean               enablePartialMarks = false;
    public boolean               autoResumeTest = false;
    public List<String>          partialMarksQTypes;
    public List<String>          oneOrMoreMarksQTypes;
    public boolean               enableSectionLocking = false;
    public boolean               showAIR = false;
    public boolean               subjectiveTest = false;
    public boolean               isNTAPattern;

    //If this is testGroup, then members of this group
    public List<String>          childrenIds;

    //If this is part of testGroup, testGroupId
    public String                parentId;

    //If this test has different sets of question
    public List<TestQuestionSet> sets;
    public TestResultVisibility  resultVisibility;
    public String                resultVisibilityMessage;
    public String                pdfId;
    public String                password;
    public String                resultPassword;

    public AbstractTestCommonModel() {

        super();
    }

    public AbstractTestCommonModel(String userId, String name, String desc, int qusCount,
            long duration, int totalMarks, List<TestMetadata> metadata, TestType type,
            TestMode mode, String code, Scope scope, TestResultVisibility resultVisibility) {
        this(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code, scope,
                resultVisibility, null, null, null);
    }

    public AbstractTestCommonModel(String userId, String name, String desc, int qusCount,
            long duration, int totalMarks, List<TestMetadata> metadata, TestType type,
            TestMode mode, String code, Scope scope, TestResultVisibility resultVisibility,
            String pdfId, String password) {
        this(userId, name, desc, qusCount, duration, totalMarks, metadata, type, mode, code, scope,
                resultVisibility, pdfId, password, null);

    }

    public AbstractTestCommonModel(String userId, String name, String desc, int qusCount,
            long duration, int totalMarks, List<TestMetadata> metadata, TestType type,
            TestMode mode, String code, Scope scope, TestResultVisibility resultVisibility,
            String pdfId, String password, String resultPassword) {

        super();
        this.userId = userId;
        this.name = name;
        this.code = code;
        this.desc = desc;
        this.qusCount = qusCount;
        this.duration = duration;
        this.totalMarks = totalMarks;
        this.metadata = metadata;
        this.type = type;
        this.mode = mode;
        this.scope = scope;
        this.published = false;
        this.resultVisibility = resultVisibility == null ? TestResultVisibility.VISIBLE
                : resultVisibility;
        this.pdfId = pdfId;
        this.password = password;
        this.resultPassword = resultPassword;
    }

    public void addChildren(String id) {

        if (this.childrenIds == null) {
            this.childrenIds = new ArrayList<String>();
        }
        if (!this.childrenIds.contains(id)) {
            this.childrenIds.add(id);
        }
    }

    public void addQuestionSet(TestQuestionSet set) {

        if (sets == null) {
            sets = new ArrayList<TestQuestionSet>();
        }
        sets.add(set);
    }

    public List<String> __getAllQIds() {

        return __getAllQIds(null);
    }

    public List<String> __getAllQIds(String brdId) {

        List<String> qIds = new ArrayList<String>();
        if (metadata == null) {
            return qIds;
        }
        for (TestMetadata mdata : metadata) {
            if (brdId == null || StringUtils.equals(brdId, mdata.id)) {
                if (mdata.qIds != null) {
                    qIds.addAll(mdata.qIds);
                }
            }
        }
        return qIds;
    }

    public TestQuestionSet __getQuestionSet(String setName) {

        if (setName == null) {
            return null;
        }
        for (TestQuestionSet qSet : sets) {
            if (StringUtils.equalsIgnoreCase(setName, qSet.name)) {
                return qSet;
            }
        }
        return null;
    }

    public TestMetadata __getTestMetadata(String brdId) {

        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        TestMetadata mdata = null;
        for (TestMetadata m : metadata) {
            if (StringUtils.equals(brdId, m.id)) {
                mdata = m;
                break;
            }
        }
        return mdata;
    }

    public void _fillBrdIds() {

        boardIds = new HashSet<String>();
        for (TestMetadata mdata : metadata) {
            boardIds.add(mdata.id);
            if (mdata.children != null) {
                for (BoardQus b : mdata.children) {
                    boardIds.add(b.id);
                }
            }
        }
    }

    public void computeTotalQusAndMarks() {

        if (metadata != null) {
        	if(isNTAPattern==true){
        		LOGGER.info("isNTAPattern==true");
        		computeTotalQusAndMarksForNTA();
        	}
        	else{
        		qusCount = 0;
                totalMarks = 0;
                for (TestMetadata mdata : metadata) {
                    int childrenTotalMarks = 0;
                    addBoard(mdata.id);
                    if (mdata.details != null) {
                        for (TestDetails detail : mdata.details) {
                            childrenTotalMarks += (detail.marks.positive * detail.qusCount);
                        }
                        if (CollectionUtils.isNotEmpty(mdata.children)) {
                            for (BoardQus qus : mdata.children) {
                                addBoard(qus.id);
                            }
                        }
                    }

                    mdata.totalMarks = childrenTotalMarks;
                    totalMarks += mdata.totalMarks;
                    qusCount += mdata.qusCount;
                }

        	}
            
        }
    }
    
    public void computeTotalQusAndMarksForNTA(){
    	
    	 if (metadata != null) {
             qusCount = 0;
             totalMarks = 0;
             actualQusCount=0;
             for (TestMetadata mdata : metadata) {
                 int childrenTotalMarks = 0;
                 addBoard(mdata.id);
                 int totalQusCount=0;
                 if (mdata.details != null) {
                	 LOGGER.info("mdata.details ");
                     for (TestDetails detail : mdata.details) {
                     	int qusCount = 0;
                     	if(detail.maxQuestionsTobeAttempted >0){
                     		if(detail.qusCount > 0){
                         		qusCount=detail.maxQuestionsTobeAttempted;
                     		}
                     	}else{
                     		qusCount=detail.qusCount;
                     	}
                     	LOGGER.info("qusCount :  "+qusCount);
                        childrenTotalMarks += (detail.marks.positive * qusCount);
                        LOGGER.info("childrenTotalMarks :  "+childrenTotalMarks);
                        totalQusCount+=qusCount;
                        LOGGER.info("totalQusCount :  "+totalQusCount);
                     }
                     if (CollectionUtils.isNotEmpty(mdata.children)) {
                         for (BoardQus qus : mdata.children) {
                             addBoard(qus.id);
                         }
                     }
                 }
                 LOGGER.info("childrenTotalMarks :  "+childrenTotalMarks);
                 LOGGER.info("totalQusCount :  "+totalQusCount);
                 mdata.totalMarks = childrenTotalMarks;
                 mdata.maxQuestionsToBeAttemptedForBoard=totalQusCount;
                 LOGGER.info("mdata.totalMarks :  "+mdata.totalMarks);
                 LOGGER.info("mdata.maxQuestionsToBeAttemptedForBoard :  "+mdata.maxQuestionsToBeAttemptedForBoard);
                 totalMarks += mdata.totalMarks;
                 qusCount += totalQusCount;
                 actualQusCount +=mdata.qusCount;
                 LOGGER.info("totalMarks :  "+totalMarks);
                 LOGGER.info("qusCount : "+qusCount);
                 LOGGER.info("actualQusCount : "+actualQusCount);
             }
         }
    }

    public void _finishEditing() {

        Set<String> brdIds = new HashSet<String>();
        if (metadata != null) {
            for (TestMetadata mdata : metadata) {
                mdata.finishEditing();
                brdIds.add(mdata.id);
                if (mdata.children != null) {
                    for (BoardQus bQus : mdata.children) {
                        brdIds.add(bQus.id);
                    }
                }
            }
            this.boardIds = brdIds;
        }
    }

    public void _removeBrdIds(List<String> brdIds) {

        if (CollectionUtils.isNotEmpty(this.boardIds)) {
            this.boardIds.removeAll(brdIds);

        }

        List<String> allQIdsForRemoval = new ArrayList<String>();
        List<TestMetadata> updatedMetadatas = new ArrayList<TestMetadata>();
        if (CollectionUtils.isNotEmpty(this.metadata)) {

            for (TestMetadata currentMetadata : this.metadata) {

                if (!brdIds.contains(currentMetadata.id)) {

                    if (CollectionUtils.isNotEmpty(currentMetadata.children)) {

                        List<BoardQus> boardQusMapForRemoval = new ArrayList<BoardQus>();
                        List<String> qIdsForRemovalBoards = new ArrayList<String>();

                        for (BoardQus qus : currentMetadata.children) {
                            if (brdIds.contains(qus.id)) {
                                boardQusMapForRemoval.add(qus);
                                qIdsForRemovalBoards.addAll(qus.qIds);
                            }
                        }

                        allQIdsForRemoval.addAll(qIdsForRemovalBoards);
                        currentMetadata.children.removeAll(boardQusMapForRemoval);
                        if (currentMetadata.qIds != null) {
                            currentMetadata.qIds.removeAll(qIdsForRemovalBoards);

                        }
                        if (CollectionUtils.isNotEmpty(currentMetadata.children)) {
                            if (CollectionUtils.isNotEmpty(currentMetadata.details)) {
                                for (TestDetails detail : currentMetadata.details) {
                                    if (detail.qIds != null) {
                                        detail.qIds.removeAll(qIdsForRemovalBoards);
                                    }
                                }
                            }
                            updatedMetadatas.add(currentMetadata);
                        }
                    }

                } else {
                    if (currentMetadata.qIds != null) {
                        allQIdsForRemoval.addAll(currentMetadata.qIds);
                    }
                }
            }
        }
        this.metadata = updatedMetadatas;
        LOGGER.debug("Current updated test" + this.metadata);
        if (CollectionUtils.isNotEmpty(this.sets)) {
            List<TestQuestionSet> updatedSets = new ArrayList<TestQuestionSet>();
            for (TestQuestionSet set : this.sets) {
                if (set.qIds != null) {
                    set.qIds.removeAll(allQIdsForRemoval);
                    if (CollectionUtils.isNotEmpty(set.qIds)) {
                        updatedSets.add(set);
                    }
                }
            }
            this.sets = updatedSets;

        }
        this._finishEditing();
        LOGGER.debug("Current test" + this.toString());
    }

    public Map<String, Marks> __getMarksMap() {

        Map<String, Marks> qIdToMarks = new HashMap<String, Marks>();
        if (metadata == null) {
            return qIdToMarks;
        }

        for (TestMetadata mData : metadata) {
            if (mData.marks == null) {
                // this will populate marks map
                mData.finishEditing();
            }

            if (mData.marks != null) {
                qIdToMarks.putAll(mData.marks);
            }
        }
        return qIdToMarks;
    }
}
