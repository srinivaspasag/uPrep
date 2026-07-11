package com.vedantu.content.managers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

import com.vedantu.content.daos.ModuleDAO;
import com.vedantu.content.models.*;
import com.vedantu.content.models.analytics.UserEntityAttempt;
import com.vedantu.content.models.analytics.UserEntityRatings;
import com.vedantu.content.models.analytics.UserQuestionAttempt;
import com.vedantu.content.models.tests.Assignment;
import com.vedantu.content.models.tests.Test;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Required;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.IContentManager;
import com.vedantu.commons.entity.factory.EntityTypeContentManagerFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.*;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.events.apis.EntityIndexEventMapper;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.pojos.DownloadableFileInfo;
import com.vedantu.commons.pojos.EntityDetails;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.SubjectMetadata;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.commons.relationships.ContentLinkRelationshipDetails;
import com.vedantu.commons.utils.*;
import com.vedantu.commons.utils.image.ImageHTMLUtils;
import com.vedantu.content.commons.interfaces.ILibraryContent;
import com.vedantu.content.daos.AnswerDAO;
import com.vedantu.content.daos.AssignmentDAO;
import com.vedantu.content.daos.DocumentDAO;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.daos.ScheduleDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.daos.UserEntityRatingsDAO;
import com.vedantu.content.daos.VideoDAO;
import com.vedantu.content.daos.analytics.UserEntityAttemptDAO;
import com.vedantu.content.daos.analytics.UserQuestionAttemptDAO;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.pojos.ModuleEntryInfo;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.requests.*;
import com.vedantu.content.pojos.requests.file.GetFileInfoReq;
import com.vedantu.content.pojos.requests.questions.GetSolutionsReq;
import com.vedantu.content.pojos.requests.schedules.AddScheduleReq;
import com.vedantu.content.pojos.requests.schedules.GetScheduleReq;
import com.vedantu.content.pojos.requests.schedules.RemoveScheduleReq;
import com.vedantu.content.pojos.requests.tests.GetAssignmentInfoReq;
import com.vedantu.content.pojos.responses.*;
import com.vedantu.content.pojos.responses.questions.GetSolutionsRes;
import com.vedantu.content.pojos.responses.schedule.DayMetadata;
import com.vedantu.content.pojos.responses.schedule.EntityCount;
import com.vedantu.content.pojos.responses.schedule.EntityList;
import com.vedantu.content.pojos.responses.schedule.EntityTypeData;
import com.vedantu.content.pojos.responses.schedule.GetDayScheduleRes;
import com.vedantu.content.pojos.responses.schedule.GetScheduleRes;
import com.vedantu.content.pojos.responses.schedule.SaveScheduleRes;
import com.vedantu.content.pojos.responses.schedule.ScheduleDay;
import com.vedantu.content.pojos.responses.schedule.SubMetadata;
import com.vedantu.content.pojos.responses.tests.GetAssignmentInfoRes;
import com.vedantu.content.pojos.tests.SimplifiedBoardNames;
import com.vedantu.content.search.details.*;
import com.vedantu.events.utils.EventDetailsFactory;
import com.vedantu.mongo.*;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.daos.device.mgmt.ActivityRecordDAO;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.models.device.mgmt.ActivityRecord;
import com.vedantu.search.es.ElasticSearchManager;
import com.vedantu.search.utils.ElasticSearchUtils;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.managers.UserManager;
import com.vedantu.user.pojos.responses.GetUserSelfFullProfileRes;

public class ContentManager extends AbstractContentManager {

    private static final Map<EntityType, Class<?>> entitySearchIndexDetailsMap;
    static {
        entitySearchIndexDetailsMap = new HashMap<EntityType, Class<?>>();
        entitySearchIndexDetailsMap.put(EntityType.VIDEO, VideoSearchIndexDetails.class);
        entitySearchIndexDetailsMap.put(EntityType.DOCUMENT, DocumentSearchIndexDetails.class);
        entitySearchIndexDetailsMap.put(EntityType.TEST, TestSearchIndexDetails.class);
        entitySearchIndexDetailsMap.put(EntityType.ASSIGNMENT, AssignmentSearchIndexDetails.class);
        entitySearchIndexDetailsMap.put(EntityType.FILE, FileSearchIndexDetails.class);
        entitySearchIndexDetailsMap.put(EntityType.QUESTION, QuestionSearchIndexDetails.class);
    }

    private static final int                       REQ_SIZE = 20;
    private static final ALogger                   LOGGER   = Logger.of(ContentManager.class);

    public static DownloadableFileInfo getSecureLink(GetContentSecuredDownloadLinkReq req)
            throws VedantuException {

        FileData fileData;
        try {

            fileData = FileSystemFactory.INSTANCE.getFS().getSecureURL(req.entityType,
                    req.mediaType, req.fileName);
        } catch (FileStoreException e) {
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND, "file not found");
        }
        LOGGER.debug("Getting secured URL");
        DownloadableFileInfo response = new DownloadableFileInfo();
        response.name = req.fileName;
        response.entityType = req.entityType;
        try {
            response.downloadUrl = URLEncoder.encode(fileData.getSecuredURL(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
           throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
        }

        response.size = fileData.getContentLength();
        return response;
    }

    public static GetContentDownloadLinkRes getContentDownloadLink(GetContentDownloadLinkReq req)
            throws VedantuException {

        GetContentDownloadLinkRes res = new GetContentDownloadLinkRes(false, null);
        LibraryContentLink cLink = LibraryContentLinksDAO.INSTANCE.getById(req.linkId);
        if (cLink == null) {
            LOGGER.error("linkId: " + req.linkId + " is not visible anymore");
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_VISIBLE, "linkId: "
                    + req.linkId + " is not visible anymore");
        }
        if (!cLink.isDownloadable() && cLink.target != null
                && cLink.target.type != EntityType.MODULE) {
            LOGGER.error("content not available for download for link[ " + cLink + "]");
            throw new VedantuException(VedantuErrorCode.CONTENT_NOT_VISIBLE,
                    "content not available for download for link[ " + cLink + "]");
        }

        VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE.get(req.entityType);
        if (dao != null) {
            VedantuBaseMongoModel model = dao.getById(req.entityId);
            if (model instanceof AbstractFileModel) {
                AbstractFileModel dModel = (AbstractFileModel) model;
                res.allowed = true;

                ContentSecurityManager csManager = new ContentSecurityManager();
                EncryptionLevel encLevel = csManager.getEncLevel(cLink, req.orgId);
                boolean encrypted = encLevel != null && encLevel != EncryptionLevel.NA;
                res.encLevel = encLevel != null ? encLevel.name() : null;
                res.passphrase = csManager.getPassphrase(encLevel, cLink, req.userId, req.orgId);

                if (dModel.linkType == LinkType.UPLOADED) {
                    if (req.entityType == EntityType.DOCUMENT) {
                        res.url = ImageDisplayURLUtil.getEntityDownloadURL(req.entityType,
                                dModel.uuid, FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                                (encrypted ? FileCategory.ENCRYPTED : FileCategory.CONVERTED));
                    } else if (req.entityType == EntityType.VIDEO) {
                        res.url = ImageDisplayURLUtil.getEntityDownloadURL(req.entityType,
                                dModel.uuid, dModel.extension, MediaTypeMapper.INSTANCE()
                                        .getMediaType(dModel.extension),
                                (encrypted ? FileCategory.ENCRYPTED
                                        : ((dModel.converted) ? FileCategory.CONVERTED
                                                : FileCategory.ORIGINAL)));
                    } else if (req.entityType == EntityType.FILE) {
                        res.url = ImageDisplayURLUtil.getEntityDownloadURL(req.entityType,
                                dModel.uuid, dModel.extension, MediaType.FILE,
                                (encrypted ? FileCategory.ENCRYPTED : FileCategory.ORIGINAL));
                    }
                }
            }
        }
        return res;

    }

    public static GetDownloadUrlOfPdfRes getPdfUrl(GetDownloadUrlOfPdfReq req)
            throws VedantuException {

        GetDownloadUrlOfPdfRes res = new GetDownloadUrlOfPdfRes(null);

        VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE.get(req.entityType);
        if (dao != null) {
            VedantuBaseMongoModel model = dao.getById(req.entityId);
            if (model instanceof AbstractFileModel) {
                AbstractFileModel dModel = (AbstractFileModel) model;
                    if (req.entityType == EntityType.DOCUMENT) {
                        res.url = ImageDisplayURLUtil.getEntityDownloadURL(req.entityType,
                                dModel.uuid, FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                                (FileCategory.CONVERTED));
                    }
            }
        }
        return res;
    }

    public static GetContentLinksRes getContentLinks(GetContentsLinkReq req)
            throws VedantuException {

        return getContentLinks(req, VedantuRecordState.ACTIVE);
    }

