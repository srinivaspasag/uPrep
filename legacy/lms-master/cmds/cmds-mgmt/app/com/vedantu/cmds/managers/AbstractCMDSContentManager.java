package com.vedantu.cmds.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.AbstractFacetBuilder;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CMDSModuleDAO;
import com.vedantu.cmds.daos.CmdsContentDAO;
import com.vedantu.cmds.mgmt.interfaces.ICMDSResource;
import com.vedantu.cmds.mgmt.publishers.EntityTypePublisherFactory;
import com.vedantu.cmds.mgmt.publishers.IPublisher;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.event.search.details.CMDSContentLinkDetails;
import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.cmds.pojos.content.question.CMDSContentLinkInfo;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IPublishable;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.IMongoAware;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.content.search.details.AbstractSearchDetail;
import com.vedantu.events.utils.EventDetailsFactory;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.search.utils.ElasticSearchUtils;
import com.vedantu.user.managers.AbstractVedantuEventManager;

public abstract class AbstractCMDSContentManager extends AbstractContentManager implements
        IPublisher {

    private static final ALogger LOGGER = Logger.of(AbstractCMDSContentManager.class);

    public boolean publish(final String userId, final String orgId,
            final SrcEntity contentToBePublished, final String jobId) throws VedantuException {

        LOGGER.debug("Checking for jobId " + jobId);
        if (StringUtils.isEmpty(jobId)) {
            throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
        }

        LOGGER.debug("Called common publishing with jobId: " + jobId);
        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE.getById(jobId);

        if (status == null) {
            LOGGER.debug("Called common publishing with null status ");
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED);

        }

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> basicEntityTypeDAO = EntityTypeDAOFactory.INSTANCE
                .get(contentToBePublished.type);

        if (!(basicEntityTypeDAO instanceof IPublishable)) {
            LOGGER.debug("  Entity of type " + contentToBePublished.type
                    + " not of publishable type ");
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED);
        }
        prePublish(contentToBePublished);

        IPublishable publishableDAO = (IPublishable) basicEntityTypeDAO;
        List<SrcEntity> childrens = publishableDAO.getChildren(contentToBePublished.id);

        if (CollectionUtils.isNotEmpty(childrens)) {

            status.numOfSteps += childrens.size();
            EntityOperationStatusDAO.INSTANCE.save(status);
            for (SrcEntity childPublishableContent : childrens) {

                IPublisher childPublisher = EntityTypePublisherFactory.INSTANCE
                        .get(childPublishableContent.type);
                EntityOperationStatus childStatus = new EntityOperationStatus();
                childStatus.type = childPublishableContent.type;
                childStatus.id = childPublishableContent.id;

                EntityOperationStatusDAO.INSTANCE.save(childStatus);

                boolean value = childPublisher.publish(userId, orgId, childPublishableContent,
                        childStatus._getStringId());
                // update status
                if (!value) {
                    throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
                }

                status.numOfStepsCompleted++;
                EntityOperationStatusDAO.INSTANCE.save(status);

            }
        }
        LOGGER.debug("publishable entity : " + contentToBePublished + ", publishableDAO: "
                + publishableDAO.getClass());
        boolean alreadyPublished = publishableDAO.isPublished(contentToBePublished.id);
        VedantuBaseMongoModel publishedEntity = null;
        //if (!alreadyPublished) {

            synchronized (contentToBePublished.toString().intern()) {
                alreadyPublished = publishableDAO.isPublished(contentToBePublished.id);
                //if (!alreadyPublished) {
                    try {
                        LOGGER.debug("Publishing parent entity type : " + contentToBePublished.type
                                + ", id:" + contentToBePublished.id);
                        // give actual model for this
                        publishedEntity = publish(userId, orgId, contentToBePublished);

                    } catch (VedantuException exception) {
                        status.errorCode = exception.errorCode.name();
                        EntityOperationStatusDAO.INSTANCE.save(status);
                        throw exception;
                    }

                //}
            }
        //}
        postPublish(publishedEntity);

        if (alreadyPublished || publishedEntity != null) {
            status.numOfStepsCompleted++;
            EntityOperationStatusDAO.INSTANCE.save(status);
        }

        return true;
    }

    public boolean delete(final String userId, final String orgId,
            final SrcEntity contentToBePublished, final String jobId) throws VedantuException {

        LOGGER.debug("Checking for jobId " + jobId);
        if (!StringUtils.isEmpty(jobId)) {
            throw new VedantuException(VedantuErrorCode.ALREADY_PUBLISHED);
        }

        LOGGER.debug("Called common publishing with jobId: " + jobId);
        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE.getById(jobId);

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> basicEntityTypeDAO = EntityTypeDAOFactory.INSTANCE
                .get(contentToBePublished.type);

        // if (!(basicEntityTypeDAO instanceof IPublishable)) {
        // LOGGER.debug("  Entity of type " + contentToBePublished.type
        // + " not of publishable type ");
        // throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_PUBLISHED);
        // }
        // prePublish(contentToBePublished);

        IPublishable publishableDAO = (IPublishable) basicEntityTypeDAO;
        List<SrcEntity> childrens = publishableDAO.getChildren(contentToBePublished.id);

        if (CollectionUtils.isNotEmpty(childrens)) {

            status.numOfSteps += childrens.size();
            EntityOperationStatusDAO.INSTANCE.save(status);
            for (SrcEntity childPublishableContent : childrens) {

                IPublisher childPublisher = EntityTypePublisherFactory.INSTANCE
                        .get(childPublishableContent.type);
                EntityOperationStatus childStatus = new EntityOperationStatus();
                childStatus.type = childPublishableContent.type;
                childStatus.id = childPublishableContent.id;

                EntityOperationStatusDAO.INSTANCE.save(childStatus);

                boolean value = childPublisher.publish(userId, orgId, childPublishableContent,
                        childStatus._getStringId());
                // update status
                if (!value) {
                    throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
                }

                status.numOfStepsCompleted++;
                EntityOperationStatusDAO.INSTANCE.save(status);

            }
        }
        LOGGER.debug("publishable entity : " + contentToBePublished + ", publishableDAO: "
                + publishableDAO.getClass());
        boolean alreadyPublished = publishableDAO.isPublished(contentToBePublished.id);
        VedantuBaseMongoModel publishedEntity = null;
        if (!alreadyPublished) {

            synchronized (contentToBePublished.toString().intern()) {
                alreadyPublished = publishableDAO.isPublished(contentToBePublished.id);
                if (!alreadyPublished) {
                    try {
                        LOGGER.debug("Publishing parent entity type : " + contentToBePublished.type
                                + ", id:" + contentToBePublished.id);
                        // give actual model for this
                        publishedEntity = publish(userId, orgId, contentToBePublished);

                    } catch (VedantuException exception) {
                        status.errorCode = exception.errorCode.name();
                        EntityOperationStatusDAO.INSTANCE.save(status);
                        throw exception;
                    }

                }
            }
        }
        postPublish(publishedEntity);

        if (alreadyPublished || publishedEntity != null) {
            status.numOfStepsCompleted++;
            EntityOperationStatusDAO.INSTANCE.save(status);
        }

        return true;
    }

    protected VedantuBaseMongoModel publish(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        return null;
    }

    protected VedantuBaseMongoModel delete(String userId, String orgId, SrcEntity content)
            throws VedantuException {

        return null;
    }

    protected static List<ModelBasicInfo> getBasicInfoFromLinks(List<CMDSContentLink> links,
            List<ModelBasicInfo> basicInfos) throws VedantuException {

        if (CollectionUtils.isNotEmpty(links)) {
            for (Object listObject : links) {
                if (listObject instanceof CMDSContentLink) {
                    try {
                        LOGGER.debug("Getting basic info for "
                                + ((CMDSContentLink) listObject).source.type + " id"
                                + ((CMDSContentLink) listObject).source.id);
                        @SuppressWarnings("rawtypes")
                        VedantuBasicDAO basicDao = EntityTypeDAOFactory.INSTANCE
                                .get(((CMDSContentLink) listObject).source.type);
                        if (basicDao == null) {
                            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
                        }

                        @SuppressWarnings("rawtypes")
                        VedantuBasicDAO targetDAO = EntityTypeDAOFactory.INSTANCE
                                .get(((CMDSContentLink) listObject).target.type);
                        if (targetDAO == null) {
                            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
                        }

                        ModelBasicInfo sourceBasicInfo = basicDao
                                .getBasicInfo(((CMDSContentLink) listObject).source.id);

                        ModelBasicInfo targetInfo = targetDAO
                                .getBasicInfo(((CMDSContentLink) listObject).target.id);

                        if (sourceBasicInfo == null || targetInfo == null) {
                            LOGGER.error("Database mismatch ");
                            continue;
                        }
                        CMDSContentLinkInfo linkInfo = (CMDSContentLinkInfo) ((CMDSContentLink) listObject)
                                .toBasicInfo();
                        linkInfo.setSourceTarget(sourceBasicInfo, targetInfo);
                        // Assigning downloadable state
                        if(((CMDSContentLink) listObject).source.type == EntityType.CMDSMODULE){
                            int totalContentCount = CMDSModuleDAO.INSTANCE.getById(((CMDSContentLink) listObject).source.id).totalContentCount;
                            if(((CMDSContentLink) listObject).getDownloadableEntities().size() == 0){
                                linkInfo.downloadableState = "DISABLED";
                            }else if(((CMDSContentLink) listObject).getDownloadableEntities().size() < totalContentCount){
                                linkInfo.downloadableState = "PARTIALLY_ENABLED";
                            }else if(((CMDSContentLink) listObject).getDownloadableEntities().size() >= totalContentCount){
                                linkInfo.downloadableState = "ENABLED";
                            }
                        }

                        if(((CMDSContentLink) listObject).source.type == EntityType.CMDSTEST){
                            if(((CMDSContentLink) listObject).getSchedule() != null){
                                linkInfo.startsIn = ((CMDSContentLink) listObject).getSchedule().startTime == null ? getRemainingTime(((CMDSContentLink) listObject).timeCreated) : getRemainingTime(((CMDSContentLink) listObject).getSchedule().startTime);
                                linkInfo.endsIn = ((CMDSContentLink) listObject).getSchedule().endTime == null ? getRemainingTime(((CMDSContentLink) listObject).timeCreated) : getRemainingTime(((CMDSContentLink) listObject).getSchedule().endTime);
                                linkInfo.closesIn = ((CMDSContentLink) listObject).getSchedule().closeTime == null ? getRemainingTime(((CMDSContentLink) listObject).timeCreated) : getRemainingTime(((CMDSContentLink) listObject).getSchedule().closeTime);
                            }else{
                                linkInfo.startsIn = getRemainingTime(((CMDSContentLink) listObject).timeCreated);
                                linkInfo.endsIn = getRemainingTime(((CMDSContentLink) listObject).timeCreated);
                                linkInfo.closesIn = getRemainingTime(((CMDSContentLink) listObject).timeCreated);
                            }
                        }
                        basicInfos.add(linkInfo);
                        LOGGER.debug("Found link info " + linkInfo);

                    } catch (VedantuException exception) {
                        LOGGER.error("Decorating infos for links ", exception);
                    }
                }

            }
        }
        return basicInfos;
    }

    private static Long getRemainingTime(long timeCreated) {
        // TODO Auto-generated method stub
        return timeCreated - System.currentTimeMillis();
    }

    public static Long getRemainingTime(Date date){
        return date.getTime() - new Date().getTime();
    }

    static long getBasicInfoFromESSearch(SearchResponse response,
            List<CMDSResourceDetails> resources) {
        return getBasicInfoFromESSearch(response, resources, null);
    }

    static long getBasicInfoFromESSearch(SearchResponse response,
            List<CMDSResourceDetails> resources,String type) {

        if (response == null || response.getHits().getTotalHits() == 0) {
            CMDSResourcesManager.LOGGER.error("empty search response for query : ");
            return 0;
        }
        CMDSResourcesManager.LOGGER.debug(" Search responses " + response.getHits());

        SearchHits allHits = response.getHits();
        long totalHits = allHits.getTotalHits();
        CMDSResourcesManager.LOGGER.debug("totalHits: " + totalHits);
        for (SearchHit hits : allHits.getHits()) {
            //CMDSResourcesManager.LOGGER.debug("hits : " + hits.sourceAsString());
            CMDSResourceDetails model = ObjectMapperUtils.convertValue(hits.sourceAsMap(),
                    CMDSResourceDetails.class);

            if(type != null && type.equals("CMDSQUESTION")){
                SrcEntity content = new SrcEntity();
                content.id = model.id;
                content.type = EntityType.CMDSQUESTION;

                model.content = content;
                model.id = "cmdsquestion_"+model.id;
            }
            resources.add(model);
        }

        return totalHits;
    }

    static List<CMDSResourceDetails> getBasicInfoOFParaQuestionFromESSearch(List<String> qIds) {

        List<CMDSResourceDetails> resources = new ArrayList<CMDSResourceDetails>();

        if (qIds == null || qIds.size() == 0) {
            CMDSResourcesManager.LOGGER.error("empty search response for getting paragraph questions : ");
            return resources;
        }
        CMDSResourcesManager.LOGGER.debug("Ready to get Para question for ES resources");
        // ES query
        TermsQueryBuilder query = QueryBuilders.inQuery("content.id", qIds.toArray(new String[qIds.size()]));

        SearchResponse response = ElasticSearchUtils.getSearchResponse(query,"timeCreated", "desc", 0, qIds.size(),
                EntityType.CMDSRESOURCE.getIndexName(), EntityType.CMDSRESOURCE.getIndexType()
                        .toLowerCase(), null, false, (AbstractFacetBuilder[]) null);
        if (response == null || response.getHits().getTotalHits() == 0) {
            CMDSResourcesManager.LOGGER.error("empty search response for ES query : ");
            return resources;
        }
        CMDSResourcesManager.LOGGER.debug(" Search responses " + response.getHits());

        SearchHits allHits = response.getHits();
        long totalHits = allHits.getTotalHits();
        CMDSResourcesManager.LOGGER.debug("totalHits: " + totalHits);
        for (SearchHit hits : allHits.getHits()) {
            CMDSResourcesManager.LOGGER.debug("hits : " + hits.sourceAsString());
            CMDSResourceDetails model = ObjectMapperUtils.convertValue(hits.sourceAsMap(),
                    CMDSResourceDetails.class);
            resources.add(model);
        }

        return resources;
    }

    static long getLinkIds(SearchResponse response, List<String> links) {

        if (response == null || response.getHits().getTotalHits() == 0) {
            CMDSResourcesManager.LOGGER.error("empty search response for query : ");
            return 0;
        }
        CMDSResourcesManager.LOGGER.debug(" Search responses " + response.getHits());

        SearchHits allHits = response.getHits();
        long totalHits = allHits.getTotalHits();
        CMDSResourcesManager.LOGGER.debug("totalHits: " + totalHits);
        for (SearchHit hits : allHits.getHits()) {

            CMDSContentLinkDetails model = ObjectMapperUtils.convertValue(hits.sourceAsMap(),
                    CMDSContentLinkDetails.class);

            links.add(model.id);
        }

        return totalHits;
    }

    protected static String addAsCMDSResource(SrcEntity modelEntity, EventActionType eventType,
            VedantuBaseMongoModel model) {

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> dao = EntityTypeDAOFactory.INSTANCE
                .get(modelEntity.type);
        String parentESId = null;
        if (dao instanceof ICMDSResource) {
            CMDSResourceDetails resourceDetails = ((ICMDSResource) dao)
                    .getCMDSResourceDetails(model);
            resourceDetails.id = getResourceId(resourceDetails.content);
            parentESId = addLiveEntityToSearchIndex(resourceDetails, EntityType.CMDSRESOURCE, true);

        }
        return parentESId;
    }

    protected static boolean removeCMDSResource(SrcEntity modelEntity) {

        @SuppressWarnings("unchecked")
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> dao = EntityTypeDAOFactory.INSTANCE
                .get(modelEntity.type);

        if (dao instanceof ICMDSResource) {

            String id = getResourceId(modelEntity);
            return removeLiveEntityToSearchIndex(id, EntityType.CMDSRESOURCE, true);

        }
        return false;
    }

    protected static boolean updateContentLink(String linkId, String userId, SrcEntity content,
            SrcEntity srcEntity, long timeCreated, Scope scope, long rank) {

        CMDSContentLinkDetails libraryContentLinkDetails = new CMDSContentLinkDetails(linkId,
                userId, content, srcEntity, scope, timeCreated, rank);

        SrcEntity resource = new SrcEntity(EntityType.CMDSRESOURCE, getResourceId(content));

        updateUserActionMappintToEs(libraryContentLinkDetails, resource, UserActionType.ADDED,
                EventActionType.ADD);
        return true;
    }

    // protected static boolean updateParentCompletionStatus(SrcEntity parent, SrcEntity entity) {
    //
    // CMDSModuleDAO.INSTANCE.updateModuleStatus(entity);
    // }

    public static void generateEventAysc(final String userId, final VedantuBaseMongoModel model,
            final EventActionType action, final EventType eventType,
            final UserActionType userAction, final boolean notificationEnabled)
            throws VedantuException {

        try {
            LOGGER.info("inside synchronized block for synchronizedKey: "
                    + (userId + eventType.name()).intern());
            AbstractSearchDetail details = (AbstractSearchDetail) EventDetailsFactory.getInstance()
                    .getDetails(eventType);

            details.userAction = userAction;
            details.isNotificationEnabled = notificationEnabled;

            details.setAction(action.name());
            IMongoAware mongoDetails = details;
            mongoDetails.fromMongoModel(model);
            LOGGER.debug("loaded IndexDetails from mongomodel: " + details);
            AbstractVedantuEventManager.generateEventAysc(userId, details, eventType);

        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            throw new VedantuException(VedantuErrorCode.EVENT_NOT_SCHEDULED, exception);
        }
    }

    protected static String getResourceId(SrcEntity content) {

        return new String(content.type + "_" + content.id).toLowerCase();
    }

    @Override
    public void prePublish(SrcEntity content) {

        // TODO Auto-generated method stub

    }

    @Override
    public void postPublish(VedantuBaseMongoModel model) {

        // TODO Auto-generated method stub

    }

    protected static boolean delete(SrcEntity entity) throws VedantuException {

        LOGGER.debug("Inside AbstractCMDSContent Delete function");
        @SuppressWarnings("rawtypes")
        VedantuBasicDAO basicDAO = EntityTypeDAOFactory.INSTANCE.get(entity.type);
        if (basicDAO instanceof CmdsContentDAO) {
            @SuppressWarnings("unchecked")
            CmdsContentDAO<VedantuBaseMongoModel, ObjectId> contentDAO = (CmdsContentDAO<VedantuBaseMongoModel, ObjectId>) basicDAO;
            VedantuBaseMongoModel baseModel = contentDAO.getById(entity.id);

            if (contentDAO instanceof IPublishable) {
                if (baseModel.recordState == VedantuRecordState.DELETED) {

                    throw new VedantuException(VedantuErrorCode.ALREADY_DELETED);

                }

                return contentDAO.deleteByModel(baseModel);

            }

        }

        return false;
    }

    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId)
            throws VedantuException {

        // TODO Auto-generated method stub
        return null;
    }

}
