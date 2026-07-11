package com.lms.components;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.CmdsContentLinkType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.*;
import com.lms.models.event.search.details.CMDSResourceDetails;
import com.lms.repo.CMDSFolderRepo;
import com.lms.repo.CMDSModuleRepo;
import com.lms.repository.CMDSQuestionRepo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Component
public class CMDSContentManager extends AbstractContentManager {
    private static final Logger logger = LoggerFactory.getLogger(CMDSContentManager.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CMDSFolderRepo cmdsFolderRepo;
    @Autowired
    private CMDSModuleRepo cmdsModuleRepo;
    @Autowired
    private CMDSQuestionRepo cmdsQuestionRepo;


    public List<CMDSContentLink> getCmdsContentLinks(SrcEntity content, SrcEntity targetEntity,
                                                     CmdsContentLinkType linkType, String actorId, Scope scope, int start, int size,
                                                     VedantuRecordState recordState, String order) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if (targetEntity != null) {
            if (targetEntity.type != null) {
                criteria.and("target.type").is(targetEntity.type);
                if (targetEntity.id != null) {
                    criteria.and("target.id").is(targetEntity.id);

                }
            }
        }
        if (content != null) {
            if (content.type != null) {
                criteria.and("source.type").is(content.type);
                if (content.id != null) {
                    criteria.and("source.id").is(content.id);

                }
            }
        }
        if (linkType != null || linkType != CmdsContentLinkType.UNKNOWN) {
            criteria.and("linkType").is(linkType);
        }
        if (scope != null || scope != Scope.UNKNOWN) {
            criteria.and("scope").is(scope);
        }
        logger.debug("Querying for " + CMDSContentLink.class);
        if (recordState != null) {
            criteria.and("recordState").is(recordState);
        }
        query.addCriteria(criteria);
        if (order != null) {
            query.skip(start).limit(size);
            //query.with
        }
        List<CMDSContentLink> cmdsContentLinks = mongoTemplate.find(query, CMDSContentLink.class);
        return cmdsContentLinks;
    }


    public List<CMDSContentLink> getCmdsContentLinks(SrcEntity content, SrcEntity folderEntity, CmdsContentLinkType linkType, String actorId,
                                                     int start, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<ModelBasicInfo> getBasicInfoFromLinks(List<CMDSContentLink> links,
                                                      List<ModelBasicInfo> basicInfos) throws VedantuException {

        if (CollectionUtils.isNotEmpty(links)) {
            for (Object listObject : links) {
                if (listObject instanceof CMDSContentLink) {
                    try {
                        logger.debug("Getting basic info for "
                                + ((CMDSContentLink) listObject).source.type + " id"
                                + ((CMDSContentLink) listObject).source.id);
                        ModelBasicInfo sourceBasicInfo = null;
                        ModelBasicInfo targetInfo = null;
                        VedantuBaseMongoModel sourceBaseMongoModel = null, targetBaseMongoModel = null;
                        if (((CMDSContentLink) listObject).source.type == EntityType.CMDSDOCUMENT) {
                            Optional<CMDSFolder> cmdsFolderOptional = cmdsFolderRepo.findById(((CMDSContentLink) listObject).source.id);
                            if (cmdsFolderOptional.isPresent()) {
                                sourceBaseMongoModel = cmdsFolderOptional.get();
                                sourceBasicInfo = cmdsFolderOptional.get().toBasicInfo();
                            }
                        }
                        if (((CMDSContentLink) listObject).target.type == EntityType.CMDSDOCUMENT) {
                            Optional<CMDSFolder> cmdsFolderOptional = cmdsFolderRepo.findById(((CMDSContentLink) listObject).target.id);
                            if (cmdsFolderOptional.isPresent()) {
                                targetBaseMongoModel = cmdsFolderOptional.get();
                                targetInfo = cmdsFolderOptional.get().toBasicInfo();
                            }
                        }
                        if (targetBaseMongoModel == null || sourceBaseMongoModel == null) {
                            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
                        }
                        if (sourceBasicInfo == null || targetInfo == null) {
                            logger.error("Database mismatch ");
                            continue;
                        }
                        CMDSContentLinkInfo linkInfo = (CMDSContentLinkInfo) ((CMDSContentLink) listObject)
                                .toBasicInfo();
                        linkInfo.setSourceTarget(sourceBasicInfo, targetInfo);
                        // Assigning downloadable state
                        if (((CMDSContentLink) listObject).source.type == EntityType.CMDSMODULE) {
                            Optional<CMDSModule> cmdsModuleOptional = cmdsModuleRepo.findById(((CMDSContentLink) listObject).source.id);
                            int totalContentCount = 0;
                            if (cmdsModuleOptional.isPresent()) {
                                totalContentCount = cmdsModuleOptional.get().totalContentCount;
                            }
                            if (((CMDSContentLink) listObject).getDownloadableEntities().size() == 0) {
                                linkInfo.downloadableState = "DISABLED";
                            } else if (((CMDSContentLink) listObject).getDownloadableEntities().size() < totalContentCount) {
                                linkInfo.downloadableState = "PARTIALLY_ENABLED";
                            } else if (((CMDSContentLink) listObject).getDownloadableEntities().size() >= totalContentCount) {
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
                        logger.debug("Found link info " + linkInfo);

                    } catch (VedantuException exception) {
                        logger.error("Decorating infos for links ", exception);
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

    public static Long getRemainingTime(Date date) {
        return date.getTime() - new Date().getTime();
    }

    public List<CMDSResourceDetails> getBasicInfoOFParaQuestion(List<String> qIds) {


        List<CMDSResourceDetails> resources = new ArrayList<CMDSResourceDetails>();

        if (qIds == null || qIds.size() == 0) {
            logger.error("empty search response for getting paragraph questions : ");
            return resources;
        }
        logger.debug("Ready to get Para question for ES resources");
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("content.id").in(qIds);
        // query.with(pageable)
        query.skip(0).limit(qIds.size());
	       /* SearchResponse response = ElasticSearchUtils.getSearchResponse(query,"timeCreated", "desc", 0, qIds.size(),
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
	        }*/


        return resources;

    }

    protected boolean delete(SrcEntity entity) throws VedantuException {
        VedantuBaseMongoModel model = null;
        logger.debug("Inside CMDSContentManager Delete function");
        if (entity.type == EntityType.CMDSQUESTION) {
            Optional<CMDSQuestion> cmdsQuestionOptional = cmdsQuestionRepo.findById(entity.id);
            if (cmdsQuestionOptional.isPresent()) {
                CMDSQuestion cmdsQuestion = cmdsQuestionOptional.get();
                if (cmdsQuestion.getRecordState() == VedantuRecordState.DELETED) {
                    throw new VedantuException(VedantuErrorCode.ALREADY_DELETED);
                }
                cmdsQuestion.setRecordState(VedantuRecordState.DELETED);
                cmdsQuestionRepo.save(cmdsQuestion);
                model = cmdsQuestion;
            }
        } else if (entity.type == EntityType.CMDSMODULE) {
            Optional<CMDSModule> cmdsModuleOptional = cmdsModuleRepo.findById(entity.id);
            if (cmdsModuleOptional.isPresent()) {
                CMDSModule cmdsModule = cmdsModuleOptional.get();
                if (cmdsModule.getRecordState() == VedantuRecordState.DELETED) {
                    throw new VedantuException(VedantuErrorCode.ALREADY_DELETED);
                }
                cmdsModule.setRecordState(VedantuRecordState.DELETED);
                cmdsModuleRepo.save(cmdsModule);
                model = cmdsModule;
            }
        }
        return model != null;
    }
}
