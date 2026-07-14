package com.lms.components;

import com.lms.common.ShareWithEntity;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.FileUtils;
import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.content.IIndexable;
import com.lms.common.vedantu.entity.storage.EntityFileStorageException;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.entity.storage.StatusFeedEntityFileStorage;
import com.lms.common.vedantu.entity.storage.StorageResult;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.ImageSize;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.enums.UserActionType.EventActionType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.NotificationReason;
import com.lms.enums.OrgMemberProfile;
import com.lms.enums.ShareType;
import com.lms.enums.SrcType.LinkType;
import com.lms.event.details.NewsRemoveDetails;
import com.lms.event.details.ShareEntityDetails;
import com.lms.managers.AbstractContentManager;
import com.lms.managers.NewsAggregatorHelper;
import com.lms.managers.news.NewsFeedSecurityVaildator;
import com.lms.managers.news.UserSecuritySet;
import com.lms.models.*;
import com.lms.pojo.OrgMemberBasicInfo;
import com.lms.pojos.Source;
import com.lms.pojos.news.NewsFeedInfo;
import com.lms.pojos.response.DeleteStatusFeedRes;
import com.lms.pojos.response.GetStatusFeedRes;
import com.lms.pojos.response.StatusFeedInfo;
import com.lms.repository.EntityShareRepo;
import com.lms.repository.OrgMemberRepo;
import com.lms.repository.OrganizationRepo;
import com.lms.repository.StatusFeedRepo;
import com.lms.requests.AddStatusFeedReq;
import com.lms.requests.DeleteStatusFeedReq;
import com.lms.requests.GetStatusFeedReq;
import com.lms.response.newsfeed.AddStatusFeedRes;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.pojo.UserInfo;
import com.lms.user.vedantu.user.repository.UserRepo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;

@Component
public class StatusFeedsComponent extends AbstractContentManager {
	public static final String STATUS_FEED_TEMP_DIR = "TEMP_DIR"
			+ File.separator + "statusfeed";
	private static final Logger logger = LoggerFactory.getLogger(StatusFeedsComponent.class);
	@Autowired
	private OrganizationRepo organizationRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private StatusFeedEntityFileStorage statusFeedEntityFileStorage;
	@Autowired
	private StatusFeedRepo statusFeedRepo;
	@Autowired
	private EntityShareRepo entityShareRepo;
	@Autowired
	private OrgMemberRepo orgMemberRepo;
	@Autowired
	private MongoTemplate mongoTemplate;

	public AddStatusFeedRes addStatusFeed(AddStatusFeedReq request) {
		Organization organization = organizationRepo.findByIdAndRecordState(request.orgId, VedantuRecordState.ACTIVE);

		if (!StringUtils.isEmpty(request.orgId) && organization == null) {
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
		}

		return addStatusFeed(request.userId, request.orgId, request.statusMessage, request.source, request.with);
	}