    public static GetContentStateRes checkWhetherProgramIsCompleted(GetContentStateReq getContentStateReq)
    		 throws VedantuException{
    	
    	
    	GetContentStateRes getContentStateRes=new GetContentStateRes();
    	
    	boolean checkWhetherProgramIsCompleted=true;
    	
    	long timeCreated= 0L;
    	
    	LOGGER.info("inside contentmanager : ");
    	Organization org=OrganizationDAO.INSTANCE.getById(getContentStateReq.orgId);
    	List<LibraryContentLink> libraryContentLinks=null;
    	if(org.digitalLibraryHiddenFields==null || org.digitalLibraryHiddenFields.size()==0){
    		Set<String> digitalLibraryHiddenFields=new HashSet<String>();
        	libraryContentLinks=
        			LibraryContentLinksDAO.INSTANCE.getLibraryContentLinksOfAProgram(getContentStateReq.sectionId,digitalLibraryHiddenFields);
    	}else{
        	libraryContentLinks=
        			LibraryContentLinksDAO.INSTANCE.getLibraryContentLinksOfAProgram(getContentStateReq.sectionId,
        					org.digitalLibraryHiddenFields);
    	}
    	    	
    	if(libraryContentLinks==null ||  libraryContentLinks.size()==0){
    		checkWhetherProgramIsCompleted=false;

    	}
    	outer : for(LibraryContentLink libraryContentLink:libraryContentLinks){
    		LOGGER.info("libraryContentLink  source type : "+libraryContentLink.source.type);
    		if(libraryContentLink.source.type.equals(EntityType.TEST)){
    			Test test=TestDAO.INSTANCE.getById(libraryContentLink.source.id);
    			if(test==null){
    	            throw new VedantuException(VedantuErrorCode.TEST_NOT_FOUND," with  this  id : "+libraryContentLink.source.id);
    			}else{
        			LOGGER.info("test : "+test.name);
    				if(test.boardIds!=null && getContentStateReq.brdIds!=null){
        				for(String brdId:getContentStateReq.brdIds){
        					if(test.boardIds.contains(brdId)){
        						if(ActivityRecordDAO.INSTANCE.activityFound(getContentStateReq.userId,
    	    							getContentStateReq.orgId, EntityType.TEST,"ATTEMPTED",test.id.toString())==false){
        							checkWhetherProgramIsCompleted=false;
        							break outer;
        						}
        						else{
        							long time=ActivityRecordDAO.INSTANCE.getActivities(getContentStateReq.userId,
        	    							getContentStateReq.orgId, EntityType.TEST,"ATTEMPTED",test.id.toString()).get(0).timeCreated;
        							timeCreated=Math.max(timeCreated,time);
        						}
        					}
        				}
        			}else if(getContentStateReq.brdIds!=null && test.boardIds==null){
        				throw new VedantuException(VedantuErrorCode.BOARD_MAPPING_NOT_FOUND," test has no boards");
        			}else if(getContentStateReq.brdIds==null){
        				if(ActivityRecordDAO.INSTANCE.activityFound(getContentStateReq.userId,
    							getContentStateReq.orgId, EntityType.TEST,"ATTEMPTED",test.id.toString())==false){
							checkWhetherProgramIsCompleted=false;
							break outer;
						}
						else{
							long time=ActivityRecordDAO.INSTANCE.getActivities(getContentStateReq.userId,
	    							getContentStateReq.orgId, EntityType.TEST,"ATTEMPTED",test.id.toString()).get(0).timeCreated;
							timeCreated=Math.max(timeCreated,time);
						}
        			}
    			}    			
    		}else if(libraryContentLink.source.type.equals(EntityType.MODULE)){
    			Module module= ModuleDAO.INSTANCE.getById(libraryContentLink.source.id);
    			if(module==null){
    	            throw new VedantuException(VedantuErrorCode.MODULE_NOT_FOUND," with  this  id : "+libraryContentLink.source.id);
    			}else{
        			LOGGER.info("module info : "+module.name);    
					GetModuleReq getModuleReq=new GetModuleReq();
	    			getModuleReq.id=libraryContentLink.source.id;
	    			getModuleReq.orgId=getContentStateReq.orgId;
	    			getModuleReq.userId=getContentStateReq.userId;
	    			GetModuleRes getModuleRes=ModuleManager.INSTANCE.getModule(getModuleReq);
	    			LOGGER.info("module size "+getModuleRes.moduleEntryInfos.size());
	    			List<ModuleEntryInfo> moduleEntryInfos=getModuleRes.moduleEntryInfos;
    				if(module.boardIds!=null && getContentStateReq.brdIds!=null){
        				for(String brdId:getContentStateReq.brdIds){
        					if(module.boardIds.contains(brdId)){
        		    			for(ModuleEntryInfo moduleEntryInfo:moduleEntryInfos){
        		    				if(moduleEntryInfo.completed==false && moduleEntryInfo.attempted == false){
        		    					checkWhetherProgramIsCompleted=false;
        		    					break outer;
        		    				}
        		    				else{
        								long time=ActivityRecordDAO.INSTANCE.getActivities(getContentStateReq.userId,
        		    							getContentStateReq.orgId, EntityType.MODULE,"VIEW",module.id.toString()).get(0).timeCreated;
        								timeCreated=Math.max(timeCreated,time);
        		    				}
        		    			}
        					}
        				}
        			}else if(getContentStateReq.brdIds!=null && module.boardIds==null){
        				throw new VedantuException(VedantuErrorCode.BOARD_MAPPING_NOT_FOUND," module has no boards");
        			}else if(getContentStateReq.brdIds==null){
            			for(ModuleEntryInfo moduleEntryInfo:moduleEntryInfos){
            				LOGGER.info("inside module completed: "+moduleEntryInfo.completed);
            				LOGGER.info("inside module attempted : "+moduleEntryInfo.attempted);
            				LOGGER.info("inside module entity : "+moduleEntryInfo.entity);
            				if(moduleEntryInfo.completed==false && moduleEntryInfo.attempted == false&& moduleEntryInfo.entity !=null ){
            					checkWhetherProgramIsCompleted=false;
            					break outer;
            				}
		    				else{
								long time=ActivityRecordDAO.INSTANCE.getActivities(getContentStateReq.userId,
		    							getContentStateReq.orgId, EntityType.MODULE,"VIEW",module.id.toString()).get(0).timeCreated;
								timeCreated=Math.max(timeCreated,time);
		    				}
            			}
        			}
    			}
    		}else if(libraryContentLink.source.type.equals(EntityType.ASSIGNMENT)){
    			Assignment assignment= AssignmentDAO.INSTANCE.getById(libraryContentLink.source.id);
    			if(assignment==null){
    	            throw new VedantuException(VedantuErrorCode.ASSIGNMENT_NOT_FOUND," with  this  id : "+libraryContentLink.source.id);
    			}else{
        			LOGGER.info("assignment : "+assignment.name);
        			
        			UserEntityAttempt userEntityAttempt=UserEntityAttemptDAO.INSTANCE.getAttempt(
        					getContentStateReq.userId, EntityType.ASSIGNMENT, assignment.id.toString());
        			if(assignment.boardIds!=null && getContentStateReq.brdIds!=null){
        				for(String brdId:getContentStateReq.brdIds){
        					if(assignment.boardIds.contains(brdId)){
        	        			if(userEntityAttempt==null){
        	    					checkWhetherProgramIsCompleted=false;
        	    					break outer;
        	        			}else{
        	        				SrcEntity parentEntity=new SrcEntity();
        	        				parentEntity.id=assignment.id.toString();
        	        				parentEntity.type=EntityType.ASSIGNMENT;
        	        				List<UserQuestionAttempt> userQuestionAttempts=UserQuestionAttemptDAO.INSTANCE.getAttempts(
        	        						getContentStateReq.userId, parentEntity, userEntityAttempt.qIds);
        	        				if(userQuestionAttempts==null){
        	        					LOGGER.info("user : "+getContentStateReq.userId + "has not attmpeted all qids in this assignment : "+assignment.id.toString());
            	    					checkWhetherProgramIsCompleted=false;
            	    					break outer;
        	        				}else{
        								timeCreated=Math.max(timeCreated,userEntityAttempt.lastUpdated);
        	        				}
        	        			}
        					}
        				}
        			}else if(getContentStateReq.brdIds!=null && assignment.boardIds==null){
        				throw new VedantuException(VedantuErrorCode.BOARD_MAPPING_NOT_FOUND," module has no boards");
        			}else if(getContentStateReq.brdIds==null){
	        			if(userEntityAttempt==null){
	    					checkWhetherProgramIsCompleted=false;
	    					break outer;
	        			}else{
	        				SrcEntity parentEntity=new SrcEntity();
	        				parentEntity.id=assignment.id.toString();
	        				parentEntity.type=EntityType.ASSIGNMENT;
	        				List<UserQuestionAttempt> userQuestionAttempts=UserQuestionAttemptDAO.INSTANCE.getAttempts(
	        						getContentStateReq.userId, parentEntity, userEntityAttempt.qIds);
	        				if(userQuestionAttempts==null){
	        					LOGGER.info("user : "+getContentStateReq.userId + "has not attmpeted all qids in this assignment : "+assignment.id.toString());
    	    					checkWhetherProgramIsCompleted=false;
    	    					break outer;
	        				}else{
								timeCreated=Math.max(timeCreated,userEntityAttempt.lastUpdated);
	        				}
	        			}
        			}
        			
    			}
    			
    		}else if(libraryContentLink.source.type.equals(EntityType.VIDEO)){
    			Video video=VideoDAO.INSTANCE.getById(libraryContentLink.source.id);
    			if(video==null){
    	            throw new VedantuException(VedantuErrorCode.VIDEO_NOT_FOUND," with  this  id : "+libraryContentLink.source.id);
    			}else{
        			LOGGER.info("video : "+video.name);
    				if(video.boardIds!=null && getContentStateReq.brdIds!=null){
        				for(String brdId:getContentStateReq.brdIds){
        					if(video.boardIds.contains(brdId)){
        						if(ActivityRecordDAO.INSTANCE.activityFound(getContentStateReq.userId,
            							getContentStateReq.orgId, EntityType.VIDEO,"VIEW",video.id.toString())==false){
                					checkWhetherProgramIsCompleted=false;
                					break outer;
        						}
        						else{
        							long time=ActivityRecordDAO.INSTANCE.getActivities(getContentStateReq.userId,
                							getContentStateReq.orgId, EntityType.VIDEO,"VIEW",video.id.toString()).get(0).timeCreated;
        							timeCreated=Math.max(timeCreated,time);
        						}
        					}
        				}
        			}else if(getContentStateReq.brdIds!=null && video.boardIds==null){
        				throw new VedantuException(VedantuErrorCode.BOARD_MAPPING_NOT_FOUND," video has no boards");
        			}else if(getContentStateReq.brdIds==null){
        				if(ActivityRecordDAO.INSTANCE.activityFound(getContentStateReq.userId,
            							getContentStateReq.orgId, EntityType.VIDEO,"VIEW",video.id.toString())==false){
        					checkWhetherProgramIsCompleted=false;
        					LOGGER.info("inside video checkWhetherProgramIsCompleted : "+checkWhetherProgramIsCompleted);
        					LOGGER.info("inside video id : "+video.id.toString());
        					break outer;
        				}
        				else{
        					LOGGER.info("inside else");
							long time=ActivityRecordDAO.INSTANCE.getActivities(getContentStateReq.userId,
        							getContentStateReq.orgId, EntityType.VIDEO,"VIEW",video.id.toString()).get(0).timeCreated;
							timeCreated=Math.max(timeCreated,time);
						}
        			}

    			}    			
    		}else if(libraryContentLink.source.type.equals(EntityType.DOCUMENT)){
    			Document document=DocumentDAO.INSTANCE.getById(libraryContentLink.source.id);
    			if(document==null){
    	            throw new VedantuException(VedantuErrorCode.DOCUMENT_NOT_FOUND," with  this  id : "+libraryContentLink.source.id);
    			}else{
        			LOGGER.info("document : "+document.name);
    				if(document.boardIds!=null && getContentStateReq.brdIds!=null){
        				for(String brdId:getContentStateReq.brdIds){
        					if(document.boardIds.contains(brdId)){
        						if(ActivityRecordDAO.INSTANCE.activityFound(getContentStateReq.userId,
            							getContentStateReq.orgId, EntityType.DOCUMENT,"VIEW",document.id.toString())==false){
                					checkWhetherProgramIsCompleted=false;
                					break outer;
        						}
        						else{
        							long time=ActivityRecordDAO.INSTANCE.getActivities(getContentStateReq.userId,
                							getContentStateReq.orgId, EntityType.DOCUMENT,"VIEW",document.id.toString()).get(0).timeCreated;
        							timeCreated=Math.max(timeCreated,time);
        						}
        					}
        				}
        			}else if(getContentStateReq.brdIds!=null && document.boardIds==null){
        				throw new VedantuException(VedantuErrorCode.BOARD_MAPPING_NOT_FOUND," document has no boards");
        			}else if(getContentStateReq.brdIds==null){
            			if(ActivityRecordDAO.INSTANCE.activityFound(getContentStateReq.userId,
            							getContentStateReq.orgId, EntityType.DOCUMENT,"VIEW",document.id.toString())==false){
        					checkWhetherProgramIsCompleted=false;
        					break outer;
            			}
            			else{
							long time=ActivityRecordDAO.INSTANCE.getActivities(getContentStateReq.userId,
        							getContentStateReq.orgId, EntityType.DOCUMENT,"VIEW",document.id.toString()).get(0).timeCreated;
							timeCreated=Math.max(timeCreated,time);
						}
        			}

    			}
    			
    		}
    	}
    	LOGGER.info("checkWhetherProgramIsCompleted  : "+checkWhetherProgramIsCompleted);
    	if(checkWhetherProgramIsCompleted){
    		LOGGER.info("inside if of checkWhetherProgramIsCompleted");
    		if(UserDAO.INSTANCE.getById(getContentStateReq.userId)!=null){
        		getContentStateRes.studentName=UserDAO.INSTANCE.getById(getContentStateReq.userId).firstName+" "+
        				UserDAO.INSTANCE.getById(getContentStateReq.userId).lastName;
        		LOGGER.info("inside if of checkWhetherProgramIsCompleted getContentStateRes.name : "+getContentStateRes.studentName);
        		if(OrgProgramDAO.INSTANCE.getById(getContentStateReq.programId)!=null){
            		getContentStateRes.programName=OrgProgramDAO.INSTANCE.getById(getContentStateReq.programId).getName();
            		getContentStateRes.sectionName=OrgSectionDAO.INSTANCE.getById(getContentStateReq.sectionId).getName();
            		getContentStateRes.centreName=OrgCenterDAO.INSTANCE.getById(getContentStateReq.centreId).getName();
            		getContentStateRes.orgName=OrganizationDAO.INSTANCE.getById(getContentStateReq.orgId).name.toString();
            		getContentStateRes.orgId=getContentStateReq.orgId;
            		LOGGER.info("inside if of checkWhetherProgramIsCompleted getContentStateRes.programName : "+getContentStateRes.programName);
            		if(timeCreated!=0L){
            			getContentStateRes.completedDate=timeCreated;
            		}
            		else{
            			Date date = new Date();
            			getContentStateRes.completedDate=date.getTime();
            		}
            		LOGGER.info("inside if of checkWhetherProgramIsCompleted getContentStateRes.completedDate : "+getContentStateRes.completedDate);
        		}
        		
    		}
    		
    	}
		LOGGER.info("getContentStateRes : "+getContentStateRes);
    	return getContentStateRes;
    }
    public static GetContentLinksRes getRemovedContentLinks(GetContentsLinkReq req)
            throws VedantuException {

        req.addContent = false;
        return getContentLinks(req, VedantuRecordState.DELETED);
    }

