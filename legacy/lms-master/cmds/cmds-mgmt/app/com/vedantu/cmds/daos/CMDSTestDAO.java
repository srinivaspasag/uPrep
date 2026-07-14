package com.vedantu.cmds.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.mgmt.interfaces.IContainable;
import com.vedantu.cmds.models.CMDSTest;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.pojos.tests.TestDetails;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSTestDAO extends CmdsContentDAO<CMDSTest, ObjectId> implements IPublishable,
        ICMDSResource, IContainable<CMDSTest> {

    private static final ALogger    LOGGER   = Logger.of(CMDSTestDAO.class);

    public static final CMDSTestDAO INSTANCE = new CMDSTestDAO();

    private CMDSTestDAO() {

        super(CMDSTest.class);
    }

    public CMDSTest addTest(String userId, String name, String code, TestType type,
            String targetId, String desc, List<TestMetadata> metadata, long duration,
            SrcEntity contentSrc, Scope scope, TestResultVisibility resultVisibility,
            String resultVisibilityMessage, boolean showAIR , boolean subjectiveTest,boolean isNTAPattern) throws VedantuException {
    	
        CMDSTest test = null;
        try {

            if (isContentByContentSrcAndCodeExists(contentSrc, code)) {
                throw new VedantuException(VedantuErrorCode.TEST_ALREAY_PRESENT,
                        "a test with code:" + code + " for contentSrc:" + contentSrc
                                + " already present");
            }
            
            test = new CMDSTest(userId, name, desc, duration, metadata, type, TestMode.ONLINE,
                    code, scope, targetId, resultVisibility);
            test.contentSrc = contentSrc;
            test.resultVisibilityMessage = resultVisibilityMessage;
            test.showAIR = showAIR;
            test.subjectiveTest = subjectiveTest;
            test.isNTAPattern=isNTAPattern;
            LOGGER.info("isnide  addTest");
            test.computeTotalQusAndMarks();
            checkForMaxQuestionsToBeAttempted(test);
            test.oneOrMoreMarksQTypes = new ArrayList<String>();
            test.oneOrMoreMarksQTypes.add("MCQ");
            test.oneOrMoreMarksQTypes.add("PARA");
            test.oneOrMoreMarksQTypes.add("MATRIX");
            save(test);
        } catch (DuplicateKey e) {
            throw new VedantuException(VedantuErrorCode.TEST_ALREAY_PRESENT, "a test with code:"
                    + code + " for contentSrc:" + contentSrc + " already present", e);
        }
        return test;
    }

    public CMDSTest checkForMaxQuestionsToBeAttempted(CMDSTest cmdsTest){
    	//this method is for updating maxQuestionsTobeAttempted with qusCount , if maxQuestionsTobeAttempted is zero.
    	if(cmdsTest.isNTAPattern){
    		for(TestMetadata testMetadata :cmdsTest.metadata){
    			for(TestDetails details:testMetadata.details){
    				if(details.maxQuestionsTobeAttempted==0){
    					if(details.qusCount > 0){
    						details.maxQuestionsTobeAttempted=details.qusCount;
    					}
    				}
    			}
    		}
    		
    	}
    	save(cmdsTest);
    	
		return cmdsTest;
    	
    }

    public CMDSTest getTest(String id) throws VedantuException {

        CMDSTest test = getById(id);
        if (test == null) {
            throw new VedantuException(VedantuErrorCode.TEST_NOT_FOUND, "no test found with id:"
                    + id);
        }
        return test;
    }

    @Override
    public VedantuBaseMongoModel getPublishedEntity(String id) throws VedantuException {

        CMDSTest cmdsTest = CMDSTestDAO.INSTANCE.getById(id);
        if (cmdsTest != null) {
            return TestDAO.INSTANCE.getTest(cmdsTest.globalTestId);
        }
        return null;
    }

    @Override
    public SrcEntity getGlobalEntity(String id) {

        CMDSTest test = getById(id);
        return new SrcEntity(EntityType.TEST, test.globalTestId);
    }

    @Override
    public boolean isPublished(String id) {

        CMDSTest test = getById(id);
        return test.published;
    }

    @Override
    public boolean isPublished(VedantuBaseMongoModel cmdsModel) {

        if (cmdsModel instanceof CMDSTest) {
            CMDSTest test = (CMDSTest) cmdsModel;
            return test.published;
        }

        return false;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        CMDSTest test = getById(id);
        List<SrcEntity> childEntities = new ArrayList<SrcEntity>();
        if (CollectionUtils.isNotEmpty(test.childrenIds)) {
            for (String childTestId : test.childrenIds) {
                childEntities.add(new SrcEntity(EntityType.CMDSTEST, childTestId));
            }
        }
        for (String qId : test.__getAllQIds()) {
            childEntities.add(new SrcEntity(EntityType.CMDSQUESTION, qId));
        }

        return childEntities;
    }

    @Override
    public boolean isReadyToPublished(String id) throws VedantuException {

        CMDSTest test = getById(id);
        return test.completed;
    }

    @Override
    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        if (cmdsModel instanceof CMDSTest) {

            CMDSTest cmdsTest = (CMDSTest) cmdsModel;
            int addedQusCount = cmdsTest.__getAllQIds().size();
            LOGGER.info("inside isReadyToPublished cmdsTest.isNTAPattern : "+cmdsTest.isNTAPattern);
            if(cmdsTest.isNTAPattern){
            	if(addedQusCount==cmdsTest.actualQusCount){
            		LOGGER.info("inside isReadyToPublished addedQusCount==cmdsTest.actualQusCount : ");
            		return true;
            	}
            }
            if (cmdsTest.qusCount == addedQusCount) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        if (!(model instanceof CMDSTest)) {
            return null;
        }
        CMDSTest cmdsTest = (CMDSTest) model;
        CMDSResourceDetails details = new CMDSResourceDetails();

        details.fromMongoModel(model);

        details.content = new SrcEntity(EntityType.CMDSTEST, model._getStringId());
        details.queryContext = cmdsTest.desc;
        details.name = cmdsTest.name;
        return details;
    }

    public List<CMDSTest>
            getCMDSTests(String query, String orgId, List<String> includeTypes,
                    List<String> excludeTypes, int start, int size, boolean published,
                    MutableLong totalHits) {

        return getCMDSTests(query, orgId, includeTypes, excludeTypes, start, size, published, null,
                totalHits);
    }

    public List<CMDSTest> getCMDSTests(String query, String orgId, List<String> includeTypes,
            List<String> excludeTypes, int start, int size, boolean published,
            VedantuRecordState state, MutableLong totalHits) {

        Query<CMDSTest> testQuery = getDS().createQuery(CMDSTest.class);
        if (StringUtils.isEmpty(orgId)) {
            return null;
        }
        LOGGER.debug("orgId :" + orgId);
        testQuery.filter("contentSrc.id", orgId);
        testQuery.filter("scope", Scope.ORG);
        if (CollectionUtils.isNotEmpty(includeTypes)) {
            testQuery.field("type").hasAnyOf(includeTypes);
        }
        if (CollectionUtils.isNotEmpty(excludeTypes)) {
            testQuery.field("type").hasNoneOf(excludeTypes);
        }
        testQuery.field("published").equal(published);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(query)) {
            testQuery.criteria("name").contains(query);
        }
        if (state != null) {
            testQuery.field(RECORD_STATE).equal(state);
        }
        LOGGER.debug(testQuery.toString());

        totalHits.setValue(testQuery.countAll());

        return testQuery.offset(start).limit(size).asList();

    }

    // TODO update this and merge with CMDSAssignmentDAO
    @Override
    public List<String> update(boolean changeState, boolean remove, List<String> brdIds)
            throws VedantuException {

        List<String> ids = new ArrayList<String>();
        if (!remove && CollectionUtils.isEmpty(brdIds)) {
            return ids;
        }

        Query<CMDSTest> findQuery = getQuery();
        findQuery.field("boardIds").hasAnyOf(brdIds);
        List<CMDSTest> testList = findQuery.asList();
        if (CollectionUtils.isNotEmpty(testList)) {
            for (CMDSTest test : testList) {
                test._removeBrdIds(brdIds);
                ids.add(test._getStringId());
                if (changeState) {
                    test.completed = false;
                    test.published = false;

                }
                this.save(test);
            }
        }
        return ids;
    }

    @Override
    public boolean deleteByModel(VedantuBaseMongoModel model) throws VedantuException {

        if (!(model instanceof CMDSTest)) {
            return false;
        }

        CMDSTest test = (CMDSTest) model;

        if (test.published == true || test.globalTestId != null) {
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        }
        super.markDeleted(test);
        updateModel(test, Arrays.asList(ConstantsGlobal.RECORD_STATE));
        return true;
    }

    @Override
    public List<CMDSTest> getContainers(String id, int start, int size, VedantuRecordState state,
            MutableLong totalHits) {

        if (id == null) {
            return null;
        }
        Query<CMDSTest> findQuery = getQuery();
        findQuery.field("metadata.qIds").contains(id);
        if (state != null) {
            findQuery.field(ConstantsGlobal.RECORD_STATE).equal(state);
        }
        totalHits.setValue(findQuery.countAll());
        return findQuery.offset(start).limit(size).asList();

    }



}