	public AddStatusFeedRes addStatusFeed(String userId, String orgId, String statusMessage, Source source,
										  List<ShareWithEntity> with) throws VedantuException {

		if (StringUtils.isEmpty(statusMessage) && source == null) {

			logger.debug(" No source no statusMessage provided " + source);

			throw new VedantuException(VedantuErrorCode.INVALID_STATUS_FEED);
		}
		Optional<User> u = userRepo.findById(userId);
		if (!u.isPresent()) {
			throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
		}

		logger.debug(" Source provided " + source);

		if (source != null) {

			File fileToBeUploaded = null;

			String uuid = UUID.randomUUID().toString();
			if (source.linkType == LinkType.UPLOADED) {
				fileToBeUploaded = ImageDisplayURLUtil.getEmbededFileName(STATUS_FEED_TEMP_DIR, source.url);
			} else if (source.linkType == LinkType.ADDED) {

				if (StringUtils.isEmpty(source.image)) {
					String imageName = uuid + FileUtils.JPG_EXTENTION;
					String imageTempOutputPath = getImageLocalTempPath(imageName);
					logger.info("downloading image to : " + imageTempOutputPath);
					fileToBeUploaded = null; //DownloadImageManager.downloadImage(source.image, imageTempOutputPath,
					//FileUtils.JPG_EXTENTION_WITHOUT_DOT);

				}
			}

			if (fileToBeUploaded == null && StringUtils.isEmpty(source.image)) {

				throw new VedantuException(VedantuErrorCode.INVALID_STATUS_FEED);
			}
			if (fileToBeUploaded != null) {
				StatusFeedEntityFileStorage storage = statusFeedEntityFileStorage;
				StorageResult uploadResult = null;
				try {

					Map<String, String> tags = new HashMap<String, String>();
					tags.put(ConstantsGlobal.USER_ID, userId);
					tags.put(ConstantsGlobal.ENTITY_TYPE, EntityType.STATUSFEED.name());
					tags.put(ConstantsGlobal.ORG_ID, orgId);

					uploadResult = storage.storeImage(uuid, fileToBeUploaded, FileCategory.CONVERTED,
							ImageSize.ORIGINAL, tags);
				} catch (EntityFileStorageException e) {
					logger.error("Could not upload file to OS fileName:" + fileToBeUploaded.getAbsolutePath());
					throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
				}

				logger.info("image " + fileToBeUploaded.getAbsolutePath() + " copied to object store : "
						+ uploadResult.isStored);

				if (uploadResult.isStored) {
					logger.info("adding image : " + uploadResult.fileId + " to statusFeed");
					source.image = uploadResult.uuid;

				}
				fileToBeUploaded.delete();
			}

			source.caption = source.caption != null ? source.caption : "";
		}

		StatusFeed statusFeed = new StatusFeed(userId, statusMessage);

		statusFeed.sourceContent = source;
		if (StringUtils.isEmpty(orgId)) {
			statusFeed.contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
		}
		statusFeedRepo.save(statusFeed);

		EntityShare entityShared = addShare(orgId, userId,
				new SrcEntity(EntityType.STATUSFEED, statusFeed._getStringId()), with, null, ShareType.SHARE);
		statusFeed.shares = entityShared.with.size();
		ShareEntityDetails details = new ShareEntityDetails();
		details.entity = entityShared.entity;
		details.userId = entityShared.userId;
		details.with = new HashSet<ShareWithEntity>();
		for (SrcEntity sectionLevelEntity : entityShared.with) {
			details.with.add(new ShareWithEntity(sectionLevelEntity.type, sectionLevelEntity.id));
		}

		details.type = entityShared.type;
		details.content = entityShared.content;
		details.userAction = (entityShared.type == ShareType.ASKED ? UserActionType.ASKED : UserActionType.SHARED);

		generateEventAysc(userId, details, EventType.SHARE_ENTITY);

		UserSecuritySet useSecuritySet = new UserSecuritySet(userId, orgId);
		NewsFeedSecurityVaildator.set(useSecuritySet);

		//
		NewsFeedInfo statusFeedInNewsFeed = new NewsFeedInfo(details.toNewsActivity());

		statusFeedInNewsFeed.why = NotificationReason.ACTOR;
		statusFeedInNewsFeed.newsActivityId = "";
		statusFeedInNewsFeed.newsFeedId = "";
		statusFeedInNewsFeed.time = statusFeed.timeCreated;
		statusFeedInNewsFeed.eType = EventType.SHARE_ENTITY;

		List<NewsFeedInfo> statusFeedList = new ArrayList<NewsFeedInfo>();
		statusFeedList.add(statusFeedInNewsFeed);
		statusFeedList = NewsAggregatorHelper.populateDetails(statusFeedList, userId);
		AddStatusFeedRes response = new AddStatusFeedRes();
		response.list.add(statusFeedList.get(0));
		return response;
	}

	private String getImageLocalTempPath(String fileName) {

       /* LocalFileSystemHandler tempLocHandler = (LocalFileSystemHandler) FileHandlerFactory
                .get(HandlerType.TEMP);
        return tempLocHandler.getDirectory() + LocalFileSystemHandler.PATH_SEPARATOR + fileName;*/
		return null;
	}