    public static ListResponse<ContentSearchDetails> getContents(GetContentsReq req) {

        QueryBuilder esQuery = QueryBuilders.filteredQuery(
                QueryBuilders.inQuery(ConstantsGlobal.ID, req.ids.toArray()),
                FilterBuilders.termFilter(ConstantsGlobal.TYPE, req.type.name().toLowerCase()));
        ListResponse<ContentSearchDetails> searchList = null;
		if (req.type == EntityType.QUESTION) {
			searchList = getEntityQuestionInfos(null, null, 0, 0,
					ContentSearchDetails.class, esQuery, null, null,
					ILibraryContent.INDEX_NAME, ILibraryContent.INDEX_TYPE);
		} else {
			searchList = getEntityInfos(null, null, 0, req.ids.size(),
					ContentSearchDetails.class, esQuery, null, null,
					ILibraryContent.INDEX_NAME, ILibraryContent.INDEX_TYPE);
		}

        annotateExtraInfo(StringUtils.EMPTY,req.userId, req.orgId, req.type, searchList.list, true);
        if(req.type == EntityType.TEST){
            LOGGER.debug("**************   getContents    Test id is "+req.ids);
        }
        if (req.type == EntityType.QUESTION) {
            addParagraphInfo(req.ids, searchList);
            if (req.addAnswer) {
                annotateQuestionAnswerInfo(req.ids, searchList);
            }
        }
        return searchList;
    }

