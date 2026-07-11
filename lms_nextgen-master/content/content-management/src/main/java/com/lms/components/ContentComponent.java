package com.lms.components;

import com.lms.common.content.interfaces.IContentManager;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.DownloadableFileInfo;
import com.lms.common.utils.FileUtils;
import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.utils.MediaTypeMapper;
import com.lms.common.vedantu.commons.pojos.requests.FileData;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.entity.storage.*;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.UserRatingType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.SrcType.LinkType;
import com.lms.managers.ContentSecurityManager;
import com.lms.managers.DocumentManager;
import com.lms.models.*;
import com.lms.models.analytics.UserEntityRatings;
import com.lms.models.tests.Assignment;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.*;
import com.lms.repository.*;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.pojo.UserExtendedInfo;
import com.lms.user.vedantu.user.pojo.responce.GetUserSelfFullProfileRes;
import com.lms.user.vedantu.user.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ContentComponent {
    private static final Logger logger = LoggerFactory.getLogger(ContentComponent.class);
    @Autowired
    private LibraryContentLinksRepo libraryContentLinksRepo;
    @Autowired
    private DocumentsRepo documentsRepo;
    @Autowired
    private ContentSecurityManager csManager;
    @Autowired
    private MediaTypeMapper mediaTypeMapper;
    @Autowired
    private DocumentEntityFileStorage documentEntityFileStorage;
    @Autowired
    private UserEntityRatingsRepo userEntityRatingsRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private DocumentManager documentManager;
    @Autowired
    private VideoRepo videoRepo;
    @Autowired
    private TestRepo testRepo;
    @Autowired
    private AssignmentRepo assignmentRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private VideoEntityFileStorage videoEntityFileStorage;
    private OrganizationEntityFileStorage organizationEntityFileStorage;

    public GetContentDownloadLinkRes getContentDownloadLink(GetContentDownloadLinkReq req)
            throws VedantuException {

        GetContentDownloadLinkRes res = new GetContentDownloadLinkRes(false, null);
        Optional<LibraryContentLink> cLinkOptional = libraryContentLinksRepo.findById(req.linkId);
        if (!cLinkOptional.isPresent()) {
            logger.error("linkId: " + req.linkId + " is not visible anymore");
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_VISIBLE, "linkId: "
                    + req.linkId + " is not visible anymore");
        }
        LibraryContentLink cLink = cLinkOptional.get();
        if (!cLink.isDownloadable() && cLink.target != null
                && cLink.target.type != EntityType.MODULE) {
            logger.error("content not available for download for link[ " + cLink + "]");
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_VISIBLE,
                    "content not available for download for link[ " + cLink + "]");
        }
        VedantuBaseMongoModel model = null;
       if(req.entityType.equals(EntityType.DOCUMENT)) {
    	Optional<Documents> documents =   documentsRepo.findById(req.entityId);
    	if(documents.isPresent()) {
    		model = documents.get();
    	}
       }
        if (model != null) {
            if (model instanceof AbstractFileModel) {
                AbstractFileModel dModel = (AbstractFileModel) model;
                res.allowed = true;

                EncryptionLevel encLevel = csManager.getEncLevel(cLink, req.orgId);
                boolean encrypted = encLevel != null && encLevel != EncryptionLevel.NA;
                res.encLevel = encLevel != null ? encLevel.name() : null;
               // res.passphrase = csManager.getPassphrase(encLevel, cLink, req.userId, req.orgId);

                if (dModel.linkType == LinkType.UPLOADED) {
                    if (req.entityType == EntityType.DOCUMENT) {
                        res.url = getEntityDownloadURL(req.entityType,
                                dModel.uuid, FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                                (encrypted ? FileCategory.ENCRYPTED : FileCategory.CONVERTED));
                    } else if (req.entityType == EntityType.VIDEO) {
                        res.url = getEntityDownloadURL(req.entityType,
                                dModel.uuid, dModel.extension, mediaTypeMapper
                                        .getMediaType(dModel.extension),
                                (encrypted ? FileCategory.ENCRYPTED
                                        : ((dModel.converted) ? FileCategory.CONVERTED
                                                : FileCategory.ORIGINAL)));
                    } else if (req.entityType == EntityType.FILE) {
                        res.url = getEntityDownloadURL(req.entityType,
                                dModel.uuid, dModel.extension, MediaType.FILE,
                                (encrypted ? FileCategory.ENCRYPTED : FileCategory.ORIGINAL));
                    }
                }
            }
        }
        return res;

    }
	
	public String getEntityDownloadURL(EntityType entityType, String uid, String fileExt, MediaType mediaType,
			FileCategory fileCategory) {

		//IEntityFileStorage fileEntityStorage = documentEntityFileStorage;
		documentEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
		return (entityType == EntityType.VIDEO ? ImageDisplayURLUtil.DEFAULT_FILE_DOWNLOAD_HOST_URL : ImageDisplayURLUtil.DEFAULT_FILE_STREAMING_HOST_URL)
				+ documentEntityFileStorage.computeDisplayUrlComponent(uid, fileExt, mediaType, fileCategory, null);
	}

	public GetDownloadUrlOfPdfRes getPdfUrl(GetDownloadUrlOfPdfReq req) {
		GetDownloadUrlOfPdfRes res = new GetDownloadUrlOfPdfRes(null);
		DocumentsRepo documentsRepoRef = null;
        if(req.entityType.equals(EntityType.DOCUMENT)) {
        	documentsRepoRef = documentsRepo;
        }
       
        if (documentsRepoRef != null) {
        Optional<Documents> documents	 = documentsRepo.findById(req.entityId);
        if(documents.isPresent()) {
            VedantuBaseMongoModel model =documents.get() ;
            if (model instanceof AbstractFileModel) {
                AbstractFileModel dModel = (AbstractFileModel) model;
                    if (req.entityType == EntityType.DOCUMENT) {
                        res.url =getEntityDownloadURL(req.entityType,
                                dModel.uuid, FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                                (FileCategory.CONVERTED));
                    }
            }
        }
        }
        return res;
	}

	public DownloadableFileInfo getSecureLink(GetContentSecuredDownloadLinkReq req) {
		FileData fileData;
        try {
            IEntityFileStorage fileStorage = null;
            if (req.entityType == EntityType.DOCUMENT) {
                documentEntityFileStorage.AbstractEntityFileStorageEntity(req.entityType);
                fileStorage = documentEntityFileStorage;
            } else if (req.entityType == EntityType.VIDEO) {
                videoEntityFileStorage.AbstractEntityFileStorageEntity(req.entityType);
                fileStorage = videoEntityFileStorage;
            } else if (req.entityType == EntityType.ORGANIZATION) {
                organizationEntityFileStorage.AbstractEntityFileStorageEntity(req.entityType);
                fileStorage = organizationEntityFileStorage;
            }
            fileData = fileStorage.getSecureURL(req.entityType,
                    req.mediaType, req.fileName);
        } catch (Exception e) {
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND, "file not found");
        }
        if (fileData == null) {
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND, "file not found");
        }
        logger.debug("Getting secured URL");
        DownloadableFileInfo response = new DownloadableFileInfo();
        response.name = req.fileName;
        response.entityType = req.entityType;
        response.downloadUrl = URLEncoder.encode(fileData.getSecuredURL(), StandardCharsets.UTF_8);

        response.size = fileData.getContentLength();
        return response;
    }

	public GetEntityInfoForAppRes getEntityInfoForApp(GetEntityInfoForAppReq getInfoReq) {
		GetEntityInfoForAppRes res = new GetEntityInfoForAppRes();
        SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, getInfoReq.orgId);
        UserEntityRatings userRating = getUserRating(
                getInfoReq.userId, getInfoReq.entity, contentSrc);
       if(getInfoReq.entity.type == EntityType.VIDEO){
            Optional<Video> videoOptional = videoRepo.findById(getInfoReq.entity.id);
            if(videoOptional.isPresent()){
            	Video video = videoOptional.get();
                if (video.linkType == LinkType.UPLOADED) {
                    String[] fileName = (video.originalFileName).split("\\.");
                    String originalExtension = fileName[fileName.length - 1];
                    String extension = video.extension;
                    if(!extension.equals(originalExtension)){
                        res.showToggleSwitch = true;
                        res.webmUrl = getEntityDownloadURL(EntityType.VIDEO,
                                video.uuid, FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                                FileCategory.CONVERTED);
                        res.mp4Url = getEntityDownloadURL(EntityType.VIDEO,
                                video.uuid, FileUtils.MP4_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                                FileCategory.ORIGINAL);
                    }
                }
                if(userRating != null){
                    res.rating = userRating.rating;
                    res.feedback = userRating.feedback;
                }
            }else{
                throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_ID);
            }
        }else if(getInfoReq.entity.type == EntityType.TEST || getInfoReq.entity.type == EntityType.ASSIGNMENT || getInfoReq.entity.type == EntityType.DOCUMENT){
            if(userRating != null){
                res.rating = userRating.rating;
                res.feedback = userRating.feedback;
            }
        }
        else{
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }
        return res;

	}

	private UserEntityRatings getUserRating( String userId,
			 SrcEntity entity, SrcEntity contentSrc) {
		
		return userEntityRatingsRepo.findByUserIdAndSrcEntityAndContentSrc(userId,entity,contentSrc);
	}

	public boolean getEntity(GetEntityReq getEntityReq) {
		LibraryContentLink clink = 
                getBySourceAndSection(getEntityReq);
        if (clink == null) {
            List<LibraryContentLink> mlink = 
                    getListBySourceIdAndType(getEntityReq);
            for (LibraryContentLink link : mlink) {
                getEntityReq.entityId = link.target.id;
                getEntityReq.entityType = link.target.type;
                LibraryContentLink rlink = 
                        getBySourceAndSection(getEntityReq);
                if (rlink != null) {
                    return true;
                }
            }
            return false;
        }
        return true;
	}

	private List<LibraryContentLink> getListBySourceIdAndType(GetEntityReq req) {
		Criteria criteria = new Criteria();
        Query query1 = new Query();
        criteria.and("source.type").is(req.entityType);
        criteria.and("source.id").is(req.entityId);
        criteria.and("target.type").is(EntityType.MODULE);
        criteria.and("recordState").is(VedantuRecordState.ACTIVE);
        query1.addCriteria(criteria);
			return mongoTemplate.find(query1, LibraryContentLink.class); 
	}

	private LibraryContentLink getBySourceAndSection(GetEntityReq req) {

        Criteria criteria = new Criteria();
        Query query1 = new Query();
        criteria.and("source.type").is(req.entityType.name());
        criteria.and("source.id").is(req.entityId);
        criteria.and("target.id").is(req.sectionId);
        criteria.and("target.type").is(EntityType.SECTION.name());
        criteria.and("recordState").is(VedantuRecordState.ACTIVE.name());
        query1.addCriteria(criteria);
        return mongoTemplate.findOne(query1, LibraryContentLink.class);
    }

	public GetFileInfosRes getFileInfo(GetFileInfoReq request) {
		GetFileInfosRes response = new GetFileInfosRes();

        if (!CollectionUtils.isEmpty(request.contents)) {
            for (SrcEntity entity : request.contents) {
                logger.debug("Checking for content" + entity.type + "  " + entity.id);
                if( entity.type == null || entity.type == EntityType.UNKNOWN ){
                    continue;
                }
                IContentManager manager = null;
                if(entity.type.equals(EntityType.DOCUMENT)) {
                	manager = documentManager;
                }
                if (manager == null) {
                    logger.debug("Content manager not found for " + entity);
                    continue;
                }
                List<DownloadableFileInfo> infos = manager.getFiles(entity.type, entity.id);
                if (!CollectionUtils.isEmpty(infos)) {
                    response.list.addAll(infos);
                    response.totalHits = infos.size();
                }

            }
        }
        return response;
	}

	public GetEntityReviewsRes getEntityReviews(GetEntityReviewsReq getInfoReq) {
        GetEntityReviewsRes res = new GetEntityReviewsRes();
        SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, getInfoReq.orgId);

        List<GetEntityReviews> goodReviews = new ArrayList<GetEntityReviews>();
        List<GetEntityReviews> avgReviews = new ArrayList<GetEntityReviews>();
        List<GetEntityReviews> badReviews = new ArrayList<GetEntityReviews>();
        
        if(getInfoReq.entity.type == EntityType.CMDSVIDEO){
        	Video video =	getByCMDSVideoId(getInfoReq.entity.id);
            if(video!=null){
                getInfoReq.entity.type = EntityType.VIDEO;
                getInfoReq.entity.id = video._getStringId();
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSTEST){
            Test test = getByCMDSTestId(getInfoReq.entity.id);
            if(test != null){
                getInfoReq.entity.type = EntityType.TEST;
                getInfoReq.entity.id = test._getStringId();
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSASSIGNMENT) {
            Assignment assignment = getByCMDSAssignmentId(getInfoReq.entity.id);
            if(assignment != null){
                getInfoReq.entity.type = EntityType.ASSIGNMENT;
                getInfoReq.entity.id = assignment._getStringId();
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSDOCUMENT) {
            Documents doc = getByCMDSDocId(getInfoReq.entity.id);
            if(doc != null){
                getInfoReq.entity.type = EntityType.DOCUMENT;
                getInfoReq.entity.id = doc._getStringId();
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }
        else{
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }

        if(StringUtils.isEmpty(getInfoReq.ratingType)){
            getInfoReq.ratingType = "ALL";
        }

        
        
        if(getInfoReq.ratingType.equalsIgnoreCase("ALL") || getInfoReq.ratingType.equalsIgnoreCase("GOOD")){
        	Criteria criteria = new Criteria();
            Query query1 = new Query();
            criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_ID).is(contentSrc.id);
            criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_TYPE).is(contentSrc.type.name());
            criteria.and(ConstantsGlobal.SRC_ENTITY_DOT_ID).is(getInfoReq.entity.id);
            criteria.and(ConstantsGlobal.SRC_ENTITY_DOT_TYPE).is(getInfoReq.entity.type.name());
           
        	criteria.and("rating").is( UserRatingType.GOOD.name());
        	 query1.addCriteria(criteria);
            List<UserEntityRatings> goodRatings =getInfos(
            		query1, null, getInfoReq.start, getInfoReq.size
                    );
            res.totalGoodHits = goodRatings.size();
            for(UserEntityRatings userRating : goodRatings){
                GetEntityReviews ratings = new GetEntityReviews();
                ratings.userId = userRating.userId;
                GetUserSelfFullProfileRes resp = getUserFullProfile(userRating.userId);
                ratings.userName = resp.info.firstName+" "+resp.info.lastName;
                ratings.userProfilePic = resp.info.thumbnail;
                ratings.feedback = userRating.feedback;
                ratings.approved = userRating.approved;
                ratings.lastUpdated = userRating.lastUpdated;
                goodReviews.add(ratings);
                //ratings.clear();
            }
            res.goodReviews = goodReviews;
        }

        if(getInfoReq.ratingType.equals("ALL") || getInfoReq.ratingType.equalsIgnoreCase("BAD")){
        	Criteria criteria = new Criteria();
            Query query1 = new Query();
            criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_ID).is( contentSrc.id);
            criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_TYPE).is( contentSrc.type.name());
            criteria.and(ConstantsGlobal.SRC_ENTITY_DOT_ID).is( getInfoReq.entity.id);
            criteria.and(ConstantsGlobal.SRC_ENTITY_DOT_TYPE).is( getInfoReq.entity.type.name());
        	criteria.and("rating").is( UserRatingType.BAD.name());
            query1.addCriteria(criteria);
            List<UserEntityRatings> badRatings = getInfos(
            		query1, null, getInfoReq.start, getInfoReq.size
                   );
            res.totalBadHits = badRatings.size();
            for(UserEntityRatings userRating : badRatings){
                GetEntityReviews ratings = new GetEntityReviews();
                ratings.userId = userRating.userId;
                GetUserSelfFullProfileRes resp = getUserFullProfile(userRating.userId);
                ratings.userName = resp.info.firstName+" "+resp.info.lastName;
                ratings.userProfilePic = resp.info.thumbnail;
                ratings.feedback = userRating.feedback;
                ratings.approved = userRating.approved;
                ratings.lastUpdated = userRating.lastUpdated;
                badReviews.add(ratings);
                //ratings.clear();
            }
            res.badReviews = badReviews;
        }

        if(getInfoReq.ratingType.equals("ALL") || getInfoReq.ratingType.equalsIgnoreCase("AVERAGE")){
        	Criteria criteria = new Criteria();
            Query query1 = new Query();
            criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_ID).is( contentSrc.id);
            criteria.and(ConstantsGlobal.CONTENT_SRC_DOT_TYPE).is( contentSrc.type.name());
            criteria.and(ConstantsGlobal.SRC_ENTITY_DOT_ID).is( getInfoReq.entity.id);
            criteria.and(ConstantsGlobal.SRC_ENTITY_DOT_TYPE).is( getInfoReq.entity.type.name());
        	criteria.and("rating").is( UserRatingType.AVERAGE.name());
        	query1.addCriteria(criteria);
            List<UserEntityRatings> avgRatings = getInfos(
            		query1, null, getInfoReq.start, getInfoReq.size
                    );
            res.totalAvgHits = avgRatings.size();
            for(UserEntityRatings userRating : avgRatings){
                GetEntityReviews ratings = new GetEntityReviews();
                ratings.userId = userRating.userId;
                GetUserSelfFullProfileRes resp = getUserFullProfile(userRating.userId);
                ratings.userName = resp.info.firstName+" "+resp.info.lastName;
                ratings.userProfilePic = resp.info.thumbnail;
                ratings.feedback = userRating.feedback;
                ratings.approved = userRating.approved;
                ratings.lastUpdated = userRating.lastUpdated;
                avgReviews.add(ratings);
                //ratings.clear();
            }
            res.averageReviews = avgReviews;
        }
        return res;
	}

	private List<UserEntityRatings> getInfos(Query query1, Object object,  int start, int size) {
		//query1.limit(size);
		//query1.skip(start);
		return mongoTemplate.find(query1, UserEntityRatings.class); 
	}

	private Documents getByCMDSDocId(String id) {
		return documentsRepo.findByCmdsDocId(id);
	}

	private Assignment getByCMDSAssignmentId(String id) {
		return assignmentRepo.findByCmdsId(id);
	}

	private Test getByCMDSTestId(String id) {
		return testRepo.findByCmdsTestId(id);
	}

	private Video getByCMDSVideoId(String id) {
		
		return videoRepo.findByCmdsVideoId(id);
	}
	public  GetUserSelfFullProfileRes getUserFullProfile(
            String userId) throws VedantuException {

        logger.debug("getUserSelfFullProfile userId: " + userId);

        UserExtendedInfo userExtendedInfo = 
                getExtendedInfo(userId);
        if (null == userExtendedInfo) {
        	logger.error("getUserSelfFullProfile user not found for userId: "
                    + userId);
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }

        GetUserSelfFullProfileRes getUserSelfFullProfileRes = new GetUserSelfFullProfileRes();
        getUserSelfFullProfileRes.info = userExtendedInfo;

        logger.info("getUserSelfFullProfile userExtendedInfo: " + userExtendedInfo);

        return getUserSelfFullProfileRes;
    }
	 private UserExtendedInfo getExtendedInfo(String userId) {
	        Optional<User> user = userRepo.findById(userId);
	        if (!user.isPresent())
	            return null;
	        UserExtendedInfo extendedInfo = new UserExtendedInfo(user.get());
	        return extendedInfo;
	    }

	public GetEntityInfoForAppRes addRatingAndFeedback(AddEntityInfoReq getEntityReq) {
		SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, getEntityReq.orgId);
        UserEntityRatings userRating = getUserRating(
                getEntityReq.userId, getEntityReq.entity, contentSrc);
        if (userRating == null) {
            // User is rating first time
            logger.debug("Recording User Rating");
            if (getEntityReq.entity.type == EntityType.VIDEO) {
                // Increment count of ratings
                Video video	 = getVideoById(getEntityReq.entity.id);
                if(getEntityReq.rating == UserRatingType.AVERAGE){
                    video.average++;
                }else if(getEntityReq.rating == UserRatingType.GOOD){
                    video.good++;
                }else if(getEntityReq.rating == UserRatingType.BAD){
                    video.bad++;
                }
                videoRepo.save(video);
                
            } else if (getEntityReq.entity.type == EntityType.TEST) {
                // Increment count of ratings
                Test test = getTestById(getEntityReq.entity.id);	
                if(getEntityReq.rating == UserRatingType.AVERAGE){
                    test.average++;
                }else if(getEntityReq.rating == UserRatingType.GOOD){
                    test.good++;
                }else if(getEntityReq.rating == UserRatingType.BAD){
                    test.bad++;
                }
                testRepo.save(test);
                
            } else if (getEntityReq.entity.type == EntityType.DOCUMENT) {
                // Increment count of ratings
                Documents doc = getDocumentById(getEntityReq.entity.id);
                if(getEntityReq.rating == UserRatingType.AVERAGE){
                    doc.average++;
                }else if(getEntityReq.rating == UserRatingType.GOOD){
                    doc.good++;
                }else if(getEntityReq.rating == UserRatingType.BAD){
                    doc.bad++;
                }
                documentsRepo.save(doc);
               
            } else if (getEntityReq.entity.type == EntityType.ASSIGNMENT) {
                // Increment count of ratings        
                Assignment assignment = getAssignMentById(getEntityReq.entity.id);
                if(getEntityReq.rating == UserRatingType.AVERAGE){
                    assignment.average++;
                }else if(getEntityReq.rating == UserRatingType.GOOD){
                    assignment.good++;
                }else if(getEntityReq.rating == UserRatingType.BAD){
                    assignment.bad++;
                }
                assignmentRepo.save(assignment);
               
            } else{
                throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
            }
            addUserRatingAndReview(getEntityReq.userId,
                    getEntityReq.entity, contentSrc, getEntityReq.rating, getEntityReq.feedback);
        }else {
            // User has rated already
            logger.debug("Updating User Rating");
            if (userRating.rating == getEntityReq.rating) {
                // Update only feedback
                if (getEntityReq.entity.type == EntityType.VIDEO
                        || getEntityReq.entity.type == EntityType.TEST
                        || getEntityReq.entity.type == EntityType.DOCUMENT
						|| getEntityReq.entity.type == EntityType.ASSIGNMENT) {
					logger.debug("Updating Only Feedback");
                    userRating.feedback = getEntityReq.feedback;
                    userEntityRatingsRepo.save(userRating);
                } else {
                    throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
                }
			} else {
				logger.debug("Updating Rating and Review");
                UserRatingType previousRating = userRating.rating;
                // Update rating and feedback
				if (getEntityReq.entity.type == EntityType.VIDEO) {
					// Increment count of ratings
					logger.debug("Updating for entity "
							+ getEntityReq.entity.type);
                    Video video = getVideoById(getEntityReq.entity.id);
                    if(getEntityReq.rating == UserRatingType.AVERAGE){
                        ++video.average;
                    }else if(getEntityReq.rating == UserRatingType.GOOD){
                        ++video.good;
                    }else if(getEntityReq.rating == UserRatingType.BAD){
                        ++video.bad;
                    }

                    // Decrement previous rating
                    if(previousRating == UserRatingType.AVERAGE){
                        video.average = video.average == 0 ? video.average : --video.average;
                    }else if(previousRating == UserRatingType.GOOD){
                        video.good = video.good == 0 ? video.good : --video.good;
                    }else if(previousRating == UserRatingType.BAD){
                        video.bad = video.bad == 0 ? video.bad : --video.bad;
                    }
                    videoRepo.save(video);
                } else if (getEntityReq.entity.type == EntityType.TEST) {
                    // Increment count of ratings
                    Test test = getTestById(getEntityReq.entity.id);
                    if(getEntityReq.rating == UserRatingType.AVERAGE){
                        ++test.average;
                    }else if(getEntityReq.rating == UserRatingType.GOOD){
                        ++test.good;
                    }else if(getEntityReq.rating == UserRatingType.BAD){
                        ++test.bad;
                    }

                    // Decrement previous rating
                    if(previousRating == UserRatingType.AVERAGE){
                        test.average = test.average == 0 ? test.average : --test.average;
                    }else if(previousRating == UserRatingType.GOOD){
                        test.good = test.good == 0 ? test.good : --test.good;
                    }else if(previousRating == UserRatingType.BAD){
                        test.bad = test.bad == 0 ? test.bad : --test.bad;
                    }
                    testRepo.save(test);
                } else if (getEntityReq.entity.type == EntityType.DOCUMENT) {
                    // Increment count of ratings
                    Documents doc = getDocumentById(getEntityReq.entity.id);
                    if(getEntityReq.rating == UserRatingType.AVERAGE){
                        ++doc.average;
                    }else if(getEntityReq.rating == UserRatingType.GOOD){
                        ++doc.good;
                    }else if(getEntityReq.rating == UserRatingType.BAD){
                        ++doc.bad;
                    }

                    // Decrement previous rating
                    if(previousRating == UserRatingType.AVERAGE){
                        doc.average = doc.average == 0 ? doc.average : --doc.average;
                    }else if(previousRating == UserRatingType.GOOD){
                        doc.good = doc.good == 0 ? doc.good : --doc.good;
                    }else if(previousRating == UserRatingType.BAD){
                        doc.bad = doc.bad == 0 ? doc.bad : --doc.bad;
                    }
                    documentsRepo.save(doc);
                } else if (getEntityReq.entity.type == EntityType.ASSIGNMENT) {
                    // Increment count of ratings
                    Assignment assignment = getAssignMentById(getEntityReq.entity.id);
                    if(getEntityReq.rating == UserRatingType.AVERAGE){
                        ++assignment.average;
                    }else if(getEntityReq.rating == UserRatingType.GOOD){
                        ++assignment.good;
                    }else if(getEntityReq.rating == UserRatingType.BAD){
                        ++assignment.bad;
                    }

                    // Decrement previous rating
                    if(previousRating == UserRatingType.AVERAGE){
                        assignment.average = assignment.average == 0 ? assignment.average : --assignment.average;
                    }else if(previousRating == UserRatingType.GOOD){
                        assignment.good = assignment.good == 0 ? assignment.good : --assignment.good;
					} else if (previousRating == UserRatingType.BAD) {
						assignment.bad = assignment.bad == 0 ? assignment.bad
								: --assignment.bad;
                    }
                    assignmentRepo.save(assignment);
                } else {
                    throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
                }
                userRating.rating = getEntityReq.rating;
                userRating.feedback = getEntityReq.feedback;
                userEntityRatingsRepo.save(userRating);
            }
        }
        return getEntityInfoForApp(getEntityReq);
    }
	
	private Assignment getAssignMentById(String id) {
        Optional<Assignment> assignmentOptional = assignmentRepo.findById(id);
        if(assignmentOptional.isPresent()) {
        	return assignmentOptional.get();
        }
		return null;
	}

	private Documents getDocumentById(String id) {
        Optional<Documents> docOptional = documentsRepo.findById(id);
       if(docOptional.isPresent()) {
    	   return docOptional.get();
       }
		return null;
	}

	private Test getTestById(String id) {
        Optional<Test> testOptional = testRepo.findById(id);
        if(testOptional.isPresent()) {
         return	testOptional.get();
        }
		return null;
	}

	private Video getVideoById(String id) {
        Optional<Video> videoOptional = videoRepo.findById(id);
        if(videoOptional.isPresent()) {
        	return videoOptional.get();
        }
		return null;
	}

	public void addUserRatingAndReview(String userId, SrcEntity srcEntity, SrcEntity contentSrc,
            UserRatingType rating, String feedback) {
        UserEntityRatings userRating = new UserEntityRatings(userId, srcEntity, contentSrc, rating,
                feedback);
        userEntityRatingsRepo.save(userRating);
    }

	public GetCMDSEntityInfoRes getCMDSEntityInfo(GetEntityInfoForAppReq getInfoReq) {
        GetCMDSEntityInfoRes res = new GetCMDSEntityInfoRes();
        SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, getInfoReq.orgId);
        if(getInfoReq.entity.type == EntityType.CMDSVIDEO){
            Video video = getByCMDSVideoId(getInfoReq.entity.id);
            if(video != null){
                if (video.linkType == LinkType.UPLOADED) {
                    String[] fileName = (video.originalFileName).split("\\.");
                    String originalExtension = fileName[fileName.length - 1];
                    String extension = video.extension;
                    if(!extension.equals(originalExtension)){
                        res.showToggleSwitch = true;
                        res.webmUrl = getEntityDownloadURL(EntityType.VIDEO,
                                video.uuid, FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                                FileCategory.CONVERTED);
                        res.mp4Url = getEntityDownloadURL(EntityType.VIDEO,
                                video.uuid, FileUtils.MP4_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                                FileCategory.ORIGINAL);
                    }
                }
                res.badRatingCount = video.bad;
                res.goodRatingCount = video.good;
                res.avgRatingCount = video.average;
                getInfoReq.entity.type = EntityType.VIDEO;
                getInfoReq.entity.id = video._getStringId();
                res.reviewCount = getReviewCount(getInfoReq.entity,contentSrc);
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSTEST){
            Test test = getByCMDSTestId(getInfoReq.entity.id);
            if(test != null){
                res.badRatingCount = test.bad;
                res.goodRatingCount = test.good;
                res.avgRatingCount = test.average;
                getInfoReq.entity.type = EntityType.TEST;
                getInfoReq.entity.id = test._getStringId();
                res.reviewCount = getReviewCount(getInfoReq.entity,contentSrc);
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSASSIGNMENT) {
            Assignment assignment = getByCMDSAssignmentId(getInfoReq.entity.id);
            if(assignment != null){
                res.badRatingCount = assignment.bad;
                res.goodRatingCount = assignment.good;
                res.avgRatingCount = assignment.average;
                getInfoReq.entity.type = EntityType.ASSIGNMENT;
                getInfoReq.entity.id = assignment._getStringId();
                res.reviewCount = getReviewCount(getInfoReq.entity,contentSrc);
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSDOCUMENT) {
            Documents doc = getByCMDSDocId(getInfoReq.entity.id);
            if(doc != null){
                res.badRatingCount = doc.bad;
                res.goodRatingCount = doc.good;
                res.avgRatingCount = doc.average;
                getInfoReq.entity.type = EntityType.DOCUMENT;
                getInfoReq.entity.id = doc._getStringId();
                res.reviewCount = getReviewCount(getInfoReq.entity,contentSrc);
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }
        else{
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }
        return res;
	}

	private long getReviewCount(SrcEntity srcEntity, SrcEntity contentSrc) {
		
        Criteria criteria = new Criteria();
        Query query = new Query();
        criteria.and(ConstantsGlobal.SRC_ENTITY).is(srcEntity);
        criteria.and(ConstantsGlobal.CONTENT_SRC).is(contentSrc);
        criteria.and("feedback").exists(true);
        query.addCriteria(criteria);
		return mongoTemplate.count(query, UserEntityRatings.class);
	}
}