	public EntityShare addShare(String orgId, String userId, SrcEntity sharedEntity,
								List<ShareWithEntity> shareWith, String content, ShareType type)
			throws VedantuException {

        logger.info("Getting eligible share ");
        Set<SrcEntity> entities = getEligibleSharee(userId, orgId, sharedEntity, shareWith);


		EntityShare entityShare = new EntityShare(userId, sharedEntity, entities, content, type);

        entityShareRepo.save(entityShare);
        return entityShare;
    }
	private Set<SrcEntity> getEligibleSharee(String userId, String orgId, SrcEntity content,
            List<ShareWithEntity> shareWith) throws VedantuException {

		logger.debug("Getting eligible share" + shareWith);
        Set<String> shareeIds = new HashSet<String>();
        OrgMember orgMember = orgMemberRepo.findByOrgIdAndUserId(orgId, userId);
        logger.debug("Getting org share" + orgMember.email);
       
        Set<SrcEntity> sharedWithEntities = new HashSet<SrcEntity>();
        logger.info("Getting eligible share" + shareWith);
        for (ShareWithEntity shareWitEntityInDetail : shareWith) {
            if (shareWitEntityInDetail.type == EntityType.USER) {
                if (shareWitEntityInDetail.id.equals(userId)) {
                    throw new VedantuException(VedantuErrorCode.CAN_NOT_SHARE_WITH_SELF);
                }
                sharedWithEntities.add(shareWitEntityInDetail);
                shareeIds.add(shareWitEntityInDetail.id);
            } else if (shareWitEntityInDetail.type == EntityType.PROGRAM) {
                Set<String> centerIds = new HashSet<String>();

                if (CollectionUtils.isNotEmpty(shareWitEntityInDetail.centers)) {

                    for (SrcEntity centerEntity : shareWitEntityInDetail.centers) {
                        logger.info(" Sharing Requested centerIds" + centerEntity.id);
                        centerIds.add(centerEntity.id);
                    }
                }
                Query query = new Query();
                Criteria criteria = new Criteria();
                criteria.and(ConstantsGlobal.PROGRAM_ID).is(shareWitEntityInDetail.id);
                if (CollectionUtils.isNotEmpty(centerIds)) {
                    criteria.and(ConstantsGlobal.CENTER_ID).in(centerIds);
                                   }

                List<OrgSection> sections = getInfos(query,criteria);             
                for (OrgSection orgSection : sections) {
                    sharedWithEntities.add(new SrcEntity(EntityType.SECTION, orgSection
                            ._getStringId()));
                    shareeIds.add(orgSection._getStringId());

                }
            } else if (shareWitEntityInDetail.type == EntityType.SECTION) {
                sharedWithEntities
                        .add(new SrcEntity(EntityType.SECTION, shareWitEntityInDetail.id));
                shareeIds.add(shareWitEntityInDetail.id);

            } else if (shareWitEntityInDetail.type == EntityType.ORGANIZATION) {
                logger.debug("Posting news feed for organization " + shareWitEntityInDetail.id);
                Query query = new Query();
                Criteria criteria = new Criteria();
                criteria.and(ConstantsGlobal.ORG_ID).is(shareWitEntityInDetail.id);
                List<OrgSection> sections = getInfos(query,criteria);

                for (OrgSection orgSection : sections) {
                    sharedWithEntities.add(new SrcEntity(EntityType.SECTION, orgSection
                            ._getStringId()));
                    shareeIds.add(orgSection._getStringId());

                }

            }
        }
        
        if (checkAlreadySharedWith(shareeIds, content)) {
            return sharedWithEntities;
        }

        return null;
    }
    public List<OrgSection> getInfos(Query query,Criteria criteria) {
        query.addCriteria(criteria);
        List<OrgSection> result = mongoTemplate.find(query, OrgSection.class);
        return result;
    }

	private boolean checkAlreadySharedWith(Set<String> probableAlreadyShareeId, SrcEntity content)
			throws VedantuException {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("with.id").in(probableAlreadyShareeId);
		criteria.and("entity.type").is(content.type.name());
		criteria.and("entity.id").is(content.id);
		Long count = mongoTemplate.count(query, EntityShare.class);
		if (count > 0) {
			throw new VedantuException(VedantuErrorCode.ALREADY_SHARED);

		}
		return true;
	}