    //Concatenation using + is faster than building multiple StringBuilder objects
    private static void addParagraphInfo(List<String> ids, ListResponse<ContentSearchDetails> searchList) {
        Map<String, String> qidToParaMap = new HashMap<String, String>();
        final String htmlBreaks = "<br/><br/>";
        for (String qid : ids) {
            String paraContent = AbstractTestManager.getParaContent(qid);
            if(StringUtils.isNotEmpty(paraContent)) {
                qidToParaMap.put(qid, paraContent);
            }
        }
        for (ContentSearchDetails details : searchList.list) {
            LOGGER.debug("Bosa Question subtype: " + details.subType);
            if(QuestionType.PARA.name().equals(details.subType)){
                String paraContent = qidToParaMap.get(details.id);
                if(StringUtils.isEmpty(paraContent)) {
                    continue;
                }
                LOGGER.debug(":::::   para content " + paraContent);
                LOGGER.debug(":::::   para question content " + details.desc);
                details.name = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, paraContent)
                        + htmlBreaks + details.name;
                details.desc = ImageHTMLUtils.addImageSrcUrl(EntityType.CMDSQUESTION, paraContent)
                        + htmlBreaks + details.desc;
            }
        }
    }


    private static void annotateQuestionAnswerInfo(Collection<String> ids,
            ListResponse<ContentSearchDetails> searchList) {

        Map<String, Answer> ansMap = AnswerDAO.INSTANCE.getQuestionAnswerMap(ids);
        for (ContentSearchDetails sDetails : searchList.list) {
            Answer ans = ansMap.get(sDetails.id);
            GetSolutionsReq solutionsReq = new GetSolutionsReq();
            solutionsReq.qId = sDetails.id;
            GetSolutionsRes solutionRes;
            try {
                solutionRes = QuestionManager.getSolutions(solutionsReq);
                annotateQuestionAnswerInfo(ans, solutionRes, sDetails);
            } catch (VedantuException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
    }

    public static void annotateQuestionAnswerInfo(Answer ans, GetSolutionsRes solutionsRes,
            ContentSearchDetails sDetails) {

        if (ans != null) {
            JSONObject info = sDetails.__getInfo();
            if (info != null) {
                try {
                    JSONObject answerJSON = new JSONObject(ObjectMapperUtils.convertValue(ans,
                            Map.class));
                    answerJSON.put("id", ans._getStringId());
                    LOGGER.debug("answerJSON: " + answerJSON);
                    info.put("answer", answerJSON);

                    LOGGER.debug("solutionJSON: " + solutionsRes);

                    if (solutionsRes != null && CollectionUtils.isNotEmpty(solutionsRes.list)) {
                        JSONObject solutionJSON = new JSONObject(ObjectMapperUtils.convertValue(
                                solutionsRes.list.get(0), Map.class));
                        info.put("solution", solutionJSON);
                    }

                } catch (JSONException e) {}
                sDetails.setInfo(info.toString());
            }
        }
    }

    private static GetContentLinksRes getContentLinks(GetContentsLinkReq req,
            VedantuRecordState recordState) throws VedantuException {

        GetContentLinksRes res = new GetContentLinksRes();
        Set<String> childrenIds = null;

        //To find and eliminate useless contentlinks with target.id as moduleId - part 1
        if(req.target.type == EntityType.MODULE) {
            Module module = ModuleDAO.INSTANCE.getModuleById(req.target.id);
            if(module == null) {
                throw new VedantuException(VedantuErrorCode.INVALID_ID);
            }
            childrenIds = new HashSet<String>(module.children.size());
            for (ModuleEntry child : module.children) {
                if(child.entity == null) {
                    continue;
                }
                childrenIds.add(child.entity.id);
            }
            LOGGER.debug("Bosa debug log ::childrenIds: " + childrenIds);
        }
        //End - To find and eliminate useless contentlinks with target.id as moduleId - part 1

        DBObject query = new BasicDBObject(ConstantsGlobal.TARGET_DOT_TYPE, req.target.type.name());
        query.put(ConstantsGlobal.TARGET_DOT_ID, req.target.id);
        query.put(ConstantsGlobal.LINK_TYPE, req.linkType.name());
        if (recordState == VedantuRecordState.ACTIVE) {
            query.put("scope", new BasicDBObject(MongoManager.NE_QUERY, Scope.PRIVATE.name()));
        }
        query.put(ConstantsGlobal.RECORD_STATE, recordState.name());
        if (req.addedAfter > 0) {
            // by querying with lastUpdate i will also get the content updated in the same request
            // else i have to make a separate request for updatedItem
            query.put(ConstantsGlobal.LAST_UPDATED, new BasicDBObject("$gt", req.addedAfter));
        }

        VedantuDBResult<LibraryContentLink> links = LibraryContentLinksDAO.INSTANCE.getInfos(query,
                null, req.start, req.size, MongoManager.getSortQuery(req.orderBy, req.sortOrder));
        res.totalHits = links.totalHits;

        //To find and eliminate useless contentlinks with target.id as moduleId - part 2
        if (childrenIds != null) {
            Iterator<LibraryContentLink> linkIterator = links.results.iterator();
            while(linkIterator.hasNext()) {
                LibraryContentLink libraryContentLink = linkIterator.next();
                if(!childrenIds.contains(libraryContentLink.source.id)) {
                    LOGGER.debug("Bosa debug log ::removing: " + libraryContentLink.source.id);
                    linkIterator.remove();
                    res.totalHits--;
                }
            }
        }
        //End - To find and eliminate useless contentlinks with target.id as moduleId - part 2

        Map<SrcEntity, ContentSearchDetails> entityDetailsMap = null;
        if (req.addContent) {
            entityDetailsMap = getEntityDetailsMap(req.orgId, req.targetUserId, links.results);
        }

        ContentSecurityManager csManager = new ContentSecurityManager();


        for (LibraryContentLink link : links.results) {
            GetContentLinkRes cLink = new GetContentLinkRes(link._getStringId(), link.timeCreated,
                    link.lastUpdated, link.target, link.isDownloadable(),
                    link.getDownloadableEntities(), (int) link.position);
            if (req.addContent) {
                cLink.content = entityDetailsMap.get(link.source);
            } else if (recordState == VedantuRecordState.DELETED) {
                // only in case of deleted link the link source info will be returned not the
                // complete ContentSearchDetails object
                cLink.content = link.source;
            }
            EncryptionLevel encLevel = csManager.getEncLevel(link, req.orgId);
            boolean encrypted = encLevel != null && encLevel != EncryptionLevel.NA;
            EntityType srcType = link.source.type;
            if(srcType == EntityType.TEST){
                Logger.debug("******** getContentLinks Test id is "+link.source.id);
            }
//            if(link.getSchedule() != null){
//                try {
//                    JSONObject temp = new JSONObject(((ContentSearchDetails)cLink.content).getInfo());
//                    temp.put("startTime", link.getSchedule().startTime.getTime());
//                    ((ContentSearchDetails)cLink.content).setInfo(temp.toString());
//                } catch (JSONException e) {
//                    LOGGER.debug("******** getContentLinks Exception came "+e.getMessage());
//                }
//            }
            if(link.getSchedule() != null){
                if(link.getSchedule().startTime != null){
                    cLink.startTime = link.getSchedule().startTime.getTime();
                }else{
                    cLink.startTime = Long.MIN_VALUE;
                }
                if(link.getSchedule().endTime != null) {
                    cLink.endTime = link.getSchedule().endTime.getTime();
                }else{
                    cLink.endTime = Long.MIN_VALUE;
                }
                if(link.getSchedule().closeTime != null) {
                    cLink.closeTime = link.getSchedule().closeTime.getTime();
                }else{
                    cLink.closeTime = Long.MIN_VALUE;
                }
            }
            if (encrypted && srcType != EntityType.QUESTION && srcType != EntityType.TEST
                    && srcType != EntityType.ASSIGNMENT && srcType != EntityType.MODULE) {
                cLink.encLevel = encLevel;
                cLink.passphrase = csManager.getPassphrase(encLevel, link, req.userId, req.orgId);
            }

            res.list.add(cLink);
        }
        if (res.list.size() > 0) {
            res.latestContent = res.list.get(res.list.size() - 1).lastUpdated;
        }
        else {
            res.latestContent = req.addedAfter;
        }
        return res;
    }

    public static GetContentLinkRes getContentLink(String orgId, String userId, SrcEntity content,
            SrcEntity target, UserActionType linkType, VedantuRecordState recordState)
            throws VedantuException {

        DBObject query = new BasicDBObject(ConstantsGlobal.TARGET_DOT_TYPE, target.type.name());
        query.put(ConstantsGlobal.TARGET_DOT_ID, target.id);
        query.put(ConstantsGlobal.LINK_TYPE, linkType.name());
        if (recordState == VedantuRecordState.ACTIVE) {
            query.put("scope", new BasicDBObject(MongoManager.NE_QUERY, Scope.PRIVATE.name()));
        }
        query.put(ConstantsGlobal.RECORD_STATE, recordState.name());

        MutableLong totalHits = new MutableLong();

        LibraryContentLink link = LibraryContentLinksDAO.INSTANCE.getLibraryContentLink(content,
                target, linkType, recordState, totalHits);
        if (link == null) {
            return null;
        }

        GetContentLinkRes cLink = new GetContentLinkRes(link._getStringId(), link.timeCreated,
                link.lastUpdated, link.target, link.isDownloadable(),
                link.getDownloadableEntities(), (int) link.position);
        VedantuBasicDAO<?, ?> contentDAO = EntityTypeDAOFactory.INSTANCE.get(content.type);
        VedantuBaseMongoModel mongoModel = null;
        if (contentDAO != null) {
            mongoModel = contentDAO.getById(content.id);

        }

        ContentSearchDetails unifiedContentSearchDetails = null;

        try {
            if (mongoModel != null && mongoModel instanceof AbstractBoardEntityTagModel) {
                EventType indexEventType = EntityIndexEventMapper.INSTANCE.get(content.type);
                IEventDetails details = EventDetailsFactory.getInstance()
                        .getDetails(indexEventType);
                if (details instanceof AbstractBoardSearchEntityTagDetails) {
                    AbstractBoardSearchEntityTagDetails searchDetails = (AbstractBoardSearchEntityTagDetails) details;
                    searchDetails.__addBoardDetails(true);
                    searchDetails.fromMongoModel(mongoModel);
                    if (searchDetails instanceof ILibraryContent) {
                        unifiedContentSearchDetails = ((ILibraryContent) searchDetails)
                                .__getContentSearchDetails();
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.debug("Exception exception", exception);
        }

        unifiedContentSearchDetails = (ContentSearchDetails) annotateExtraInfo(userId, orgId,
                content.type, unifiedContentSearchDetails);
        if (unifiedContentSearchDetails != null) {
            cLink.content = unifiedContentSearchDetails;
            LOGGER.debug("Created cLink " + unifiedContentSearchDetails);
            return cLink;
        }

        return null;

    }

    private static Map<SrcEntity, ContentSearchDetails> getEntityDetailsMap(String orgId,
            String userId, List<LibraryContentLink> links) {

        Map<SrcEntity, ContentSearchDetails> detailsMap = new HashMap<SrcEntity, ContentSearchDetails>();
        Map<EntityType, Set<String>> eTypeToIdsMap = new HashMap<EntityType, Set<String>>();
        for (LibraryContentLink link : links) {
            if (eTypeToIdsMap.get(link.source.type) == null) {
                eTypeToIdsMap.put(link.source.type, new HashSet<String>());
            }
            eTypeToIdsMap.get(link.source.type).add(link.source.id);
        }
        OrFilterBuilder orFilter = FilterBuilders.orFilter();

        for (Entry<EntityType, Set<String>> entry : eTypeToIdsMap.entrySet()) {
            if (CollectionUtils.isNotEmpty(entry.getValue())) {
                orFilter.add(FilterBuilders.andFilter(
                        FilterBuilders.termFilter(ConstantsGlobal.TYPE, entry.getKey().name()
                                .toLowerCase()),
                        FilterBuilders.inFilter(ConstantsGlobal.ID, entry.getValue().toArray())));
            }
        }
        QueryBuilder esQuery = QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), orFilter);
        SearchListResponse<ContentSearchDetails> searchList = getEntityInfos(null, null, 0,
                links.size(), ContentSearchDetails.class, esQuery, null, null,
                ILibraryContent.INDEX_NAME, ILibraryContent.INDEX_TYPE);
        annotateExtraInfo(StringUtils.EMPTY, userId, orgId, null, searchList.list, true);
        Gson gson = new Gson();
        for (ContentSearchDetails info : searchList.list) {
            if(info.type == EntityType.TEST){
                Logger.debug("******** getEntityDetailsMap Test id is "+info.id);
                JSONObject tempInfo;
                try {
                    Test test = TestDAO.INSTANCE.getById(info.id);
                    tempInfo = new JSONObject(info.getInfo());
                    tempInfo.put("isSubjectiveTest",test.subjectiveTest);
                    tempInfo.put("isPartialEnabled", test.enablePartialMarks);
                    tempInfo.put("isSectionLockEnabled", test.enableSectionLocking);
                    tempInfo.put("oneOrMoreMarksQTypes", test.oneOrMoreMarksQTypes);
                    tempInfo.put("isNTAPattern",test.isNTAPattern);
                    if(test.enablePartialMarks){
                        tempInfo.put("partialMarksQTypes", test.partialMarksQTypes);
                    }
                    if(test.simplifiedBoardNames != null && !test.simplifiedBoardNames.isEmpty()){
                        tempInfo.put("simplifiedBoardNames", gson.toJson(test.simplifiedBoardNames, (new TypeToken<List<SimplifiedBoardNames>>(){}).getType()));
                    }
                    info.setInfo(tempInfo.toString());
                } catch (JSONException e) {
                    LOGGER.debug("******** getEntityDetailsMap Exception came "+e.getMessage());
                }
                //info.info = TestDAO.INSTANCE.getById(link.source.id).enablePartialMarks;
            }
            annotateThumbnailInfo(info, false);
            SrcEntity entity = new SrcEntity(info.type, info.id);

            detailsMap.put(entity, info);
        }
        return detailsMap;
    }

    public static void
            annotateThumbnailInfo(ContentSearchDetails contentDetails, boolean encrypted) {

        switch (contentDetails.type) {
        case VIDEO:
            contentDetails.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(contentDetails.type,
                    contentDetails.thumbnail);
            if (LinkType.UPLOADED.name().equalsIgnoreCase(contentDetails.subType)) {
                if (contentDetails.__getInfo() != null) {
                    JSONObject info = contentDetails.__getInfo();
                    try {
                        Video video = VideoDAO.INSTANCE.getById(contentDetails.id);
                        String orgFileName = info.getString("originalFileName");
                        String[] orgFileNameBreakDown = orgFileName.split("\\.");
                        String orgExtension = orgFileNameBreakDown[orgFileNameBreakDown.length - 1];
                        info.put(ConstantsGlobal.URL, ImageDisplayURLUtil.getEntityDownloadURL(
                                EntityType.VIDEO, JSONUtils.getString(info, ConstantsGlobal.UUID),
                                FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                                encrypted ? FileCategory.ENCRYPTED : FileCategory.CONVERTED));
                        info.put("backupVideoUrl", ImageDisplayURLUtil.getEntityDownloadURL(
                                EntityType.VIDEO, JSONUtils.getString(info, ConstantsGlobal.UUID),
                                orgExtension.equalsIgnoreCase("mp4") ? FileUtils.MP4_EXTENTION_WITHOUT_DOT : FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                                FileCategory.ORIGINAL));
                        info.put("extension", video.extension);

                    } catch (JSONException e) {
                        LOGGER.debug("Exception when trying to get extension and originalFileName data from info");
                        LOGGER.error(e.getMessage(), e);
                    }

                }
            }
            break;
        case DOCUMENT:
            contentDetails.thumbnail = ImageDisplayURLUtil.getEntityThumbnail(contentDetails.type,
                    contentDetails.thumbnail);
            if (contentDetails.__getInfo() != null) {
                JSONObject info = contentDetails.__getInfo();
                try {
                    info.put(ConstantsGlobal.URL, ImageDisplayURLUtil.getEntityDownloadURL(
                            EntityType.DOCUMENT, JSONUtils.getString(info, ConstantsGlobal.UUID),
                            FileUtils.PDF_EXTENSTION_WITHOUT_DOT, MediaType.DOC,
                            encrypted ? FileCategory.ENCRYPTED : FileCategory.CONVERTED));
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            break;
        case FILE:
            if (contentDetails.__getInfo() != null) {
                JSONObject info = contentDetails.__getInfo();
                try {
                    info.put(ConstantsGlobal.URL, ImageDisplayURLUtil.getEntityDownloadURL(
                            EntityType.FILE, JSONUtils.getString(info, ConstantsGlobal.UUID),
                            JSONUtils.getString(info, ConstantsGlobal.EXTENSION), MediaType.FILE,
                            encrypted ? FileCategory.ENCRYPTED : FileCategory.ORIGINAL));
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage(), e);
                }

            }
        default:
            break;

        }
    }

    @SuppressWarnings("unchecked")
    public static boolean reIndexLibraryContents(final EntityType contentType, String... ids)
            throws VedantuException {

        if (!entitySearchIndexDetailsMap.keySet().contains(contentType)) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE, "contentType: "
                    + contentType + " is not supported in library");
        }

        int start = 0;
        boolean hasMore = true;
        QueryBuilder esQuery = ids == null || ids.length == 0 ? QueryBuilders.matchAllQuery()
                : QueryBuilders.inQuery(ConstantsGlobal.ID, ids);

        while (hasMore) {

            SearchResponse response = ElasticSearchUtils.getSearchResponse(esQuery,
                    ConstantsGlobal.TIME_CREATED, SortOrder.ASC.name(), start, REQ_SIZE,
                    contentType.getIndexName(), contentType.getIndexType(), null);
            SearchHits hits = response.getHits();
            for (SearchHit hit : hits.getHits()) {
                ILibraryContent content = (ILibraryContent) ObjectMapperUtils.convertValue(
                        hit.sourceAsMap(), entitySearchIndexDetailsMap.get(contentType));
                ContentSearchDetails details = null;
                try {
                    details = content.__getContentSearchDetails();
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                LOGGER.debug("ContentSearchDetails: " + details);
                if (details == null || !details._isIndexable()) {
                    continue;
                }
                SearchHit searchHit = ElasticSearchUtils.findOne(ILibraryContent.INDEX_NAME,
                        ILibraryContent.INDEX_TYPE, details._getEsQuery());
                if (searchHit == null) {
                    ElasticSearchManager.getInstance().addIndex(ILibraryContent.INDEX_NAME,
                            ILibraryContent.INDEX_TYPE,
                            ObjectMapperUtils.convertValue(details, Map.class));
                } else {
                    ElasticSearchManager.getInstance().reIndex(searchHit.getIndex(),
                            ObjectMapperUtils.convertValue(details, Map.class), searchHit.getId(),
                            searchHit.getType());
                }

            }
            start += REQ_SIZE;

            hasMore = start < hits.getTotalHits();
        }

        return true;
    }

    public static boolean reIndexContentLink(@Required EntityType entityType,
            UserActionType linkType, String... ids) throws VedantuException {

        DBObject query = new BasicDBObject("source.type", entityType.name());
        if (ids != null && ids.length > 0) {
            query.put("source.id", new BasicDBObject(MongoManager.IN_QUERY, ids));
        }

        if (linkType != null) {
            query.put(ConstantsGlobal.LINK_TYPE, linkType.name());
        }

        int start = 0;
        boolean hasMore = true;

        while (hasMore) {
            VedantuDBResult<LibraryContentLink> links = LibraryContentLinksDAO.INSTANCE.getInfos(
                    query, null, start, REQ_SIZE, MongoManager.getSortQuery(
                            ConstantsGlobal.TIME_CREATED, MongoManager.SortOrder.ASC.name()));
            for (LibraryContentLink link : links.results) {
                ContentLinkRelationshipDetails libraryContentLinkDetails = new ContentLinkRelationshipDetails(
                        link.userId, link.source, link.target, link.getScope());
                libraryContentLinkDetails.position= link.position;
                libraryContentLinkDetails.schedule = link.getSchedule();
                QueryBuilder esQuery = QueryBuilders
                        .boolQuery()
                        .must(QueryBuilders.termQuery(ConstantsGlobal.ID, link.source.id))
                        .must(QueryBuilders.termQuery(ConstantsGlobal.TYPE, link.source.type.name()
                                .toLowerCase()));
                SearchHit hit = ElasticSearchUtils.findOne(ILibraryContent.INDEX_NAME,
                        ILibraryContent.INDEX_TYPE, esQuery);
                if (hit != null) {
                    updateUserActionMappintToEs(libraryContentLinkDetails, link.source,
                            ILibraryContent.INDEX_NAME, ILibraryContent.INDEX_TYPE,
                            link.linkType.getSearchIndexType(), EventActionType.UPDATE, hit.getId());
                }
            }
            start += REQ_SIZE;
            hasMore = start < links.totalHits;
        }
        return true;
    }

    public static void addOrUpdateContentSearchDetails(AbstractSearchDetail details) {

        addOrUpdateContentSearchDetails(details, false);
    }

    public static void addOrUpdateContentSearchDetails(AbstractSearchDetail details,
            boolean ensureQueryState) {

        if (!(details instanceof ILibraryContent)) {
            return;
        }
        ContentSearchDetails libraryContent = null;
        try {
            libraryContent = ((ILibraryContent) (details)).__getContentSearchDetails();
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (libraryContent == null || !libraryContent._isIndexable()) {
            return;
        }

        SearchHit searchHit = ElasticSearchUtils.findOne(ILibraryContent.INDEX_NAME,
                ILibraryContent.INDEX_TYPE, libraryContent._getEsQuery());

        if (searchHit == null) {
            String esId = ElasticSearchManager.getInstance().addIndex(ILibraryContent.INDEX_NAME,
                    ILibraryContent.INDEX_TYPE, getValuesMap(libraryContent));
            if (StringUtils.isNotEmpty(esId) && ensureQueryState) {
                boolean isQueriable = false;

                int tryCount = 0;
                while (!isQueriable && tryCount < ES_ENSURE_QUERY_STATE_MAX_TRY_COUNT) {
                    tryCount++;
                    LOGGER.debug("tryCount: " + tryCount + ", query:"
                            + libraryContent._getEsQuery());
                    searchHit = ElasticSearchUtils.findOne(ILibraryContent.INDEX_NAME,
                            ILibraryContent.INDEX_TYPE, libraryContent._getEsQuery());
                    if (searchHit != null) {
                        isQueriable = true;
                    } else {
                        try {

                            Thread.sleep(ELASTIC_SEARCH_REFRESH_TIME);

                        } catch (InterruptedException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }

        } else {
            ElasticSearchManager.getInstance().reIndex(searchHit.getIndex(),
                    getValuesMap(libraryContent), searchHit.getId(), searchHit.getType());
        }
    }

    public static void removeContentSearchDetails(AbstractSearchDetail details) {

        if (!(details instanceof ILibraryContent)) {
            return;
        }
        ContentSearchDetails libraryContent = null;
        try {
            libraryContent = ((ILibraryContent) (details)).__getContentSearchDetails();
        } catch (JSONException e) {
            LOGGER.error(e.getMessage(), e);
        }

        ElasticSearchManager.getInstance().removeEntry(ILibraryContent.INDEX_NAME,
                ILibraryContent.INDEX_TYPE, libraryContent._getEsQuery());
    }

    public static GetEntityInfoForAppRes getEntityInfoForApp(GetEntityInfoForAppReq getInfoReq) throws VedantuException {
        GetEntityInfoForAppRes res = new GetEntityInfoForAppRes();
        SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, getInfoReq.orgId);
        UserEntityRatings userRating = UserEntityRatingsDAO.INSTANCE.getUserRating(
                getInfoReq.userId, getInfoReq.entity, contentSrc);
        if(getInfoReq.entity.type == EntityType.VIDEO){
            Video video = VideoDAO.INSTANCE.getById(getInfoReq.entity.id);
            if(video != null){
                if (video.linkType == LinkType.UPLOADED) {
                    String[] fileName = (video.originalFileName).split("\\.");
                    String originalExtension = fileName[fileName.length - 1];
                    String extension = video.extension;
                    if(!extension.equals(originalExtension)){
                        res.showToggleSwitch = true;
                        res.webmUrl = ImageDisplayURLUtil.getEntityDownloadURL(EntityType.VIDEO,
                                video.uuid, FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                                FileCategory.CONVERTED);
                        res.mp4Url = ImageDisplayURLUtil.getEntityDownloadURL(EntityType.VIDEO,
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

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getValuesMap(AbstractSearchDetail details) {

        Map<String, Object> esValueMap = ObjectMapperUtils.convertValue(details, Map.class);
        esValueMap.remove(ConstantsGlobal.USER);// this will be added to the
                                                // model while rendering..
        esValueMap.remove("action");
        esValueMap.remove("isNotificationEnabled");
        esValueMap.remove("boardTree");
        return esValueMap;
    }

    public static GetFileInfosRes getFileInfo(GetFileInfoReq request) throws VedantuException {

        GetFileInfosRes response = new GetFileInfosRes();

        if (CollectionUtils.isNotEmpty(request.contents)) {
            for (SrcEntity entity : request.contents) {
                LOGGER.debug("Checking for content" + entity.type + "  " + entity.id);
                if( entity.type == null || entity.type == EntityType.UNKNOWN ){
                    continue;
                }
                IContentManager manager = EntityTypeContentManagerFactory.INSTANCE.get(entity.type);
                if (manager == null) {
                    LOGGER.debug("Content manager not found for " + entity);
                    continue;
                }
                List<DownloadableFileInfo> infos = manager.getFiles(entity.type, entity.id);
                if (CollectionUtils.isNotEmpty(infos)) {
                    response.list.addAll(infos);
                }

            }
        }
        return response;
    }

    public static boolean getEntity(GetEntityReq getEntityReq) {
        LibraryContentLink clink = LibraryContentLinksDAO.INSTANCE
                .getBySourceAndSection(getEntityReq);
        if (clink == null) {
            List<LibraryContentLink> mlink = LibraryContentLinksDAO.INSTANCE
                    .getListBySourceIdAndType(getEntityReq);
            for (LibraryContentLink link : mlink) {
                getEntityReq.entityId = link.target.id;
                getEntityReq.entityType = link.target.type;
                LibraryContentLink rlink = LibraryContentLinksDAO.INSTANCE
                        .getBySourceAndSection(getEntityReq);
                if (rlink != null) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static GetEntityInfoForAppRes addRatingAndFeedback(AddEntityInfoReq getEntityReq) throws VedantuException{
        SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, getEntityReq.orgId);
        UserEntityRatings userRating = UserEntityRatingsDAO.INSTANCE.getUserRating(
                getEntityReq.userId, getEntityReq.entity, contentSrc);
        if (userRating == null) {
            // User is rating first time
            LOGGER.debug("Recording User Rating");
            if (getEntityReq.entity.type == EntityType.VIDEO) {
                // Increment count of ratings
                Video video = VideoDAO.INSTANCE.getById(getEntityReq.entity.id);
                if(getEntityReq.rating == UserRatingType.AVERAGE){
                    video.average++;
                }else if(getEntityReq.rating == UserRatingType.GOOD){
                    video.good++;
                }else if(getEntityReq.rating == UserRatingType.BAD){
                    video.bad++;
                }
                VideoDAO.INSTANCE.save(video);
            } else if (getEntityReq.entity.type == EntityType.TEST) {
                // Increment count of ratings
                Test test = TestDAO.INSTANCE.getById(getEntityReq.entity.id);
                if(getEntityReq.rating == UserRatingType.AVERAGE){
                    test.average++;
                }else if(getEntityReq.rating == UserRatingType.GOOD){
                    test.good++;
                }else if(getEntityReq.rating == UserRatingType.BAD){
                    test.bad++;
                }
                TestDAO.INSTANCE.save(test);
            } else if (getEntityReq.entity.type == EntityType.DOCUMENT) {
                // Increment count of ratings
                Document doc = DocumentDAO.INSTANCE.getById(getEntityReq.entity.id);
                if(getEntityReq.rating == UserRatingType.AVERAGE){
                    doc.average++;
                }else if(getEntityReq.rating == UserRatingType.GOOD){
                    doc.good++;
                }else if(getEntityReq.rating == UserRatingType.BAD){
                    doc.bad++;
                }
                DocumentDAO.INSTANCE.save(doc);
            } else if (getEntityReq.entity.type == EntityType.ASSIGNMENT) {
                // Increment count of ratings
                Assignment assignment = AssignmentDAO.INSTANCE.getById(getEntityReq.entity.id);
                if(getEntityReq.rating == UserRatingType.AVERAGE){
                    assignment.average++;
                }else if(getEntityReq.rating == UserRatingType.GOOD){
                    assignment.good++;
                }else if(getEntityReq.rating == UserRatingType.BAD){
                    assignment.bad++;
                }
                AssignmentDAO.INSTANCE.save(assignment);
            } else{
                throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
            }
            UserEntityRatingsDAO.INSTANCE.addUserRatingAndReview(getEntityReq.userId,
                    getEntityReq.entity, contentSrc, getEntityReq.rating, getEntityReq.feedback);
        } else {
            // User has rated already
            LOGGER.debug("Updating User Rating");
            if (userRating.rating == getEntityReq.rating) {
                // Update only feedback
                if (getEntityReq.entity.type == EntityType.VIDEO
                        || getEntityReq.entity.type == EntityType.TEST
                        || getEntityReq.entity.type == EntityType.DOCUMENT
						|| getEntityReq.entity.type == EntityType.ASSIGNMENT) {
					LOGGER.debug("Updating Only Feedback");
                    userRating.feedback = getEntityReq.feedback;
                    UserEntityRatingsDAO.INSTANCE.save(userRating);
                } else {
                    throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
                }
			} else {
				LOGGER.debug("Updating Rating and Review");
                UserRatingType previousRating = userRating.rating;
                // Update rating and feedback
				if (getEntityReq.entity.type == EntityType.VIDEO) {
					// Increment count of ratings
					LOGGER.debug("Updating for entity "
							+ getEntityReq.entity.type);
                    Video video = VideoDAO.INSTANCE.getById(getEntityReq.entity.id);
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
                    VideoDAO.INSTANCE.save(video);
                } else if (getEntityReq.entity.type == EntityType.TEST) {
                    // Increment count of ratings
                    Test test = TestDAO.INSTANCE.getById(getEntityReq.entity.id);
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
                    TestDAO.INSTANCE.save(test);
                } else if (getEntityReq.entity.type == EntityType.DOCUMENT) {
                    // Increment count of ratings
                    Document doc = DocumentDAO.INSTANCE.getById(getEntityReq.entity.id);
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
                    DocumentDAO.INSTANCE.save(doc);
                } else if (getEntityReq.entity.type == EntityType.ASSIGNMENT) {
                    // Increment count of ratings
                    Assignment assignment = AssignmentDAO.INSTANCE.getById(getEntityReq.entity.id);
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
                    AssignmentDAO.INSTANCE.save(assignment);
                } else{
                    throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
                }
                userRating.rating = getEntityReq.rating;
                userRating.feedback = getEntityReq.feedback;
                UserEntityRatingsDAO.INSTANCE.save(userRating);
            }
        }
        return getEntityInfoForApp(getEntityReq);
    }

    public static GetCMDSEntityInfoRes getCMDSEntityInfo(GetEntityInfoForAppReq getInfoReq) throws VedantuException {
        GetCMDSEntityInfoRes res = new GetCMDSEntityInfoRes();
        SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, getInfoReq.orgId);
        if(getInfoReq.entity.type == EntityType.CMDSVIDEO){
            Video video = VideoDAO.INSTANCE.getByCMDSVideoId(getInfoReq.entity.id);
            if(video != null){
                if (video.linkType == LinkType.UPLOADED) {
                    String[] fileName = (video.originalFileName).split("\\.");
                    String originalExtension = fileName[fileName.length - 1];
                    String extension = video.extension;
                    if(!extension.equals(originalExtension)){
                        res.showToggleSwitch = true;
                        res.webmUrl = ImageDisplayURLUtil.getEntityDownloadURL(EntityType.VIDEO,
                                video.uuid, FileUtils.WEBM_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                                FileCategory.CONVERTED);
                        res.mp4Url = ImageDisplayURLUtil.getEntityDownloadURL(EntityType.VIDEO,
                                video.uuid, FileUtils.MP4_EXTENTION_WITHOUT_DOT, MediaType.VIDEO,
                                FileCategory.ORIGINAL);
                    }
                }
                res.badRatingCount = video.bad;
                res.goodRatingCount = video.good;
                res.avgRatingCount = video.average;
                getInfoReq.entity.type = EntityType.VIDEO;
                getInfoReq.entity.id = video._getStringId();
                res.reviewCount = UserEntityRatingsDAO.INSTANCE.getReviewCount(getInfoReq.entity,contentSrc);
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSTEST){
            Test test = TestDAO.INSTANCE.getByCMDSTestId(getInfoReq.entity.id);
            if(test != null){
                res.badRatingCount = test.bad;
                res.goodRatingCount = test.good;
                res.avgRatingCount = test.average;
                getInfoReq.entity.type = EntityType.TEST;
                getInfoReq.entity.id = test._getStringId();
                res.reviewCount = UserEntityRatingsDAO.INSTANCE.getReviewCount(getInfoReq.entity,contentSrc);
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSASSIGNMENT) {
            Assignment assignment = AssignmentDAO.INSTANCE.getByCMDSAssignmentId(getInfoReq.entity.id);
            if(assignment != null){
                res.badRatingCount = assignment.bad;
                res.goodRatingCount = assignment.good;
                res.avgRatingCount = assignment.average;
                getInfoReq.entity.type = EntityType.ASSIGNMENT;
                getInfoReq.entity.id = assignment._getStringId();
                res.reviewCount = UserEntityRatingsDAO.INSTANCE.getReviewCount(getInfoReq.entity,contentSrc);
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSDOCUMENT) {
            Document doc = DocumentDAO.INSTANCE.getByCMDSDocId(getInfoReq.entity.id);
            if(doc != null){
                res.badRatingCount = doc.bad;
                res.goodRatingCount = doc.good;
                res.avgRatingCount = doc.average;
                getInfoReq.entity.type = EntityType.DOCUMENT;
                getInfoReq.entity.id = doc._getStringId();
                res.reviewCount = UserEntityRatingsDAO.INSTANCE.getReviewCount(getInfoReq.entity,contentSrc);
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }
        else{
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }
        return res;
    }

    public static GetEntityReviewsRes getEntityReviews(GetEntityReviewsReq getInfoReq) throws VedantuException {
        GetEntityReviewsRes res = new GetEntityReviewsRes();
        SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, getInfoReq.orgId);

        List<GetEntityReviews> goodReviews = new ArrayList<GetEntityReviews>();
        List<GetEntityReviews> avgReviews = new ArrayList<GetEntityReviews>();
        List<GetEntityReviews> badReviews = new ArrayList<GetEntityReviews>();

        if(getInfoReq.entity.type == EntityType.CMDSVIDEO){
            Video video = VideoDAO.INSTANCE.getByCMDSVideoId(getInfoReq.entity.id);
            if(video != null){
                getInfoReq.entity.type = EntityType.VIDEO;
                getInfoReq.entity.id = video._getStringId();
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSTEST){
            Test test = TestDAO.INSTANCE.getByCMDSTestId(getInfoReq.entity.id);
            if(test != null){
                getInfoReq.entity.type = EntityType.TEST;
                getInfoReq.entity.id = test._getStringId();
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSASSIGNMENT) {
            Assignment assignment = AssignmentDAO.INSTANCE.getByCMDSAssignmentId(getInfoReq.entity.id);
            if(assignment != null){
                getInfoReq.entity.type = EntityType.ASSIGNMENT;
                getInfoReq.entity.id = assignment._getStringId();
            }else{
                throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED);
            }
        }else if(getInfoReq.entity.type == EntityType.CMDSDOCUMENT) {
            Document doc = DocumentDAO.INSTANCE.getByCMDSDocId(getInfoReq.entity.id);
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


        DBObject query = new BasicDBObject();
        query.put(ConstantsGlobal.CONTENT_SRC_DOT_ID, contentSrc.id);
        query.put(ConstantsGlobal.CONTENT_SRC_DOT_TYPE, contentSrc.type.name());
        query.put(ConstantsGlobal.SRC_ENTITY_DOT_ID, getInfoReq.entity.id);
        query.put(ConstantsGlobal.SRC_ENTITY_DOT_TYPE, getInfoReq.entity.type.name());

        if(getInfoReq.ratingType.equalsIgnoreCase("ALL") || getInfoReq.ratingType.equalsIgnoreCase("GOOD")){
            query.put("rating", UserRatingType.GOOD.name());
            VedantuDBResult<UserEntityRatings> goodRatings = UserEntityRatingsDAO.INSTANCE.getInfos(
                    query, null, getInfoReq.start, getInfoReq.size,
                    MongoManager.getSortQuery(ConstantsGlobal.LAST_UPDATED, SortOrder.DESC.name()));
            res.totalGoodHits = goodRatings.totalHits;
            for(UserEntityRatings userRating : goodRatings.results){
                GetEntityReviews ratings = new GetEntityReviews();
                ratings.userId = userRating.userId;
                GetUserSelfFullProfileRes resp = UserManager.getUserFullProfile(userRating.userId);
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
            query.put("rating", UserRatingType.BAD.name());
            VedantuDBResult<UserEntityRatings> badRatings = UserEntityRatingsDAO.INSTANCE.getInfos(
                    query, null, getInfoReq.start, getInfoReq.size,
                    MongoManager.getSortQuery(ConstantsGlobal.LAST_UPDATED, SortOrder.DESC.name()));
            res.totalBadHits = badRatings.totalHits;
            for(UserEntityRatings userRating : badRatings.results){
                GetEntityReviews ratings = new GetEntityReviews();
                ratings.userId = userRating.userId;
                GetUserSelfFullProfileRes resp = UserManager.getUserFullProfile(userRating.userId);
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
            query.put("rating", UserRatingType.AVERAGE.name());
            VedantuDBResult<UserEntityRatings> avgRatings = UserEntityRatingsDAO.INSTANCE.getInfos(
                    query, null, getInfoReq.start, getInfoReq.size,
                    MongoManager.getSortQuery(ConstantsGlobal.LAST_UPDATED, SortOrder.DESC.name()));
            res.totalAvgHits = avgRatings.totalHits;
            for(UserEntityRatings userRating : avgRatings.results){
                GetEntityReviews ratings = new GetEntityReviews();
                ratings.userId = userRating.userId;
                GetUserSelfFullProfileRes resp = UserManager.getUserFullProfile(userRating.userId);
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

    public static GetScheduleRes getSchedule(GetScheduleReq getScheduleReq) {
        // TODO Auto-generated method stub
        GetScheduleRes res = new GetScheduleRes();
        VedantuDBResult<Schedule> scheduleList = ScheduleDAO.INSTANCE.getScheduleByMonth(getScheduleReq.orgId, getScheduleReq.sectionId, getScheduleReq.month);
        res.month = getScheduleReq.month;
        Iterator<Schedule> schedules = scheduleList.results.iterator();
        Schedule schedule = new Schedule();
        List<ScheduleDay> scheduleDays = new ArrayList<ScheduleDay>();
        while(schedules.hasNext()){
            schedule = schedules.next();
            ScheduleDay scheduleDay = new ScheduleDay();
            scheduleDay.date = schedule.day;
            List<DayMetadata> metadata = new ArrayList<DayMetadata>();
            for(SubjectMetadata subMetaData : schedule.metadata) {
                List<EntityCount> entityDetails = new ArrayList<EntityCount>();
                DayMetadata dayMetaData = new DayMetadata();
                dayMetaData.id = subMetaData.id;
                dayMetaData.name = subMetaData.name;
                for(EntityDetails details : subMetaData.details){
                    EntityCount entityDetail = new EntityCount();
                    entityDetail.type = details.type;
                    entityDetail.count = details.ids.size();
                    entityDetails.add(entityDetail);
                }
                dayMetaData.details = entityDetails;
                metadata.add(dayMetaData);
            }
            scheduleDay.metadata = metadata;
            scheduleDays.add(scheduleDay);
        }
        res.days = scheduleDays;
        return res;
    }

    public static GetDayScheduleRes getDaySchedule(GetScheduleReq getScheduleReq) throws VedantuException {
        // TODO Auto-generated method stub
        if(getScheduleReq.day == 0){
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "Missing param DAY");
        }
        GetDayScheduleRes res = new GetDayScheduleRes();
        Schedule schedule = ScheduleDAO.INSTANCE.getScheduleByDate(
                getScheduleReq.orgId, getScheduleReq.sectionId, getScheduleReq.month,
                getScheduleReq.day);
        for(SubjectMetadata metadata : schedule.metadata){
            SubMetadata subMetadata = new SubMetadata();
            subMetadata.id = metadata.id;
            subMetadata.subName = metadata.name;
            for(EntityDetails detail : metadata.details){
                EntityTypeData det = new EntityTypeData();
                det.type = detail.type;
                for(String cmdsId : detail.cmdsIds){
                    EntityList list = new EntityList();
                    list.id = getGlobalId(detail.type, cmdsId);
                    if(list.id == null){
                        throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND, "Entity not found");
                    }
                    list.cmdsId = cmdsId;
                    String name = getName(list.id, detail.type);
                    list.name = name;
                    if(detail.type.equals("TEST")){
                        list.attempted = UserEntityAttemptDAO.INSTANCE.getAttempt(getScheduleReq.userId, EntityType.TEST, list.id) != null ? true : false;
                    }
                    det.contents.add(list);
                }
                subMetadata.details.add(det);
            }
            res.metadata.add(subMetadata);
        }
        return res;
    }

    private static String getName(String id, String type) {
        if(type.equals(EntityType.TEST.name())){
            Test test = TestDAO.INSTANCE.getById(id);
            if(test == null){
                return null;
            }else{
                return test.name;
            }
        }else if(type.equals(EntityType.MODULE.name())){
            Module module = ModuleDAO.INSTANCE.getById(id);
            if(module == null){
                return null;
            }else{
                return module.name;
            }
        }else if(type.equals(EntityType.VIDEO.name())){
            Video video = VideoDAO.INSTANCE.getById(id);
            if(video == null){
                return null;
            }else{
                return video.name;
            }
        }else if(type.equals(EntityType.DOCUMENT.name())){
            Document doc = DocumentDAO.INSTANCE.getById(id);
            if(doc == null){
                return null;
            }else{
                return doc.name;
            }
        }else if(type.equals(EntityType.ASSIGNMENT.name())){
            Assignment assignment = AssignmentDAO.INSTANCE.getById(id);
            if(assignment == null){
                return null;
            }else{
                return assignment.name;
            }
        }else{
            return null;
        }
    }

    public static SaveScheduleRes addSchedule(AddScheduleReq addScheduleReq) throws VedantuException {
        SaveScheduleRes response = new SaveScheduleRes();
        if(addScheduleReq.entityList.isEmpty()){
            throw new VedantuException(VedantuErrorCode.NO_CONTENT_FOUND, "No content selected");
        }
        List<String> cmdsTestIds = new ArrayList<String>();
        List<String> cmdsModuleIds = new ArrayList<String>();
        List<String> cmdsDocumentIds = new ArrayList<String>();
        List<String> cmdsVideoIds = new ArrayList<String>();
        List<String> cmdsAssignmentIds = new ArrayList<String>();

        List<String> testIds = new ArrayList<String>();
        List<String> moduleIds = new ArrayList<String>();
        List<String> documentIds = new ArrayList<String>();
        List<String> videoIds = new ArrayList<String>();
        List<String> assignmentIds = new ArrayList<String>();
        for(SrcEntity entity : addScheduleReq.entityList){
            if(!StringUtils.isEmpty(entity.id)){
                if(entity.type == EntityType.CMDSTEST){
                    cmdsTestIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSTEST, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        testIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else if(entity.type == EntityType.CMDSMODULE){
                    cmdsModuleIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSMODULE, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        moduleIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else if(entity.type == EntityType.CMDSVIDEO){
                    cmdsVideoIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSVIDEO, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        videoIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else if(entity.type == EntityType.CMDSDOCUMENT){
                    cmdsDocumentIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSDOCUMENT, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        documentIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else if(entity.type == EntityType.CMDSASSIGNMENT){
                    cmdsAssignmentIds.add(entity.id);
                    String id = getGlobalId(new SrcEntity(EntityType.CMDSASSIGNMENT, entity.id));
                    if(!StringUtils.isEmpty(id)){
                        assignmentIds.add(id);
                    }else{
                        throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "One or More than one content is not published");
                    }
                }else{
                    throw new VedantuException(VedantuErrorCode.NOT_ALLOWED, "One or More than one content is not supported");
                }
            }else{
                throw new VedantuException(VedantuErrorCode.NO_CONTENT_FOUND, "One or More than one content id is not found");
            }
        }
        Schedule schedule = ScheduleDAO.INSTANCE.getScheduleByDate(addScheduleReq.orgId, addScheduleReq.sectionId, addScheduleReq.month, addScheduleReq.day);
        if(schedule != null){
            List<String> boardIds = schedule.boardIds;
            if(boardIds.contains(addScheduleReq.boardId)){
                SubjectMetadata subMetaData = null;
                for(SubjectMetadata singleMetaData : schedule.metadata){
                    if(addScheduleReq.boardId.equals(singleMetaData.id)){
                        subMetaData = singleMetaData;
                        break;
                    }
                }
                if(subMetaData == null){
                    throw new VedantuException(VedantuErrorCode.NO_SUBJECT_FOUND, "Subject not found");
                }
                List<EntityDetails> details = subMetaData.details;
                if(details == null){
                    details = new ArrayList<EntityDetails>();
                }
                if(!cmdsTestIds.isEmpty() && !testIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.TEST.name())){
                            Iterator<String> cmdstestIds = cmdsTestIds.iterator();
                            while(cmdstestIds.hasNext()){
                                String cmdsId = cmdstestIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSTEST, cmdsId));
                                    testIds.remove(globalId);
                                    cmdstestIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsTestIds);
                            detail.ids.addAll(testIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.TEST.name();
                        detail.cmdsIds = cmdsTestIds;
                        detail.ids = testIds;
                        details.add(detail);
                    }
                }
                if(!cmdsVideoIds.isEmpty() && !videoIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.VIDEO.name())){
                            Iterator<String> cmdsvideoIds = cmdsVideoIds.iterator();
                            while(cmdsvideoIds.hasNext()){
                                String cmdsId = cmdsvideoIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSVIDEO, cmdsId));
                                    videoIds.remove(globalId);
                                    cmdsvideoIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsVideoIds);
                            detail.ids.addAll(videoIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.VIDEO.name();
                        detail.cmdsIds = cmdsVideoIds;
                        detail.ids = videoIds;
                        details.add(detail);
                    }
                }
                if(!cmdsModuleIds.isEmpty() && !moduleIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.MODULE.name())){
                            Iterator<String> cmdsmoduleIds = cmdsModuleIds.iterator();
                            while(cmdsmoduleIds.hasNext()){
                                String cmdsId = cmdsmoduleIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSMODULE, cmdsId));
                                    moduleIds.remove(globalId);
                                    cmdsmoduleIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsModuleIds);
                            detail.ids.addAll(moduleIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.MODULE.name();
                        detail.cmdsIds = cmdsModuleIds;
                        detail.ids = moduleIds;
                        details.add(detail);
                    }
                }
                if(!cmdsAssignmentIds.isEmpty() && !assignmentIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.ASSIGNMENT.name())){
                            Iterator<String> cmdsassignmentIds = cmdsAssignmentIds.iterator();
                            while(cmdsassignmentIds.hasNext()){
                                String cmdsId = cmdsassignmentIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSASSIGNMENT, cmdsId));
                                    assignmentIds.remove(globalId);
                                    cmdsassignmentIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsAssignmentIds);
                            detail.ids.addAll(assignmentIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.ASSIGNMENT.name();
                        detail.cmdsIds = cmdsAssignmentIds;
                        detail.ids = assignmentIds;
                        details.add(detail);
                    }
                }
                if(!cmdsDocumentIds.isEmpty() && !documentIds.isEmpty()){
                    boolean flag = false;
                    for(EntityDetails detail : details){
                        if(detail.type.equals(EntityType.DOCUMENT.name())){
                            Iterator<String> cmdsdocumentIds = cmdsDocumentIds.iterator();
                            while(cmdsdocumentIds.hasNext()){
                                String cmdsId = cmdsdocumentIds.next();
                                if(detail.cmdsIds.contains(cmdsId)){
                                    String globalId = getGlobalId(new SrcEntity(EntityType.CMDSDOCUMENT, cmdsId));
                                    documentIds.remove(globalId);
                                    cmdsdocumentIds.remove();
                                }
                            }
                            detail.cmdsIds.addAll(cmdsDocumentIds);
                            detail.ids.addAll(documentIds);
                            flag = true;
                            break;
                        }
                    }
                    if(!flag){
                        EntityDetails detail = new EntityDetails();
                        detail.type = EntityType.DOCUMENT.name();
                        detail.cmdsIds = cmdsDocumentIds;
                        detail.ids = documentIds;
                        details.add(detail);
                    }
                }
                subMetaData.details = details;
                for(SubjectMetadata singleMetaData : schedule.metadata){
                    if(addScheduleReq.boardId.equals(singleMetaData.id)){
                        singleMetaData = subMetaData;
                        break;
                    }
                }
                ScheduleDAO.INSTANCE.save(schedule);
                response.saved = true;
            }else{
                schedule.boardIds.add(addScheduleReq.boardId);
                SubjectMetadata subMetaData = new SubjectMetadata();
                subMetaData.id = addScheduleReq.boardId;
                subMetaData.name = addScheduleReq.boardName;
                List<EntityDetails> details = new ArrayList<EntityDetails>();
                if(!cmdsTestIds.isEmpty() && !testIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.TEST.name();
                    detail.cmdsIds = cmdsTestIds;
                    detail.ids = testIds;
                    details.add(detail);
                }
                if(!cmdsVideoIds.isEmpty() && !videoIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.VIDEO.name();
                    detail.cmdsIds = cmdsVideoIds;
                    detail.ids = videoIds;
                    details.add(detail);
                }
                if(!cmdsModuleIds.isEmpty() && !moduleIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.MODULE.name();
                    detail.cmdsIds = cmdsModuleIds;
                    detail.ids = moduleIds;
                    details.add(detail);
                }
                if(!cmdsAssignmentIds.isEmpty() && !assignmentIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.ASSIGNMENT.name();
                    detail.cmdsIds = cmdsAssignmentIds;
                    detail.ids = assignmentIds;
                    details.add(detail);
                }
                if(!cmdsDocumentIds.isEmpty() && !documentIds.isEmpty()){
                    EntityDetails detail = new EntityDetails();
                    detail.type = EntityType.DOCUMENT.name();
                    detail.cmdsIds = cmdsDocumentIds;
                    detail.ids = documentIds;
                    details.add(detail);
                }
                subMetaData.details = details;
                schedule.metadata.add(subMetaData);
                ScheduleDAO.INSTANCE.save(schedule);
                response.saved = true;
            }
        }else{
            schedule = new Schedule();
            schedule.orgId = addScheduleReq.orgId;
            schedule.programId = addScheduleReq.programId;
            schedule.centerId = addScheduleReq.centerId;
            schedule.sectionId = addScheduleReq.sectionId;
            schedule.month = addScheduleReq.month;
            schedule.day = addScheduleReq.day;
            schedule.boardIds.add(addScheduleReq.boardId);
            SubjectMetadata subMetaData = new SubjectMetadata();
            subMetaData.id = addScheduleReq.boardId;
            subMetaData.name = addScheduleReq.boardName;
            List<EntityDetails> details = new ArrayList<EntityDetails>();
            if(!cmdsTestIds.isEmpty() && !testIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.TEST.name();
                detail.cmdsIds = cmdsTestIds;
                detail.ids = testIds;
                details.add(detail);
            }
            if(!cmdsVideoIds.isEmpty() && !videoIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.VIDEO.name();
                detail.cmdsIds = cmdsVideoIds;
                detail.ids = videoIds;
                details.add(detail);
            }
            if(!cmdsModuleIds.isEmpty() && !moduleIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.MODULE.name();
                detail.cmdsIds = cmdsModuleIds;
                detail.ids = moduleIds;
                details.add(detail);
            }
            if(!cmdsAssignmentIds.isEmpty() && !assignmentIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.ASSIGNMENT.name();
                detail.cmdsIds = cmdsAssignmentIds;
                detail.ids = assignmentIds;
                details.add(detail);
            }
            if(!cmdsDocumentIds.isEmpty() && !documentIds.isEmpty()){
                EntityDetails detail = new EntityDetails();
                detail.type = EntityType.DOCUMENT.name();
                detail.cmdsIds = cmdsDocumentIds;
                detail.ids = documentIds;
                details.add(detail);
            }
            subMetaData.details = details;
            schedule.metadata.add(subMetaData);
            ScheduleDAO.INSTANCE.save(schedule);
            response.saved = true;
        }
        return response;
    }

    public static String getGlobalId(String type, String cmdsId){
        if(type.equals("TEST")){
            return getGlobalId(new SrcEntity(EntityType.CMDSTEST, cmdsId));
        }else if(type.equals("VIDEO")){
            return getGlobalId(new SrcEntity(EntityType.CMDSVIDEO, cmdsId));
        }else if(type.equals("MODULE")){
            return getGlobalId(new SrcEntity(EntityType.CMDSMODULE, cmdsId));
        }else if(type.equals("DOCUMENT")){
            return getGlobalId(new SrcEntity(EntityType.CMDSDOCUMENT, cmdsId));
        }else{
            return null;
        }
    }

    public static String getGlobalId(SrcEntity entity){
        if(entity.type == EntityType.CMDSTEST){
            Test test = TestDAO.INSTANCE.getByCMDSTestId(entity.id);
            if(test == null){
                return null;
            }else{
                return test._getStringId();
            }
        }else if(entity.type == EntityType.CMDSMODULE){
            Module module = ModuleDAO.INSTANCE.getByCMDSModuleId(entity.id);
            if(module == null){
                return null;
            }else{
                return module._getStringId();
            }
        }else if(entity.type == EntityType.CMDSVIDEO){
            Video video = VideoDAO.INSTANCE.getByCMDSVideoId(entity.id);
            if(video == null){
                return null;
            }else{
                return video._getStringId();
            }
        }else if(entity.type == EntityType.CMDSDOCUMENT){
            Document doc = DocumentDAO.INSTANCE.getByCMDSDocId(entity.id);
            if(doc == null){
                return null;
            }else{
                return doc._getStringId();
            }
        }else if(entity.type == EntityType.CMDSASSIGNMENT){
            Assignment assignment = AssignmentDAO.INSTANCE.getByCMDSAssignmentId(entity.id);
            if(assignment == null){
                return null;
            }else{
                return assignment._getStringId();
            }
        }else{
            return null;
        }
    }

    public static SaveScheduleRes removeDaySchedule(GetScheduleReq removeScheduleReq) throws VedantuException {
        if(removeScheduleReq.day == 0){
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "Missing param DAY");
        }
        SaveScheduleRes res = new SaveScheduleRes();
        res.saved = ScheduleDAO.INSTANCE.removeScheduleByDate(
                removeScheduleReq.orgId, removeScheduleReq.sectionId, removeScheduleReq.month,
                removeScheduleReq.day);
        return res;
    }

    @SuppressWarnings("unchecked")
    public static SaveScheduleRes removeSchedule(RemoveScheduleReq removeScheduleReq) throws VedantuException {
        if(removeScheduleReq.day == 0){
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "Missing param DAY");
        }
        SaveScheduleRes res = new SaveScheduleRes();
        Schedule schedule = ScheduleDAO.INSTANCE.getScheduleByDate(
                removeScheduleReq.orgId, removeScheduleReq.sectionId, removeScheduleReq.month,
                removeScheduleReq.day);
        Iterator<SubjectMetadata> metadata = schedule.metadata.iterator();
        List<SubjectMetadata> newMetadata = new ArrayList<SubjectMetadata>();
        while(metadata.hasNext()){
            SubjectMetadata subMetaData = metadata.next();
            if(subMetaData.id.equals(removeScheduleReq.boardId)){
                Iterator<EntityDetails> details = subMetaData.details.iterator();
                List<EntityDetails> newDetails = new ArrayList<EntityDetails>();
                while(details.hasNext()){
                    EntityDetails detail = details.next();
                    if(detail.type.equals(removeScheduleReq.entityType)){
                        if(detail.ids.contains(removeScheduleReq.entityId) && detail.cmdsIds.contains(removeScheduleReq.entityCmdsId)){
                            detail.ids.remove(removeScheduleReq.entityId);
                            detail.cmdsIds.remove(removeScheduleReq.entityCmdsId);
                        }
                        if(detail.cmdsIds.size() == 0 && detail.ids.size() == 0){
                            details.remove();
                        }else{
                            newDetails.add(detail);
                        }
                    }else{
                        newDetails.add(detail);
                    }
                }
                subMetaData.details = newDetails;
                if(subMetaData.details.size() == 0){
                    metadata.remove();
                    if(schedule.boardIds.contains(subMetaData.id)){
                        schedule.boardIds.remove(subMetaData.id);
                    }
                }else{
                    newMetadata.add(subMetaData);
                }
            }else{
                newMetadata.add(subMetaData);
            }
        }
        schedule.metadata = newMetadata;
        ScheduleDAO.INSTANCE.save(schedule);
        res.saved = true;
        return res;
    }
}
