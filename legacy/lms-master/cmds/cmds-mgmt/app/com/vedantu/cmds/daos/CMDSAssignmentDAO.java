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
import com.vedantu.cmds.models.CMDSAssignment;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.daos.AssignmentDAO;
import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestMode;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSAssignmentDAO extends CmdsContentDAO<CMDSAssignment, ObjectId> implements
        IPublishable, ICMDSResource, IContainable<CMDSAssignment> {

    private static final ALogger          LOGGER   = Logger.of(CMDSAssignmentDAO.class);

    public static final CMDSAssignmentDAO INSTANCE = new CMDSAssignmentDAO();

    private CMDSAssignmentDAO() {

        super(CMDSAssignment.class);
    }

    public CMDSAssignment addAssignment(String userId, String name, String code, TestType type,
            String targetId, String desc, List<TestMetadata> metadata, long duration,
            SrcEntity contentSrc, Scope scope, TestResultVisibility resultVisibility)
            throws VedantuException {

        CMDSAssignment assignment = null;
        try {
            if (isContentByContentSrcAndCodeExists(contentSrc, code)) {
                throw new VedantuException(VedantuErrorCode.ASSIGNMENT_ALREAY_PRESENT,
                        "a assignment with code:" + code + " for contentSrc:" + contentSrc
                                + " already present");
            }
            assignment = new CMDSAssignment(userId, name, desc, duration, metadata, type,
                    TestMode.ONLINE, code, scope, targetId, resultVisibility);
            assignment.contentSrc = contentSrc;
            assignment.computeTotalQusAndMarks();
            /* Added by Shivank */
            assignment.completed = CMDSAssignmentDAO.INSTANCE.isReadyToPublished(assignment);
            /* Added by Shivank */
            save(assignment);
        } catch (DuplicateKey e) {
            throw new VedantuException(VedantuErrorCode.ASSIGNMENT_ALREAY_PRESENT,
                    "a assignment with code:" + code + " for contentSrc:" + contentSrc
                            + " already present", e);
        }
        return assignment;
    }

    public CMDSAssignment getAssignment(String id) throws VedantuException {

        CMDSAssignment assignment = getById(id);
        if (assignment == null) {
            throw new VedantuException(VedantuErrorCode.ASSIGNMENT_NOT_FOUND,
                    "no assignment found with id:" + id);
        }
        return assignment;
    }

    @Override
    public VedantuBaseMongoModel getPublishedEntity(String id) throws VedantuException {

        CMDSAssignment cmdsAssignment = CMDSAssignmentDAO.INSTANCE.getById(id);
        if (cmdsAssignment != null) {
            return AssignmentDAO.INSTANCE.getAssignment(cmdsAssignment.globalId);
        }
        return null;
    }

    @Override
    public SrcEntity getGlobalEntity(String id) {

        CMDSAssignment assignment = getById(id);
        return new SrcEntity(EntityType.ASSIGNMENT, assignment.globalId);
    }

    @Override
    public boolean isPublished(String id) {

        CMDSAssignment assignment = getById(id);
        return assignment.published;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        CMDSAssignment assignment = getById(id);
        List<SrcEntity> childEntities = new ArrayList<SrcEntity>();
        if (CollectionUtils.isNotEmpty(assignment.childrenIds)) {
            for (String childTestId : assignment.childrenIds) {
                childEntities.add(new SrcEntity(EntityType.CMDSASSIGNMENT, childTestId));
            }
        }
        for (String qId : assignment.__getAllQIds()) {
            childEntities.add(new SrcEntity(EntityType.CMDSQUESTION, qId));
        }

        return childEntities;
    }

    @Override
    public boolean isReadyToPublished(String id) throws VedantuException {

        CMDSAssignment assignment = getById(id);
        return assignment.completed;
    }

    @Override
    public boolean isReadyToPublished(VedantuBaseMongoModel cmdsModel) throws VedantuException {

        if (cmdsModel instanceof CMDSAssignment) {

            CMDSAssignment cmdsAssignment = (CMDSAssignment) cmdsModel;
            int addedQusCount = cmdsAssignment.__getAllQIds().size();
            if (cmdsAssignment.qusCount != addedQusCount) {
                return false;
                /*
                 * throw new VedantuException(VedantuErrorCode.INCOMPLETE_PUBLISHABLE_STATE,
                 * "incomplete test[" + cmdsAssignment.id + "], expected qusCount: " +
                 * cmdsAssignment.qusCount + ", addedQusCount:" + addedQusCount);
                 */
            }
            return true;
        }
        return false;
    }

    @Override
    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model) {

        if (!(model instanceof CMDSAssignment)) {
            return null;
        }
        CMDSAssignment cmdsAssignment = (CMDSAssignment) model;
        CMDSResourceDetails details = new CMDSResourceDetails();

        details.fromMongoModel(model);

        details.content = new SrcEntity(EntityType.CMDSASSIGNMENT, model._getStringId());
        details.queryContext = cmdsAssignment.desc;
        details.name = cmdsAssignment.name;
        return details;
    }

    public List<CMDSAssignment> getCMDSAssignments(String query, String orgId,
            List<String> includeTypes, List<String> excludeTypes, int start, int size,
            boolean published, MutableLong totalHits) {

        Query<CMDSAssignment> assignmentQuery = getDS().createQuery(CMDSAssignment.class);
        if (StringUtils.isEmpty(orgId)) {
            return null;
        }
        LOGGER.debug("orgId :" + orgId);
        assignmentQuery.filter("contentSrc.id", orgId);
        assignmentQuery.filter("scope", Scope.ORG);
        if (CollectionUtils.isNotEmpty(includeTypes)) {
            assignmentQuery.field("type").hasAnyOf(includeTypes);
        }
        if (CollectionUtils.isNotEmpty(excludeTypes)) {
            assignmentQuery.field("type").hasNoneOf(excludeTypes);
        }
        assignmentQuery.field("published").equal(published);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(query)) {
            assignmentQuery.criteria("name").contains(query);
        }
        LOGGER.debug(assignmentQuery.toString());

        totalHits.setValue(assignmentQuery.countAll());

        return assignmentQuery.offset(start).batchSize(size).asList();

    }

    @Override
    public boolean isPublished(VedantuBaseMongoModel cmdsModel) {

        if (cmdsModel instanceof CMDSAssignment) {
            CMDSAssignment test = (CMDSAssignment) cmdsModel;
            return test.published;
        }

        return false;
    }

    // TODO update this and merge with CMDSAssignmentDAO
    @Override
    public List<String> update(boolean changeState, boolean remove, List<String> brdIds)
            throws VedantuException {

        List<String> ids = new ArrayList<String>();
        if (!remove && CollectionUtils.isEmpty(brdIds)) {
            return ids;
        }

        Query<CMDSAssignment> findQuery = getDS().createQuery(entityClazz);
        findQuery.field("boardIds").hasAnyOf(brdIds);

        List<CMDSAssignment> testList = findQuery.asList();

        if (CollectionUtils.isNotEmpty(testList)) {
            for (CMDSAssignment test : testList) {
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

        if (!(model instanceof CMDSAssignment)) {

            return false;
        }

        CMDSAssignment file = (CMDSAssignment) model;

        if (file.published == true || file.globalId != null) {
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        }
        super.markDeleted(file);
        updateModel(file, Arrays.asList(ConstantsGlobal.RECORD_STATE));

        return true;

    }

    @Override
    public List<CMDSAssignment> getContainers(String id, int start, int size,
            VedantuRecordState state, MutableLong totalHits) {

        if (id == null) {
            return null;
        }
        Query<CMDSAssignment> findQuery = getQuery();

        findQuery.field("metadata.qIds").contains(id);
        if (state != null) {
            findQuery.field(ConstantsGlobal.RECORD_STATE).equal(state);
        }
        totalHits.setValue(findQuery.countAll());
        return findQuery.offset(start).offset(size).asList();

    }
    


}