	public GetStatusFeedRes getStatusFeed(GetStatusFeedReq request) {
		GetStatusFeedRes response = new GetStatusFeedRes();
        UserSecuritySet securitySet = new UserSecuritySet(request.userId, request.orgId);

        SrcEntity statusFeedEntity = new SrcEntity(EntityType.STATUSFEED, request.feedId);
        if (securitySet.verifyShares(statusFeedEntity)) {
        	Optional<StatusFeed>  statusFeedOptional = statusFeedRepo.findById(request.feedId);
            StatusFeed  statusFeed = statusFeedOptional.get();
            StatusFeedInfo feedInfo = (StatusFeedInfo) statusFeed.toBasicInfo();
            Optional<User> userOptional =   userRepo.findById(statusFeed.userId);
            if(userOptional.isPresent()) {
            	User user = userOptional.get();
            	feedInfo.srcOwner = (UserInfo) user.toBasicInfo();
            }
            if (feedInfo.sourceContent != null && !StringUtils.isEmpty(feedInfo.sourceContent.image)) {
       		  logger.info("Source image" + feedInfo.sourceContent.image); feedInfo.sourceContent.image =
       		  getStatuFeedOrginalImageURL(feedInfo.sourceContent.image);
				if (statusFeed.sourceContent.linkType == LinkType.UPLOADED) {
					feedInfo.sourceContent.url = feedInfo.sourceContent.image;
				}

			}
        	//StatusFeedInfo feedInfo = StatusFeedDAO.INSTANCE.getBasicInfo(request.feedId);
            feedInfo.voted = getEntityUpvote(request.userId,
                    feedInfo.id);
            if (!StringUtils.isEmpty(request.orgId)) {

				UserInfo info = getUserInfo(request.orgId, feedInfo.srcOwner.id);
                info.id = info instanceof OrgMemberBasicInfo ? ((OrgMemberBasicInfo) info).userId
                        : info.id;
                feedInfo.srcOwner = info;
            }
            if (feedInfo.sourceContent != null && feedInfo.sourceContent.linkInfo != null) {

                feedInfo.sourceContent.linkInfo.populate();
            }
            response.info = feedInfo;

        }
        if (response.info == null) {
            throw new VedantuException(VedantuErrorCode.ACTIVITY_NOT_AVAILABLE);

        }
        return response;

	}
	public boolean getEntityUpvote(String userId, String id) {

        Set<String> entitySet = new HashSet<String>();
        entitySet.add(id);
        Map<String, Boolean> upVoteMap = getEntityUpVoteMap(userId, entitySet);
        if (upVoteMap.get(id) != null) {
            return upVoteMap.get(id).booleanValue();
        }
        return false;
    }
	public ModelBasicInfo toBasicInfo(User user) {

        return new UserInfo(user._getStringId(), user.firstName, user.lastName, _getThumbnailUrl(user),
        		user.recordState);
    }
	public String _getThumbnailUrl(User user) {

        Gender tGender = (null != user.gender && Gender.UNKNOWN != user.gender) ? user.gender : Gender.UNKNOWN;
        return !StringUtils.isEmpty(user.thumbnail) ? getEntityImageURLUtil(
                EntityType.USER, user.thumbnail,ImageSize.SMALL) : ImageDisplayURLUtil.getEntityStaticThumbnail(
                EntityType.USER, Collections.singletonList(tGender.name()));
    }

	public DeleteStatusFeedRes delete(DeleteStatusFeedReq request) {
		DeleteStatusFeedRes response = new DeleteStatusFeedRes();
        OrgMember deletionRequester = orgMemberRepo.findByOrgIdAndUserId(request.orgId,
                request.userId);
        String userId = getOwnerId(request.id);
        if (deletionRequester == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (userId == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_STATUS_FEED);
        }
        boolean deletionAllowed = false;
        if (deletionRequester.profile == OrgMemberProfile.STUDENT
                && deletionRequester.userId.equals(userId)) {
            deletionAllowed |= true;
        }

        if (deletionRequester.profile == OrgMemberProfile.TEACHER
                || deletionRequester.profile == OrgMemberProfile.MANAGER) {
            deletionAllowed |= true;
        }

        if (!deletionAllowed) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED);
        }

        SrcEntity content = new SrcEntity(EntityType.STATUSFEED, request.id);
        response.deleted = delete(request.userId, EventType.UNKNOWN, content);

        return response;

	}
    public String getOwnerId(String id) {

        if (ObjectIdUtils.hasInvalidId(id)) {
            return null;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("_id").is(id);
        query.addCriteria(criteria);
        query.fields().include(ConstantsGlobal.USER_ID);
        StatusFeed statusFeed = mongoTemplate.findOne(query, StatusFeed.class);
        if (statusFeed != null) {
            return statusFeed.userId;
        }
        return null;
    }

	protected boolean delete(final String userId, EventType indexEventType, SrcEntity content) throws VedantuException {
		Optional<StatusFeed> statusFeedOptional = statusFeedRepo.findById(content.id);

		if (!statusFeedOptional.isPresent()) {
			return false;
		}
		StatusFeed statusFeed = statusFeedOptional.get();
		statusFeed.recordState = VedantuRecordState.DELETED;
		statusFeedRepo.save(statusFeed);

		if (statusFeed instanceof IIndexable) {

			generateEventAysc(userId, statusFeed, EventActionType.REMOVE, indexEventType, UserActionType.DELETED,
					false);
		}

		NewsRemoveDetails newsRemoveDetails = new NewsRemoveDetails();
		newsRemoveDetails.content = content;
		generateEventAysc(userId, newsRemoveDetails, EventType.REMOVE_NEWS);
		return true;
	}

	public  String getStatuFeedOrginalImageURL(String uuid) {

		return getEntityImageURLUtil(EntityType.STATUSFEED, uuid, ImageSize.ORIGINAL);

	}
}
