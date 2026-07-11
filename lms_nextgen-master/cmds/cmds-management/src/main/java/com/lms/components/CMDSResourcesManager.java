package com.lms.components;

import com.google.gson.Gson;
import com.lms.board.components.BoardManager;
import com.lms.board.model.Board;
import com.lms.board.model.GranteeOrgProgram;
import com.lms.board.repo.BoardRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.fs.exception.FileStoreException;
import com.lms.common.fs.handlers.IFileSystemHandler;
import com.lms.common.fs.handlers.S3Handler;
import com.lms.common.fs.handlers.responce.SignUploadFileRes;
import com.lms.common.utils.FileUtils;
import com.lms.common.vedantu.Repo.CounterRepo;
import com.lms.common.vedantu.Repo.EntityOperationStatusRepo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.entity.storage.*;
import com.lms.common.vedantu.enums.BoardType;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.OperationType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.model.Counter;
import com.lms.common.vedantu.mongo.EntityOperationStatus;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.content.CMDSFolderInfo;
import com.lms.enums.*;
import com.lms.maintenance.managers.ShareQuestionsThread;
import com.lms.models.*;
import com.lms.models.event.search.details.CMDSResourceDetails;
import com.lms.pojo.BoardMappings;
import com.lms.pojos.BoardInfo;
import com.lms.pojos.OrgDetails;
import com.lms.pojos.SharedBoardInfo;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.*;
import com.lms.pojos.responses.GetResourcesRes;
import com.lms.repo.*;
import com.lms.repository.BoardMappingRepo;
import com.lms.repository.CMDSQuestionRepo;
import com.lms.repository.OrganizationRepo;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class CMDSResourcesManager {
	private static final Logger logger = LoggerFactory.getLogger(CMDSResourcesManager.class);
	@Autowired
	private BoardManager boardManager;
	@Autowired
	private CMDSContentManager cmdsContentManager;
	@Autowired
	private CMDSFolderRepo cmdsFolderRepo;
	@Value("${learnpedia.id}")
	private String learnPediaId;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private CMDSQuestionRepo cmdsQuestionRepo;
	@Autowired
	private CounterRepo counterRepo;
	@Autowired
	private CMDSContentLinkRepo cmdsContentLinkRepo;
	@Autowired
	private CMDSFileStorage cmdsFileStorage;
	@Autowired
	private CMDSVideoStorage cmdsVideoStorage;
	@Autowired
	private CMDSQuestionSetEntityFileStorage cmdsQuestionSetEntityFileStorage;
	@Autowired
	private S3Handler s3Handler;
	@Autowired
	private CMDSVideoRepo cmdsVideoRepo;
	@Autowired
	private CMDSDocumentRepo cmdsDocumentRepo;
	@Autowired
	private CMDSFileRepo cmdsFileRepo;
	@Autowired
	private OrganizationRepo organizationRepo;
	@Autowired
	private BoardMappingRepo boardMappingRepo;
	@Autowired
	private BoardRepo boardRepo;
	@Autowired
	private EntityOperationStatusRepo entityOperationStatusRepo;

	public GetResourcesRes getResources(GetResourcesReq request) {

		logger.debug("Getting resources in folder Id " + request.folderId
				+ System.currentTimeMillis());
		GetResourcesRes response = new GetResourcesRes();
		List<ModelBasicInfo> basicInfos = new ArrayList<ModelBasicInfo>();
		try {

			SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);

            //Get all the orgIds that gave access to the current organization
            AtomicLong totalProgramHits = new AtomicLong(0L);
            List<GranteeOrgProgram> granteeOrgPrograms = boardManager.getGranteeOrgPrograms(request.orgId, null, totalProgramHits);
            List<String> grantedOrgs = new ArrayList<String>();
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
                grantedOrgs.add(granteeOrgProgram.providerOrgId);
            }
            Query query = new Query();
            Criteria criteria = new Criteria();
            cmdsContentManager.buildSearchQuery(SearchResultType.ALL, request.userId,
                    contentSrc, null, null, null, request.query, null, grantedOrgs, query, criteria);
            cmdsContentManager.addBoardAndTargetFilter(request.brdIds, false, query, criteria);
            criteria.and("target.id").is(request.folderId);
            if (request.diffculty != null && request.diffculty != Difficulty.UNKNOWN) {
                criteria.and(ConstantsGlobal.DIFFICULTY).is(request.diffculty.name());
            }

            if (CollectionUtils.isNotEmpty(request.includes)) {
                criteria.and("source.type").in(request.includes);
                criteria.and("content.type").in(request.includes);

            }
            if (CollectionUtils.isNotEmpty(request.excludes)) {
                criteria.and("source.type").in(request.excludes);
                criteria.and("content.type").in(request.excludes);
            }

          /*  HasChildQueryBuilder folderQueryBuilder = QueryBuilders.hasChildQuery(
                    CmdsContentLinkType.ADDED.name().toLowerCase(), folderQuery);

            resourceQuery.must(folderQueryBuilder);*/
          /*  SearchResponse searchResults = ElasticSearchUtils.getSearchResponse(query,
                    request.orderBy, request.sortOrder, request.start, request.size,
                    EntityType.CMDSRESOURCE.getIndexName(), EntityType.CMDSRESOURCE.getIndexType()
                            .toLowerCase(), null, false, (AbstractFacetBuilder[]) null);

            List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();

            response.totalHits = AbstractCMDSContentManager.getBasicInfoFromESSearch(searchResults,
                    details);*/

            Gson gson = new Gson();
            //LOGGER.debug(":::::: Details : "+gson.toJson(details, (new TypeToken<List<CMDSResourceDetails>>(){}).getType()));

            //List<CMDSContentLink> links = new ArrayList<CMDSContentLink>();
            SrcEntity folderEntity = new SrcEntity(EntityType.FOLDER, request.folderId);
            List<CMDSContentLink> links = cmdsContentManager.getCmdsContentLinks(null, folderEntity, CmdsContentLinkType.ADDED, null, 0, 1);
            basicInfos = cmdsContentManager.getBasicInfoFromLinks(links, basicInfos);
            response.list.addAll(basicInfos);
            response.folderInfo = cmdsFolderRepo.findById(request.folderId).get().toBasicInfo();
            annotateParentInfo((CMDSFolderInfo) response.folderInfo,
                    request.folderId);

        } catch (Exception exception) {
            logger.debug(" Error", exception);
			logger.debug(" Error", exception);
			throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
		}
		logger.debug("Received resources " + request.folderId + System.currentTimeMillis());

		// check in
		return response;

	}

	public static String getCounterName(CMDSContentLink link, String suffix) {

		logger.debug("Getting counter name for" + link.target + suffix + link.linkType);
		return getCounterName(link.target, link.linkType, suffix);
	}

	protected static String getCounterName(SrcEntity target, CmdsContentLinkType linkType,
										   String suffix) {

		logger.debug("Getting counter name for" + target + suffix + linkType);
		return target.type.name().toLowerCase() + "_" + target.id + "_"
				+ linkType.name().toLowerCase() + "_" + suffix.trim().toLowerCase();
	}

	public boolean annotateParentInfo(CMDSFolderInfo folderInfo, String folderId) {

		if (folderInfo == null || folderId == null || !folderInfo.id.equalsIgnoreCase(folderId)) {
			return false;
		}
		Optional<CMDSFolder> folderOptional = cmdsFolderRepo.findById(folderId);
		if (!folderOptional.isPresent()) {
			return false;
		}
		CMDSFolder folder = folderOptional.get();
		if (CollectionUtils.isNotEmpty(folder.parentSources)) {
			if (folderInfo.parents == null) {
				folderInfo.parents = new ArrayList<CMDSFolderInfo>();
			}
			for (String parent : folder.parentSources) {
				CMDSFolderInfo info = (CMDSFolderInfo) cmdsFolderRepo.findById(parent).get().toBasicInfo();
				folderInfo.parents.add(0, info);
			}
		}
		return true;
	}

	public GetResourcesRes getQuestionsCount(GetResourcesReq request) {
		logger.info(":::::::::   Inside Get Questions Count   ::::::::::");

		GetResourcesRes resp = new GetResourcesRes();
		resp.totalHits = getQuestionsCount("NOT_PARA", request);
		resp.paraHits = getQuestionsCount("PARA", request);
		return resp;
	}

	public int getQuestionsCount(String type, GetResourcesReq request) {
		int hits = 0;

		SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and(ConstantsGlobal.CONTENT_SRC + "." + ConstantsGlobal.ID).is(contentSrc.id);
		criteria.and(ConstantsGlobal.CONTENT_SRC + "." + ConstantsGlobal.TYPE).is(contentSrc.type.name());
		if (!request.orgId.equals(learnPediaId)) {
			if (!includeLearnpediaQuestions(learnPediaId, request.orgId)) {
				criteria.and("scope").is("ORG");
			}
		}

		// oring all boards assuming boards are of only one level
		cmdsContentManager.addBoardAndTargetFilter(request.brdIds, false, query, criteria);
		if (type.equals("PARA")) {
			criteria.and("type").in("text");
		} else if (type.equals("NOT_PARA")) {
			criteria.and("type").in("scq", "mcq", "numeric", "matrix");
		} else {
			criteria.and("type").in("scq", "mcq", "numeric", "text", "matrix");
		}
		query.addCriteria(criteria);
           /* SearchResponse questionsResponse = ElasticSearchUtils.getSearchResponse(
	                query, "", "", 0, 0, EntityType.CMDSQUESTION.getIndexName(),
	                EntityType.CMDSQUESTION.getIndexType().toLowerCase(), null, false,
	                (AbstractFacetBuilder[]) null);*/
		long count = mongoTemplate.count(query, CMDSQuestion.class);
		if (count == 0) {
			logger.error("empty search response for questions query : " + type);
			return 0;
		}
		hits = (int) count;
		return hits;
	}

	public boolean includeLearnpediaQuestions(String learnpediaId, String orgId) {
		// TODO Auto-generated method stub
		BoardMapping mapping = getBySharedToOrgId(learnpediaId, orgId);
		if (mapping != null) {
			return mapping.publish;
		}
		return false;
	}

	public BoardMapping getBySharedToOrgId(String parentOrgId, String sharedToOrgId) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		if (!parentOrgId.equals("N.A")) {
			criteria.and("parentOrgId").is(parentOrgId);
		}
		criteria.and("sharedToOrgId").is(sharedToOrgId);
		query.addCriteria(criteria);
		return mongoTemplate.findOne(query, BoardMapping.class);
	}

	public GetResourcesRes getQuestions(GetResourcesReq request) {

		logger.debug("Inside getQuestions");

		GetResourcesRes response = new GetResourcesRes();
		String type = "";

		List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();
		List<ModelBasicInfo> basicInfos = new ArrayList<ModelBasicInfo>();

		String folderId = getRootFolder(request.orgId)._getStringId();

		if (request.quesType.equals("PARA_QUES")) {
			details = getParagraphQuestions(request);
			response.totalHits = details.size();
		} else {
			SrcEntity contentSrc = new SrcEntity(EntityType.ORGANIZATION, request.orgId);
			Query query = new Query();
			Criteria criteria = new Criteria();
			criteria.and(ConstantsGlobal.CONTENT_SRC + "." + ConstantsGlobal.ID).is(contentSrc.id);
			criteria.and(ConstantsGlobal.CONTENT_SRC + "." + ConstantsGlobal.TYPE).is(contentSrc.type.name());
			if (!StringUtils.isEmpty(request.includeDifficulty)) {
				criteria.and(ConstantsGlobal.DIFFICULTY).is(request.includeDifficulty);
			}
			if (!request.orgId.equals(learnPediaId)) {
				if (!includeLearnpediaQuestions(learnPediaId, request.orgId)) {
					criteria.and("scope").is("ORG");
				}
			}

			cmdsContentManager.addBoardAndTargetFilter(request.brdIds, false, query, criteria);

			if (request.quesType.equals("PARA")) {
				criteria.and("type").in("text");
				type = "NOT_PARA";
			} else if (request.quesType.equals("NOT_PARA")) {
				if (!StringUtils.isEmpty(request.includeTypes)) {
					criteria.and(ConstantsGlobal.TYPE).in(request.includeTypes);

				} else {
					criteria.and("type").in("scq", "mcq", "numeric", "matrix");
				}
				type = "PARA";
			} else {
				criteria.and("type").in("scq", "mcq", "numeric", "text", "matrix");
			}
			query.addCriteria(criteria);
			query.skip(request.start).limit(request.size);
			if (request.sortOrder != null) {
				//query.with(pageable)
			}
			List<CMDSQuestion> cmdsQuestions = mongoTemplate.find(query, CMDSQuestion.class);
				/*SearchResponse questionsResponse = ElasticSearchUtils.getSearchResponse(query, request.orderBy,
						request.sortOrder, request.start, request.size, EntityType.CMDSQUESTION.getIndexName(),
						EntityType.CMDSQUESTION.getIndexType().toLowerCase(), null, false,
						(AbstractFacetBuilder[]) null);

				response.totalHits = AbstractCMDSContentManager.getBasicInfoFromESSearch(questionsResponse, details,
						"CMDSQUESTION");*/
			if (cmdsQuestions != null && !cmdsQuestions.isEmpty()) {
				List<CMDSResourceDetails> detailsList = cmdsQuestions.stream().map(cmdsQuestion -> {
					CMDSResourceDetails cmdsResourceDetails = new CMDSResourceDetails();
					cmdsResourceDetails.content = cmdsQuestion.contentSrc;
					return cmdsResourceDetails;
				}).collect(Collectors.toList());
				details.addAll(detailsList);
				response.totalHits = details.size();
			} else {
				response.totalHits = 0;
			}


		}
		if (request.quesType.equals("PARA") || request.quesType.equals("NOT_PARA")) {
			response.otherHits = getQuestionsCount(type, request);
			response.otherType = type;
		} else {
			response.nonParaHits = getQuestionsCount("NOT_PARA", request);
			response.paraHits = getQuestionsCount("PARA", request);
		}

		List<CMDSContentLink> links = new ArrayList<CMDSContentLink>();

		CMDSContentLink link = null;
		for (CMDSResourceDetails detail : details) {
			List<CMDSContentLink> testLinks = cmdsContentManager.getCmdsContentLinks(detail.content, null, null,
					null, null, 0, 1, null, null);
			if (CollectionUtils.isNotEmpty(testLinks)) {
				link = testLinks.get(0);
				link.target.type = EntityType.FOLDER;
				link.target.id = folderId;
				links.add(link);
			} else {
				SrcEntity target = new SrcEntity(EntityType.FOLDER, folderId);
				CMDSContentLink contentLinkage = new CMDSContentLink(target, detail.content);

				contentLinkage.userId = detail.userId;
				contentLinkage.linkType = CmdsContentLinkType.ADDED;
				// this is ever increasing number for all positions
				contentLinkage.position = getNextSequence(
						"cmdscontentlinks",
						getCounterName(contentLinkage, CMDSContentLink.POSITION), 1);
				logger.debug(" Created linkage : " + contentLinkage);
				cmdsContentLinkRepo.save(contentLinkage);
				link = contentLinkage;
				link.target = target;
				links.add(link);
			}

		}
		basicInfos = cmdsContentManager.getBasicInfoFromLinks(links, basicInfos);
		response.list.addAll(basicInfos);
		Optional<CMDSFolder> cmdsFolderOptional = cmdsFolderRepo.findById(request.folderId);
		response.folderInfo = cmdsFolderOptional.get().toBasicInfo();
		annotateParentInfo((CMDSFolderInfo) response.folderInfo, request.folderId);

		return response;

	}

	public CMDSFolder getRootFolder(String orgId) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("organizationId").is(orgId);
		criteria.and("isRoot").is(Boolean.TRUE);
		query.addCriteria(criteria);
		query.fields().exclude("parent");
		return mongoTemplate.findOne(query, CMDSFolder.class);
	}

	public List<CMDSResourceDetails> getParagraphQuestions(GetResourcesReq request) {
		CMDSQuestion paragraphQuestion = new CMDSQuestion();
		List<CMDSResourceDetails> details = new ArrayList<CMDSResourceDetails>();
		try {
			paragraphQuestion = getQuestionById(request.paraId);
		} catch (VedantuException e) {
			logger.debug("Exception while retreiveing data : " + e.getMessage());
		}
		List<String> paraQuesIds = paragraphQuestion.paraIds;
		if (paraQuesIds.size() > 0) {
			return cmdsContentManager.getBasicInfoOFParaQuestion(paraQuesIds);
		} else {
			return details;
		}
	}

	public CMDSQuestion getQuestionById(String questionId) throws VedantuException {

		Optional<CMDSQuestion> questionOptional = cmdsQuestionRepo.findById(questionId);
		if (!questionOptional.isPresent()) {
			logger.error("cannot find cmds question for _id: " + questionId);
			throw new VedantuException(VedantuErrorCode.CMDS_QUESTION_NOT_FOUND);
		}

		return questionOptional.get();
	}

	public long getNextSequence(String collectionName, String field, int byValue) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("collection").is(collectionName);
		criteria.and("field").is(field);
		Counter counter = mongoTemplate.findOne(query, Counter.class);
		counter.setValue(counter.getValue() + byValue);
		counterRepo.save(counter);
		return counter.value;

	}

	public CreateFolderRes createFolder(CreateFolderReq request) {


		try {
			if (StringUtils.isEmpty(request.parentFolderId)) {
				CMDSFolder folder = getRootFolder(request.userId, request.orgId);
				request.parentFolderId = folder._getStringId();
			}
			logger.debug("ParentFolder " + request.parentFolderId);
			CMDSFolder createdFolder = createFolder(request.parentFolderId,
					request.userId, request.orgId, request.name);

			if (createdFolder != null && StringUtils.isEmpty(request.parentFolderId)) {

				SrcEntity folderEntity = new SrcEntity(EntityType.FOLDER,
						createdFolder._getStringId());
	                /*String parentLiveIndexedId = AbstractCMDSContentManager.addAsCMDSResource(
	                        folderEntity, EventActionType.ADD, createdFolder);*/

				addToFolder(request.orgId, request.userId, new SrcEntity(
								EntityType.FOLDER, createdFolder._getStringId()), request.parentFolderId,
						CmdsContentLinkType.ADDED, null);

			}
			CreateFolderRes response = new CreateFolderRes();
			response.id = createdFolder._getStringId();
			response.parent = createdFolder.parent;
			response.name = createdFolder.name;
			response.createdOn = createdFolder.timeCreated;
			logger.debug(" Added folder " + response.id);
			return response;

		} catch (VedantuException exception) {
			throw exception;
		} catch (Exception exception) {
			logger.error(" Error", exception);

			throw new VedantuException(VedantuErrorCode.SERVICE_ERROR, exception);
		}


	}

	public synchronized CMDSFolder getRootFolder(String userId, String orgId) throws VedantuException {

		CMDSFolder rootFolder = getRootFolder(orgId);

		if (rootFolder == null) {
			logger.debug("Root Folder" + rootFolder);

			rootFolder = createFolder(null, userId, orgId, "/");
		}

		return rootFolder;
	}

	public CMDSFolder createFolder(String parentId, String userId, String organizationId,
								   String name) throws VedantuException {

		List<String> parents = null;

		if (!StringUtils.isEmpty(parentId)) {

			CMDSFolder parentFolder = findById(organizationId, parentId);

			if (parentFolder == null) {
				throw new VedantuException(VedantuErrorCode.PARENT_DIRECTORY_NOT_FOUND);
			}
			logger.debug("Found parent directory:" + parentFolder);
			if (parentFolder != null) {
				parents = parentFolder.parentSources;
				if (CollectionUtils.isEmpty(parents)) {
					parents = new ArrayList<String>();
				}
				parents.add(0, parentFolder._getStringId());
			}
		}

		if (checkForSimilarNameDirectory(organizationId, parentId, name)) {
			throw new VedantuException(VedantuErrorCode.FOLDER_ALREADY_EXISTS);
		}

		CMDSFolder directory = new CMDSFolder();
		directory.setName(name);
		directory.parentSources = parents;
		directory.organizationId = organizationId;
		directory.parent = parentId;
		directory.userId = userId;
		if (CollectionUtils.isEmpty(parents) && StringUtils.isEmpty(parentId)) {
			directory.isRoot = true;
		}

		cmdsFolderRepo.save(directory);

		logger.debug("default folder created");
		return directory;
	}

	public CMDSFolder findById(String orgId, String folderId) {

		CMDSFolder folder = cmdsFolderRepo.findByIdAndOrganizationId(folderId, orgId);

		return folder;
	}

	public boolean checkForSimilarNameDirectory(String orgId, String parentId, String name) {

		CMDSFolder directory = cmdsFolderRepo.findByOrganizationIdAndParentAndName(orgId, parentId, name);

		return directory != null;
	}

	public boolean addToFolder(String orgId, String userId, SrcEntity content,
							   String folderId, CmdsContentLinkType linkType, String parentESId)
			throws VedantuException {

		CMDSFolder qrFolder = findById(orgId, folderId);

		if (qrFolder != null) {
			SrcEntity folderEntity = new SrcEntity(EntityType.FOLDER, qrFolder._getStringId());
			CMDSContentLink linkage = addLink(content, folderEntity,
					linkType, userId, false);

			logger.debug(" Created linkage : " + linkage);

	            /*CMDSContentLinkDetails libraryContentLinkDetails = new CMDSContentLinkDetails(
	                    linkage._getStringId(), userId, content, folderEntity, linkage.getScope(),
	                    linkage.timeCreated, linkage.position);

	            SrcEntity resource = new SrcEntity(EntityType.CMDSRESOURCE, getResourceId(content));

	            updateUserActionMappintToEs(libraryContentLinkDetails, resource, UserActionType.ADDED,
	                    EventActionType.ADD, parentESId);*/

			return true;
		}

		throw new VedantuException(VedantuErrorCode.FOLDER_NOT_FOUND);
	}

	public CMDSContentLink addLink(SrcEntity content, SrcEntity targetEntity,
								   CmdsContentLinkType linkType, String actorId, boolean allowDuplicates)
			throws VedantuException {

		//MutableLong totalHits = new MutableLong(0L);
		List<CMDSContentLink> links = cmdsContentManager.getCmdsContentLinks(content, targetEntity, linkType, null, null, 0,
				1, VedantuRecordState.ACTIVE, null);

		if (links.size() > 1 && !allowDuplicates) {
			logger.error("Added multiple times ");
			throw new VedantuException(VedantuErrorCode.ALREADY_ADDED);
		}

		CMDSContentLink contentLinkage = null;
		if (CollectionUtils.isNotEmpty(links) && !allowDuplicates) {
			contentLinkage = links.get(0);
		} else {
			contentLinkage = new CMDSContentLink(targetEntity, content);

			contentLinkage.userId = actorId;
			contentLinkage.linkType = linkType;
			// this is ever increasing number for all positions
			contentLinkage.position = getNextSequence(
					"",
					getCounterName(contentLinkage, CMDSContentLink.POSITION), 1);

		}
		cmdsContentLinkRepo.save(contentLinkage);
		return contentLinkage;
	}

	public com.lms.pojos.responce.MoveContentRes moveFolder(MoveContentReq request) {
		List<EntityResponse> responses = moveFolderTo(request.userId, request.entities,
				request.targetFolderId, request.orgId);
		com.lms.pojos.responce.MoveContentRes response = new com.lms.pojos.responce.MoveContentRes();
		response.list.addAll(responses);
		VedantuErrorCode errorCode = EntityResponse.getCumulativeErrorCode(responses);
		if (errorCode != null) {
			response.cumulativeErrorCode = errorCode;
		}
		return response;
	}

	public List<EntityResponse> moveFolderTo(String userId, List<SrcEntity> movingEntities,
											 String targetFolderId, String organizationId) throws VedantuException {

		// Map<SrcEntity, String> errorCodeMap = new HashMap<SrcEntity, String>();

		List<EntityResponse> responseList = new ArrayList<EntityResponse>();
		CMDSFolder targetFolder = findById(organizationId, targetFolderId);
		for (SrcEntity movingEntity : movingEntities) {
			try {
				// here moving entity type is folder
				if (movingEntity.type == EntityType.FOLDER) {
					CMDSFolder movingFolder = findById(organizationId,
							movingEntity.id);
					if (movingFolder != null) {
						move(userId, movingFolder, targetFolder, organizationId);
					}
				} else {
					// here individual entity is being moved
					move(userId, movingEntity, targetFolder, organizationId);
				}
				responseList.add(new EntityResponse(movingEntity, null));
			} catch (VedantuException exception) {
				// errorCodeMap.put(movingEntity, exception.errorCode.name());
				responseList.add(new EntityResponse(movingEntity, exception.errorCode));
			}
		}
		return responseList;
	}

	private boolean move(String userId, SrcEntity entity, CMDSFolder moveToFolder,
						 String organizationId) throws VedantuException {

		AtomicLong totalHits = new AtomicLong(0L);

		SrcEntity target = new SrcEntity(EntityType.FOLDER, moveToFolder._getStringId());
	       /* CmdsContentLinkDAO.INSTANCE.updateTargetEntity(null, new SrcEntity(entity.type, entity.id),
	                new SrcEntity(EntityType.FOLDER, null), target, CmdsContentLinkType.ADDED, null);*/
		SrcEntity content = new SrcEntity(entity.type, entity.id);

		if (totalHits.longValue() > 1) {
			logger.error("Incorrect number of links for data");
			throw new VedantuException(VedantuErrorCode.CONTENT_CAN_NOT_BE_MOVED);
		}
	       /* VedantuBasicDAO<?, ?> basicDAO = EntityTypeDAOFactory.INSTANCE.get(entity.type);
	        if (basicDAO == null) {
	            logger.error("No dao found for content :" + entity);
	        }
	        if (basicDAO instanceof CmdsContentDAO) {
	            CmdsContentDAO<?, ?> cmdsContentDAO = (CmdsContentDAO<?, ?>) basicDAO;
	            if (!cmdsContentDAO.isMovingAllowed(entity.id)) {
	                logger.error("No moving is allowed found for question inside questionsets :"
	                        + entity);
	                throw new VedantuException(VedantuErrorCode.CONTENT_CAN_NOT_BE_MOVED);
	            }
	        } else {
	            logger.error("Content does not have database dao :" + entity);
	            throw new VedantuException(VedantuErrorCode.CONTENT_CAN_NOT_BE_MOVED);
	        }*/

		List<CMDSContentLink> links = cmdsContentManager.getCmdsContentLinks(content,
				target, CmdsContentLinkType.ADDED, null, null, 0, 1, null, null);
		CMDSContentLink linkage = links.get(0);
		return linkage != null;

		// CmdsContentLinkDAO.INSTANCE.save(linkage);
	       /* updateContentLink(linkage._getStringId(), linkage.userId, content, target,
	                linkage.timeCreated, linkage.getScope(), linkage.position);*/
	}

	private boolean move(String userId, CMDSFolder currentFolder, CMDSFolder targetFolder,
						 String organizationId) throws VedantuException {

		if (currentFolder == null || targetFolder == null) {
			throw new VedantuException(VedantuErrorCode.FOLDER_NOT_FOUND);
		}

		if (!validateMove(currentFolder, targetFolder)) {
			throw new VedantuException(VedantuErrorCode.FOLDER_CAN_NOT_BE_MOVED,
					"Can not move folder as its root");
		}
		logger.debug("Moving one folder to other" + currentFolder + " target folder "
				+ targetFolder);

		List<String> newParents = new ArrayList<String>();
		if (targetFolder.parentSources == null) {
			targetFolder.parentSources = new ArrayList<String>();

		}

		newParents.addAll(targetFolder.parentSources);
		List<String> oldParents = currentFolder.parentSources;
		updateParent(currentFolder._getStringId(),
				targetFolder._getStringId(), newParents, oldParents);

		SrcEntity content = new SrcEntity(EntityType.FOLDER, currentFolder._getStringId());

		SrcEntity target = new SrcEntity(EntityType.FOLDER, targetFolder._getStringId());

		updateTargetEntity(null,
				new SrcEntity(content.type, content.id), new SrcEntity(EntityType.FOLDER, null),
				target, CmdsContentLinkType.ADDED, null);

		List<CMDSContentLink> links = cmdsContentManager.getCmdsContentLinks(content,
				target, CmdsContentLinkType.ADDED, null, null, 0, 1, null, null);
		CMDSContentLink linkage = links.get(0);
		return linkage != null;

	       /* updateContentLink(linkage._getStringId(), linkage.userId, content, target,
	                linkage.timeCreated, linkage.getScope(), linkage.position);*/
	}

	private boolean validateMove(CMDSFolder currentFolder, CMDSFolder targetFolder)
			throws VedantuException {

		if (currentFolder.isRoot) {
			throw new VedantuException(VedantuErrorCode.ROOT_FOLDER_CAN_NOT_BE_MOVED,
					"Can not move folder as its root");
		}

		if (targetFolder._getStringId().equals(currentFolder._getStringId())) {
			throw new VedantuException(VedantuErrorCode.FOLDER_CAN_NOT_BE_CHILD_OF_SELF,
					"Can not move folder as both folders are same");
		}

		if ((targetFolder.parentSources != null && targetFolder.parentSources
				.contains(currentFolder._getStringId()))) {
			throw new VedantuException(VedantuErrorCode.FOLDER_CAN_NOT_BE_MOVED_TO_CHILD_FOLDER,
					"Can not move folder as target folder is child of current folder");
		}

		if (checkForSimilarNameDirectory(currentFolder.organizationId,
				targetFolder._getStringId(), currentFolder.name)) {
			throw new VedantuException(VedantuErrorCode.FOLDER_ALREADY_EXISTS,
					"Can not move folder as  target folder already contains folder with same name");
		}
		return true;
	}

	public boolean updateParent(String folderId, String newParent, List<String> newParents,
								List<String> oldParents) {
		Optional<CMDSFolder> cmdsFolderOptional = cmdsFolderRepo.findById(folderId);
		CMDSFolder cmdsFolder = cmdsFolderOptional.get();
		newParents.add(0, newParent);
		cmdsFolder.setParent(newParent);
		cmdsFolder.setParentSources(newParents);
		cmdsFolderRepo.save(cmdsFolder);
		if (CollectionUtils.isNotEmpty(oldParents)) {
			List<String> childsOfCurrentFolder = new ArrayList<String>();
			childsOfCurrentFolder.add(folderId);
			childsOfCurrentFolder.addAll(oldParents);
			CMDSFolder cmdsFolder2 = cmdsFolderRepo.findByParentSources(childsOfCurrentFolder);

			if (cmdsFolder2.getParentSources() != null) {
				cmdsFolder2.getParentSources().removeAll(oldParents);
			}
			cmdsFolderRepo.save(cmdsFolder2);
		}
		if (CollectionUtils.isNotEmpty(newParents)) {
			CMDSFolder cmdFolder = cmdsFolderRepo.findByParentSources(folderId);
			if (cmdFolder != null) {
				cmdFolder.setParentSources(newParents);
				cmdsFolderRepo.save(cmdFolder);
			}
		}
		return true;
	}

	public boolean updateTargetEntity(String id, SrcEntity content, SrcEntity oldTargetEntity,
									  SrcEntity newTargetEntity, CmdsContentLinkType linkType, Scope scope)
			throws VedantuException {
		try {
			Query query = new Query();
			Criteria criteria = new Criteria();
			if (id != null) {
				criteria.and("_id").is(id);
			}
			addTargetFilter(criteria, oldTargetEntity);
			addSourceFilter(criteria, content);
			addLinkTypeFilter(criteria, linkType);
			CMDSContentLink cmdsContentLink = mongoTemplate.findOne(query, CMDSContentLink.class);
			if (scope != null) {
				cmdsContentLink.setScope(scope);
			}
			SrcEntity targetEntity = new SrcEntity(newTargetEntity.type, newTargetEntity.id);
			cmdsContentLink.setTarget(targetEntity);
			cmdsContentLink.setPosition(getNextSequence("cmdsContentLink"
					, getCounterName(newTargetEntity, linkType,
							CMDSContentLink.POSITION), 1));
			cmdsContentLinkRepo.save(cmdsContentLink);
		} catch (VedantuException e) {
			throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);

		} catch (Exception e) {
			throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
		}
		return true;
	}

	private void addTargetFilter(Criteria criteria, SrcEntity target) {

		if (target != null) {
			if (target.type != null) {
				criteria.and("target.type").is(target.type);
				if (target.id != null) {
					criteria.and("target.id").is(target.id);

				}
			}

		}

	}

	private void addSourceFilter(Criteria criteria, SrcEntity source) {

		if (source != null) {
			if (source.type != null) {
				criteria.and("source.type").is(source.type);
				if (source.id != null) {
					criteria.and("source.id").is(source.id);

				}
			}

		}

	}

	private void addLinkTypeFilter(Criteria criteria, CmdsContentLinkType linkType) {

		if (linkType != null || linkType != CmdsContentLinkType.UNKNOWN) {
			criteria.and("linkType").is(linkType);
		}

	}

	public DeleteContentRes removeResources(DeleteContentReq request) {
		DeleteContentRes response = new DeleteContentRes();
		// response.info = errorCodeMap;

		// Map<SrcEntity, String> errorCodeMap = new HashMap<SrcEntity, String>();
		List<EntityResponse> responseList = new ArrayList<EntityResponse>();

		for (SrcEntity entity : request.entities) {
			try {
				if (!EntityType.isSupportedCMDSLibraryEntityType(entity.type)) {
					throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
				}

				boolean deleteResult = cmdsContentManager.delete(entity);

				logger.debug("Delete result" + entity + "  results " + deleteResult);
				if (!deleteResult) {
					throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
				}
				// remove link indiex

				logger.debug("Entity out of cannot be removed");

				remove(entity, null, null);

				// drop index and parent child everything
			               /* removeCMDSResource(entity);

			                logger.debug("Entity out of remove CMDS Resource");

			                // drop all linx
			                ReIndexDetails details = new ReIndexDetails();
			                details.ids.add(entity.id);
			                details.type = entity.type;
			                details.userId = request.userId;

			                generateEventAysc(details.userId, details, EventType.REINDEX_CMDS_RESOURCE);*/
				responseList.add(new EntityResponse(entity, null));

				if (entity.type == EntityType.CMDSQUESTION) {
					CMDSQuestion ques = cmdsQuestionRepo.findByIdAndRecordState(entity.id, VedantuRecordState.DELETED);
					// Delete PARA Questions
					if (ques != null && ques.type == QuestionType.TEXT) {
						deleteParaIds(ques);
					}
					// Delete mapping from parent question
					if (ques.scope == Scope.PRIVATE) {
						Optional<CMDSQuestion> origQuesOptional = cmdsQuestionRepo.findById(ques.parentQId);
						CMDSQuestion origQues = origQuesOptional.get();
						origQues.sharedToOrgIds.remove(request.orgId);
						origQues.sharedCMDSQuesIds.remove(entity.id);
						cmdsQuestionRepo.save(origQues);
					}
					// Delete all shared questions
					if (request.orgId.equals(learnPediaId)) {
						logger.debug("delete : About to delete shared LP Questions");
						deleteSharedQuestion(entity.id, request.orgId);
					}
				}
			} catch (VedantuException exception) {
				// errorCodeMap.put(movingEntity, exception.errorCode.name());
				responseList.add(new EntityResponse(entity, exception.errorCode));
			}
		}
		response.list.addAll(responseList);
		 VedantuErrorCode errorCode = EntityResponse.getCumulativeErrorCode(response.list);
	        if (errorCode != null) {
	            response.cumulativeErrorCode = errorCode;
	        }
		return response;
	}
           
			public GetFoldersRes getFolders(GetFoldersReq request) {

		        GetFoldersRes response = new GetFoldersRes();
		        AtomicLong totalHits = new AtomicLong(0);

		        if (StringUtils.isEmpty(request.folderId)) {
		            CMDSFolder rootFolder = getRootFolder(request.userId, request.orgId);
		            totalHits.incrementAndGet();

		            response.list.add(rootFolder.toBasicInfo());
		        } else {

		            List<CMDSFolder> folders = getChilds(request.folderId,
		                    request.start, request.size, totalHits);
		            if (CollectionUtils.isNotEmpty(folders)) {
		                for (CMDSFolder folder : folders) {
		                    response.list.add(folder.toBasicInfo());
		                }
		            }
		        }

		        response.totalHits = totalHits.longValue();

		        return response;

			}
			public List<CMDSFolder> getChilds(String folderId, int start, int size, AtomicLong totals) {
                Query query = new Query();
                Criteria criteria = new Criteria();
                criteria.and("parent").is(folderId);
                query.addCriteria(criteria);
                query.skip(start).limit(size);
                query.with(Sort.by(Sort.Direction.ASC, "cName"));
		        totals.set(cmdsFolderRepo.findByParent(folderId).size());
		        return mongoTemplate.find(query, CMDSFolder.class);

		    }


	public CMDSContentLink remove(SrcEntity content, SrcEntity targetEntity,
								  CmdsContentLinkType linkType) throws VedantuException {
		Query query = new Query();
		Criteria criteria = new Criteria();
		addTargetFilter(criteria, targetEntity);
		addSourceFilter(criteria, content);
		addLinkTypeFilter(criteria, linkType);
		CMDSContentLink contentLink = mongoTemplate.findOne(query, CMDSContentLink.class);
		if (contentLink != null) {
			contentLink.setRecordState(VedantuRecordState.DELETED);
			cmdsContentLinkRepo.save(contentLink);
		}

		return contentLink;
	}

	private void deleteParaIds(CMDSQuestion ques) {
		// TODO Auto-generated method stub
		List<String> paraIds = ques.paraIds;
		for (String qId : paraIds) {
			DeleteContentReq req = new DeleteContentReq();
			List<SrcEntity> entities = new ArrayList<SrcEntity>();
			Optional<CMDSQuestion> queOptional = cmdsQuestionRepo.findById(qId);
			CMDSQuestion que = queOptional.get();
			SrcEntity entity = new SrcEntity();
			entity.id = qId;
			entity.type = EntityType.CMDSQUESTION;
			entities.add(entity);
			req.entities = entities;
			req.orgId = que.contentSrc.id;
			req.userId = que.userId;
			req.callingUserId = que.userId;
			req.callingApp = "cmds-app";
			req.callingAppId = "cmds-app";
			try {
				logger.debug("Deleting PARA question " + qId);
				removeResources(req);
				logger.debug("Deleted PARA question " + qId);
			} catch (VedantuException e) {
				logger.debug("Error while deleting PARA Question " + qId + " with message " + e.getMessage());
			}
		}
	}

	private void deleteSharedQuestion(String quesId, String orgId) {
		// TODO Auto-generated method stub
		CMDSQuestion question = cmdsQuestionRepo.findByIdAndRecordState(quesId, VedantuRecordState.DELETED);
		Set<String> sharedQIds = question.sharedCMDSQuesIds;
		for (String qId : sharedQIds) {
			DeleteContentReq req = new DeleteContentReq();
			List<SrcEntity> entities = new ArrayList<SrcEntity>();
			Optional<CMDSQuestion> queOptional = cmdsQuestionRepo.findById(qId);
			CMDSQuestion que = queOptional.get();
			SrcEntity entity = new SrcEntity();
			entity.id = qId;
			entity.type = EntityType.CMDSQUESTION;
			entities.add(entity);
			req.entities = entities;
			req.orgId = que.contentSrc.id;
			req.userId = que.userId;
			req.callingUserId = que.userId;
			req.callingApp = "cmds-app";
			req.callingAppId = "cmds-app";
			try {
				logger.debug("Deleting shared LP question " + qId);
				removeResources(req);
				logger.debug("Deleted shared LP question " + qId);
			} catch (VedantuException e) {
				logger.debug(
						"Error while deleting shared LP Question " + qId + " with message " + e.getMessage());
			}
		}
	}

	public boolean upload(UploadCMDSContentFileReq request) {
		if (request.entityType == EntityType.UNKNOWN) {
			throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
		}
		AbstractEntityFileStorage storage = getEntityFileStore(request.entityType);
		try {
			storage.storeInFS(request.file, request.key, null);
		} catch (EntityFileStorageException e) {
			throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
		}

		return true;
	}

	public SignUploadFileRes sign(SignUploadFileReq request) throws VedantuException {
		SignUploadFileRes response = null;
		AbstractEntityFileStorage storage = getEntityFileStore(request.type);

		if (storage == null) {
			throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
		}

		VedantuBaseMongoModel basicMongoModel = null;

		String uuid = UUID.randomUUID().toString();

		IFileSystemHandler fsSystemHandler = s3Handler;

		String s3Key = AbstractEntityFileStorage.computeFileId(uuid, request.type,
				FileUtils.getExtensionWithoutDOT(request.fileName), request.mediaType, FileCategory.ORIGINAL, null);
		String contentType = "test"; // ContentTypeMapper.get().getContentType(request.fileName);
		if (StringUtils.isEmpty(contentType)) {
			throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
		}
		try {
			response = fsSystemHandler.signContentUpload(request.type, storage.getStorageId(), s3Key, contentType);
		} catch (FileStoreException e) {
			throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
		}
		// TODO clean this one usng AbstractFileModel
		if (request.type == EntityType.CMDSVIDEO) {

			CMDSVideo video = new CMDSVideo();
			video.originalFileName = request.fileName;
			video.extension = FileUtils.getExtensionWithoutDOT(request.fileName);
			video.uuid = uuid;
			video.setRecordState(VedantuRecordState.TEMPORARY);
			cmdsVideoRepo.save(video);
			basicMongoModel = video;
		} else if (request.type == EntityType.CMDSDOCUMENT) {
			CMDSDocument doc = new CMDSDocument();
			doc.originalFileName = request.fileName;
			doc.extension = FileUtils.getExtensionWithoutDOT(request.fileName);
			doc.uuid = uuid;
			doc.setRecordState(VedantuRecordState.TEMPORARY);
			cmdsDocumentRepo.save(doc);
			basicMongoModel = doc;

		} else if (request.type == EntityType.CMDSFILE) {
			CMDSFile file = new CMDSFile();
			file.originalFileName = request.fileName;
			file.extension = FileUtils.getExtensionWithoutDOT(request.fileName);
			file.uuid = uuid;
			file.setRecordState(VedantuRecordState.TEMPORARY);
			cmdsFileRepo.save(file);
			basicMongoModel = file;
		}
		// TODO testing
		request.url = null;
		if (!StringUtils.isEmpty(request.url)) {
			response.url = request.url;
		}
		if (basicMongoModel == null) {
			throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE);
		}
		response.id = basicMongoModel._getStringId();
		response.uuid = uuid;
		return response;
	}

	public AbstractEntityFileStorage getEntityFileStore(EntityType type) {
		AbstractEntityFileStorage storage = null;
		if (type == EntityType.CMDSFILE) {
			cmdsFileStorage.AbstractEntityFileStorageEntity(EntityType.CMDSFILE);
			storage = cmdsFileStorage;
		} else if (type == EntityType.CMDSVIDEO) {
			cmdsVideoStorage.AbstractEntityFileStorageEntity(EntityType.CMDSVIDEO);
			storage = cmdsVideoStorage;
		} else if (type == EntityType.CMDSQUESTIONSET) {
			cmdsQuestionSetEntityFileStorage.AbstractEntityFileStorageEntity(EntityType.CMDSQUESTIONSET);
			storage = cmdsQuestionSetEntityFileStorage;
		} else if (type == EntityType.CMDSDOCUMENT) {
			cmdsFileStorage.AbstractEntityFileStorageEntity(EntityType.CMDSDOCUMENT);
			storage = cmdsFileStorage;
		} else if (type == EntityType.CMDSQUESTION) {
			cmdsFileStorage.AbstractEntityFileStorageEntity(EntityType.CMDSQUESTION);
			storage = cmdsFileStorage;
		}
		return storage;
	}

	public boolean update(EditContentReq request) {
		Optional<CMDSFolder> contentOptional = cmdsFolderRepo.findById(request.entity.id);
		if (!contentOptional.isPresent()) {
			throw new VedantuException(VedantuErrorCode.CONTENT_NOT_FOUND);
		}
		CMDSFolder content = contentOptional.get();
		List<String> updateList = new ArrayList<String>();
		if (!StringUtils.isEmpty(request.name)
				&& request.updateList.contains(EditContentReq.NAME)) {
			content.name = request.name;
			updateList.add(CMDSFolder.NAME);
		}
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("_id").is(request.entity.id);
		query.addCriteria(criteria);
		Update update = new Update();

		try {
			if (updateList.isEmpty()) {
				update.unset(CMDSFolder.NAME);
			} else {
				update.set(CMDSFolder.NAME, request.name);
			}
			update.set(ConstantsGlobal.LAST_UPDATED, System.currentTimeMillis());
			mongoTemplate.updateMulti(query, update, CMDSFolder.class);

			// CMDSFolderDAO.INSTANCE.updateModel(content, updateList);
			//addAsCMDSResource(request.entity, EventActionType.UPDATE, content);

		} catch (VedantuException exception) {
			if (exception.errorCode == VedantuErrorCode.ALREADY_ADDED) {
				logger.error("Folder with name already exists", exception);
				throw new VedantuException(VedantuErrorCode.FOLDER_ALREADY_EXISTS,
						"folder with same name already exists");
			}
			throw exception;
		}

		return true;

	}

	public GetSharedQuestionsBasicInfoRes getQuestionSharingBasicInfo(GetSharedQuestionsBasicInfoReq request) {
		GetSharedQuestionsBasicInfoRes response = new GetSharedQuestionsBasicInfoRes();
		List<ObjectId> orgIds = new ArrayList<ObjectId>();

		AtomicLong totalHits = new AtomicLong(0L);
		List<Organization> organizations = getAllOrganizations(null, totalHits);
		Map<String, String> orgNameMap = new HashMap<String, String>();
		for (Organization org : organizations) {
			orgNameMap.put(org._getStringId(), org.fullName);
		}
		List<BoardMapping> boardMappings = getByParentOrgId(request.orgId);
		List<OrgDetails> sharedOrgDetails = new ArrayList<OrgDetails>();
		boolean showSharedSubjects = organizationRepo.findById(request.orgId).get().showSharedSubjects;
		List<Board> boards = getAllCourses(request.orgId, showSharedSubjects);
		for (BoardMapping boardMapping : boardMappings) {
			List<ObjectId> boardIds = new ArrayList<ObjectId>();
			OrgDetails orgDetail = new OrgDetails();
			orgDetail.orgId = boardMapping.sharedToOrgId;
			orgDetail.orgName = orgNameMap.get(boardMapping.sharedToOrgId);
			orgDetail.publishStatus = boardMapping.publish;
			List<SharedBoardInfo> sharedBoards = new ArrayList<SharedBoardInfo>();
			List<BoardMappings> boardmappings = boardMapping.boardMappings;
			for (BoardMappings board : boardmappings) {
				if (board.boardType.equalsIgnoreCase("course")) {
					SharedBoardInfo sbinfo = new SharedBoardInfo();
					sbinfo.parentBoardId = board.parentBoardId;
					sbinfo.sharedBoardId = board.sharedToBoardId;
					sbinfo.parentBoardName = boardRepo.findById(board.parentBoardId).get().name;
					sbinfo.sharedBoardName = boardRepo.findById(board.sharedToBoardId).get().name;
					sbinfo.status = board.status;
					sharedBoards.add(sbinfo);
					boardIds.add(new ObjectId(board.parentBoardId));
				}
			}
			orgDetail.sharedBoards = sharedBoards;
			if (!boardIds.isEmpty()) {
				List<Board> tempBoards = getAllCoursesExcept(boardIds, request.orgId);
				for (Board board : tempBoards) {
					orgDetail.unSharedBoards.add(board.name);
				}
			} else {
				for (Board board : boards) {
					orgDetail.unSharedBoards.add(board.name);
				}
			}
			sharedOrgDetails.add(orgDetail);
			orgIds.add(new ObjectId(boardMapping.sharedToOrgId));
		}
		response.sharedOrgDetails = sharedOrgDetails;
		if (!orgIds.isEmpty()) {
			organizations.clear();
			organizations = getAllOrganizationsExcept(orgIds);
		}
		List<OrgDetails> orgDetails = new ArrayList<OrgDetails>();
		for (Organization org : organizations) {
			OrgDetails orgDetail = new OrgDetails();
			orgDetail.orgId = org._getStringId();
			orgDetail.orgName = org.name;
			orgDetails.add(orgDetail);
		}
		response.orgDetails = orgDetails;
		return response;

	}

	private List<Organization> getAllOrganizations(OrganizationStatus status, AtomicLong totalHits) {
		List<Organization> organizations = organizationRepo.findAll(Sort.by(Sort.Direction.ASC, "cName"));
		totalHits.set(organizations.size());
		return organizations;
	}

	public List<BoardMapping> getByParentOrgId(String orgId) {
		List<BoardMapping> boardMappings = boardMappingRepo.findByparentOrgId(orgId);
		return boardMappings;
	}

	public List<Board> getAllCourses(String orgId, boolean showSharedSubjects) {
		List<Board> boards = new ArrayList<Board>();
		Set<String> grantedOrgsIds = new LinkedHashSet<String>();
		// Get all the orgIds that gave access to the current organization
		grantedOrgsIds.add(orgId);
		if (showSharedSubjects) {
			AtomicLong totalProgramHits = new AtomicLong(0L);
			List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgPrograms(orgId,
					null, totalProgramHits);
			for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
				grantedOrgsIds.add(granteeOrgProgram.providerOrgId);
			}
		}
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("type").is(BoardType.COURSE);
		criteria.and("recordState").is(VedantuRecordState.ACTIVE);
		criteria.and("ownerId").in(grantedOrgsIds);
		query.addCriteria(criteria);
		/*for (String organizationId : grantedOrgsIds) {
			criteria.and("type").is(BoardType.COURSE);
			criteria.and("recordState").is(VedantuRecordState.ACTIVE);
			criteria.and("ownerId").is(organizationId);
			boards.addAll(query.asList());
		}*/
		boards.addAll(mongoTemplate.find(query, Board.class));
		return boards;
	}

	public List<GranteeOrgProgram> getGranteeOrgPrograms(String providerOrgId, String departmentId,
														 AtomicLong totalHits) {
		logger.debug("getGrateeOrgPrograms orgId: " + providerOrgId
				+ ", departmentId: " + departmentId);
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("subscriberOrgId").is(providerOrgId);
		criteria.and("recordState").is(VedantuRecordState.ACTIVE);
		query.addCriteria(criteria);
		//query.order("cName");
		if (null != departmentId) {
			criteria.and("departmentId").is(departmentId);
		}
		logger.debug("getGrateeOrgPrograms query: " + query);

		List<GranteeOrgProgram> programs = mongoTemplate.find(query, GranteeOrgProgram.class);
		totalHits.set(programs.size());

		logger.info("getGrateeOrgPrograms" + programs.size() + " totalHits: " + totalHits.get());

		return programs;
	}

	private List<Board> getAllCoursesExcept(List<ObjectId> boardIds, String orgId) {
		List<Board> boards = null;
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("type").is(BoardType.COURSE);
		criteria.and("recordState").is(VedantuRecordState.ACTIVE);
		criteria.and("ownerId").is(orgId);
		criteria.and("_id").nin(boardIds);
		query.addCriteria(criteria);
		boards = mongoTemplate.find(query, Board.class);
		return boards;
	}

	private List<Organization> getAllOrganizationsExcept(List<ObjectId> orgIds) {
		List<Organization> organizations = null;
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("_id").nin(orgIds);
		query.addCriteria(criteria);
		query.with(Sort.by(Sort.Direction.ASC, "cName"));
		organizations = mongoTemplate.find(query, Organization.class);
		return organizations;
	}

	public AddMappingsRes getBoardsToAddMappings(AddMappingsReq request) {
		AddMappingsRes response = new AddMappingsRes();
		BoardMapping boardMapping = getBySharedToOrgId(request.parentOrgId, request.targetOrgId);
		boolean showSharedSubjects = organizationRepo.findById(request.parentOrgId).get().showSharedSubjects;
		List<Board> parentCourseBoards = getAllCourses(request.parentOrgId, showSharedSubjects);
		logger.debug("Size of parentCourseBoards is " + parentCourseBoards.size());
		showSharedSubjects = organizationRepo.findById(request.targetOrgId).get().showSharedSubjects;
		List<Board> targetCourseBoards = getAllCourses(request.targetOrgId, showSharedSubjects);
		logger.debug("Size of targetCourseBoards is " + targetCourseBoards.size());
		Map<String, String> parentBoardMap = new HashMap<String, String>();
		Map<String, String> sharedBoardMap = new HashMap<String, String>();
		if (boardMapping != null) {
			List<BoardMappings> boardMappings = boardMapping.boardMappings;
			for (BoardMappings boardMap : boardMappings) {
				if (boardMap.boardType.equalsIgnoreCase("COURSE")) {
					parentBoardMap.put(boardMap.parentBoardId, boardMap.sharedToBoardId);
					sharedBoardMap.put(boardMap.sharedToBoardId, boardMap.parentBoardId);
				}
			}
		}
		logger.debug("Size of parentBoardMap is " + parentBoardMap.size());
		logger.debug("Size of sharedBoardMap is " + sharedBoardMap.size());
		List<BoardInfo> boardDetails = new ArrayList<BoardInfo>();
		for (Board board : parentCourseBoards) {
			BoardInfo parentBoardDetail = new BoardInfo();
			if (!parentBoardMap.containsKey(board._getStringId())) {
				logger.debug(board.name + " is not mapped");
				List<BoardInfo> childrenBoardDetail = new ArrayList<BoardInfo>();
				List<Board> childBoards = getAllChildren(request.parentOrgId, board._getStringId());
				logger.debug("Size of the children of board " + board.name + " is " + childBoards.size());
				parentBoardDetail.boardId = board._getStringId();
				parentBoardDetail.boardName = board.name;
				parentBoardDetail.boardtype = board.type.name();
				for (Board brd : childBoards) {
					BoardInfo brdInfo = new BoardInfo();
					brdInfo.boardId = brd._getStringId();
					brdInfo.boardName = brd.name;
					brdInfo.boardtype = brd.type.toString();
					childrenBoardDetail.add(brdInfo);
				}
				logger.debug("Size of the children board we created is " + childrenBoardDetail.size());
				parentBoardDetail.children = childrenBoardDetail;
				boardDetails.add(parentBoardDetail);
			}
		}
		response.parentBoardDetails.addAll(boardDetails);
		logger.debug("Size of parentBoardDetails is " + response.parentBoardDetails.size());
		boardDetails.clear();
		for (Board board : targetCourseBoards) {
			BoardInfo sharedBoardDetail = new BoardInfo();
			if (!sharedBoardMap.containsKey(board._getStringId())) {
				logger.debug(board.name + " is not mapped");
				List<BoardInfo> childrenBoardDetail = new ArrayList<BoardInfo>();
				List<Board> childBoards = getAllChildren(request.targetOrgId, board._getStringId());
				logger.debug("Size of the children of board " + board.name + " is " + childBoards.size());
				sharedBoardDetail.boardId = board._getStringId();
				sharedBoardDetail.boardName = board.name;
				sharedBoardDetail.boardtype = board.type.toString();
				for (Board brd : childBoards) {
					BoardInfo brdInfo = new BoardInfo();
					brdInfo.boardId = brd._getStringId();
					brdInfo.boardName = brd.name;
					brdInfo.boardtype = brd.type.toString();
					childrenBoardDetail.add(brdInfo);
				}
				logger.debug("Size of the children board we created is " + childrenBoardDetail.size());
				sharedBoardDetail.children = childrenBoardDetail;
				boardDetails.add(sharedBoardDetail);
			}
		}
		response.targetBoardDetails.addAll(boardDetails);
		logger.debug("Size of targetBoardDetails is " + response.targetBoardDetails.size());
		return response;

	}

	public List<Board> getAllChildren(String parentOrgId, String boardId) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("type").is(BoardType.TOPIC);
		//query.filter("ownerId", parentOrgId);
		criteria.and("recordState").is(VedantuRecordState.ACTIVE);
		List<String> parentBoardIds = new ArrayList<String>();
		parentBoardIds.add(boardId);
		criteria.and("parentBrdIds").in(parentBoardIds);
		query.addCriteria(criteria);
		return mongoTemplate.find(query, Board.class);
	}

	public SaveMappingRes saveBoardMapping(SaveMappingsReq request) {
		SaveMappingRes response = new SaveMappingRes();
		Organization parentOrg = organizationRepo.findById(request.parentOrgId).get();
		Organization sharedToOrg = organizationRepo.findById(request.sharedToOrgId).get();
		if (parentOrg == null || sharedToOrg == null) {
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND, "Invalid Organisation Info");
		}
		BoardMapping boardMapping = getBySharedToOrgId(request.parentOrgId, request.sharedToOrgId);
		if (boardMapping == null) {
			boardMapping = new BoardMapping();
			boardMapping.orgId = request.orgId;
			boardMapping.userId = request.userId;
			boardMapping.parentOrgId = request.parentOrgId;
			boardMapping.sharedToOrgId = request.sharedToOrgId;
		}
		boardMapping.boardMappings.addAll(request.boardMappings);
		response.saved = saveBoardMappings(boardMapping);
		return response;
	}

	public boolean saveBoardMappings(BoardMapping boardMapping) {
		boardMappingRepo.save(boardMapping);
		return true;
	}

	public SaveMappingRes deleteBoardMapping(DeleteMappingReq request) {
		SaveMappingRes response = new SaveMappingRes();
		Organization parentOrg = null;
		Optional<Organization> parentOrgOptional = organizationRepo.findById(request.parentOrgId);
		if (parentOrgOptional.isPresent()) {
			parentOrg = parentOrgOptional.get();
		}
		Organization sharedToOrg = null;
		Optional<Organization> sharedToOrgOptional = organizationRepo.findById(request.sharedToOrgId);
		if (sharedToOrgOptional.isPresent()) {
			sharedToOrg = sharedToOrgOptional.get();
		}
		if (parentOrg == null || sharedToOrg == null) {
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND, "Invalid Organisation Info");
		}
		BoardMapping boardMappings = getBySharedToOrgId(request.parentOrgId, request.sharedToOrgId);
		if (boardMappings == null) {
			throw new VedantuException(VedantuErrorCode.ORGANISATION_MAPPING_NOT_FOUND,
					"No Organisation Mapping Found");
		}
		String parentBoardId = request.parentBoardId;
		String sharedToBoardId = request.sharedToBoardId;
		List<Board> childrenBoards = getAllChildren(request.parentOrgId, parentBoardId);
		List<String> childBoards = getIdsOfchildBoards(childrenBoards);
		List<BoardMappings> newBoardMappings = new ArrayList<BoardMappings>();
		boolean mappingFound = false;
		for (BoardMappings boardMapping : boardMappings.boardMappings) {
			if (boardMapping.boardType.equalsIgnoreCase("COURSE") && boardMapping.parentBoardId.equals(parentBoardId)
					&& boardMapping.sharedToBoardId.equals(sharedToBoardId)) {
				if (boardMapping.status == false) {
					mappingFound = true;
					break;
				} else {
					throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED, "Cannot Remove Board Mappings");
				}
			}
		}
		if (!mappingFound) {
			throw new VedantuException(VedantuErrorCode.BOARD_MAPPING_NOT_FOUND, "No Board Mapping Found");
		}
		for (BoardMappings boardMapping : boardMappings.boardMappings) {
			if (boardMapping.boardType.equalsIgnoreCase("COURSE") && boardMapping.parentBoardId.equals(parentBoardId)
					&& boardMapping.sharedToBoardId.equals(sharedToBoardId)) {
				// Dont add this mapping(COURSE) to new board
			} else if (boardMapping.boardType.equalsIgnoreCase("TOPIC")
					&& childBoards.contains(boardMapping.parentBoardId)) {
				// Dont add this mapping(TOPIC) to new board
			} else {
				newBoardMappings.add(boardMapping);
			}
		}
		boardMappings.boardMappings = newBoardMappings;
		response.saved = saveBoardMappings(boardMappings);
		return response;

	}

	private List<String> getIdsOfchildBoards(List<Board> childBoards) {
		// TODO Auto-generated method stub
		List<String> childrenIds = new ArrayList<String>();
		for (Board board : childBoards) {
			childrenIds.add(board._getStringId());
		}
		return childrenIds;
	}

	public SaveMappingRes visibleBoardMapping(VisibleMappingReq request) {
		SaveMappingRes response = new SaveMappingRes();
		Organization parentOrg = null;
		Optional<Organization> parentOrgOptional = organizationRepo.findById(request.parentOrgId);
		if (parentOrgOptional.isPresent()) {
			parentOrg = parentOrgOptional.get();
		}
		Organization sharedToOrg = null;
		Optional<Organization> sharedToOrgOptional = organizationRepo.findById(request.sharedToOrgId);
		if (sharedToOrgOptional.isPresent()) {
			sharedToOrg = sharedToOrgOptional.get();
		}
		if (!request.isSelfVisible && (parentOrg == null || sharedToOrg == null)) {
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND, "Invalid Organisation Info");
		}
		BoardMapping boardMappings = getBySharedToOrgId(request.parentOrgId, request.sharedToOrgId);
		if (boardMappings == null) {
			throw new VedantuException(VedantuErrorCode.ORGANISATION_MAPPING_NOT_FOUND, "No Organisation Mapping Found");
		}
		boardMappings.publish = request.visible;
		response.saved = saveBoardMappings(boardMappings);
		return response;
	}

	public List<ShareMappingResponse> shareBoardMapping(DeleteMappingReq request) {

		List<ShareMappingResponse> response = new ArrayList<ShareMappingResponse>();
		Organization parentOrg = getOrganizationById(request.parentOrgId);
		Organization sharedToOrg = getOrganizationById(request.sharedToOrgId);
		if (parentOrg == null || sharedToOrg == null) {
			throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND, "Invalid Organisation Info");
		}
		BoardMapping boardMappings = getBySharedToOrgId(request.parentOrgId, request.sharedToOrgId);
		if (boardMappings == null) {
			throw new VedantuException(VedantuErrorCode.ORGANISATION_MAPPING_NOT_FOUND, "No Organisation Mapping Found");
		}
		String parentBoardId = request.parentBoardId;
		String sharedToBoardId = request.sharedToBoardId;
		List<Board> childrenBoards = getAllChildren(request.parentOrgId, parentBoardId);
		List<Board> sharedChildBoards = getAllChildren(request.sharedToOrgId, sharedToBoardId);
		if (sharedChildBoards.size() < childrenBoards.size()) {
			throw new VedantuException(VedantuErrorCode.TOPIC_NOT_FOUND);
		}
		List<String> childBoards = getIdsOfchildBoards(childrenBoards);
		boolean shareMapping = false;
		for (BoardMappings boardMapping : boardMappings.boardMappings) {
			if (boardMapping.boardType.equalsIgnoreCase("COURSE") && boardMapping.parentBoardId.equals(parentBoardId) && boardMapping.sharedToBoardId.equals(sharedToBoardId)) {
				if (boardMapping.status == false) {
					shareMapping = true;
					break;
				} else if (boardMapping.status == true && request.reSync == true) {
					shareMapping = true;
					break;
				} else {
					throw new VedantuException(VedantuErrorCode.ALREADY_SHARED, "Board Mapping already shared");
				}
			}
		}
		if (!shareMapping) {
			throw new VedantuException(VedantuErrorCode.BOARD_MAPPING_NOT_FOUND, "Unable To Share Questions");
		} else {
			// Implement logic to share questions
			String sharedToOrgUserId = getOrganizationById(request.sharedToOrgId).adminUserId;
			String sharedToOrgId = request.sharedToOrgId;
			Map<String, String> boardIdsMap = new HashMap<String, String>();
			// Inserting course level boards into map
			boardIdsMap.put(parentBoardId, sharedToBoardId);
			// Inserting topic level boards into map
			for (BoardMappings boardMapping : boardMappings.boardMappings) {
				if (boardMapping.boardType.equalsIgnoreCase("TOPIC") && childBoards.contains(boardMapping.parentBoardId)) {
					boardIdsMap.put(boardMapping.parentBoardId, boardMapping.sharedToBoardId);
				}
			}
			if ((boardIdsMap.size() - 1) != childBoards.size()) {
				throw new VedantuException(VedantuErrorCode.INCOMPLETE_BOARD_MAPPING);
			}
			JSONObject info = new JSONObject();
			info.put("parentOrgId", request.parentOrgId);
			info.put("sharedToOrgUserId", sharedToOrgUserId);
			info.put("sharedToOrgId", sharedToOrgId);
			info.put("boardIdsMap", new JSONObject(boardIdsMap));
			info.put("parentBoardId", parentBoardId);
			info.put("sharedToBoardId", sharedToBoardId);
			// Now get list of all learnpedias cmds questions from MONGO and create a new request with target organisation entries and add to cmds questions table
			List<String> boardIds = new ArrayList<String>();
			boardIds.add(parentBoardId);
			// Creating job for SCQ Questions
			List<String> types = new ArrayList<String>();
			types.add(QuestionType.SCQ.toString());
			long totalSCQQuestionsToMap = countByBoard(request.parentOrgId, request.sharedToOrgId, boardIds, types);
			logger.debug("shareBoardMapping : Total SCQ questions to share is " + totalSCQQuestionsToMap);
			info.put("QType", "SCQ");
			if (totalSCQQuestionsToMap > 0) {
				logger.debug("shareBoardMapping : Creating New Job SCQ");
				EntityOperationStatus job = new EntityOperationStatus();
				job.numOfSteps = (int) totalSCQQuestionsToMap;
				job.oType = OperationType.CMDS_QUESTION_SHARING;
				job.message = info.toString();
				entityOperationStatusRepo.save(job);
				ShareMappingResponse resp = new ShareMappingResponse();
				resp.jobId = job._getStringId();
				resp.QType = "SCQ";
				response.add(resp);
				ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
				thread.start();
			}
			// Creating Job for MCQ Questions
			types.clear();
			types.add(QuestionType.MCQ.toString());
			long totalMCQQuestionsToMap = countByBoard(request.parentOrgId, request.sharedToOrgId, boardIds, types);
			logger.debug("shareBoardMapping : Total MCQ questions to share is " + totalMCQQuestionsToMap);
			info.put("QType", "MCQ");
			if (totalMCQQuestionsToMap > 0) {
				logger.debug("shareBoardMapping : Creating New Job MCQ");
				EntityOperationStatus job = new EntityOperationStatus();
				job.numOfSteps = (int) totalMCQQuestionsToMap;
				job.oType = OperationType.CMDS_QUESTION_SHARING;
				job.message = info.toString();
				entityOperationStatusRepo.save(job);
				ShareMappingResponse resp = new ShareMappingResponse();
				resp.jobId = job._getStringId();
				resp.QType = "MCQ";
				response.add(resp);
				ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
				thread.start();
			}
			// Creating Job for MATRIX Questions
			types.clear();
			types.add(QuestionType.MATRIX.toString());
			long totalMatrixQuestionsToMap = countByBoard(request.parentOrgId, request.sharedToOrgId, boardIds, types);
			logger.debug("shareBoardMapping : Total Matrix questions to share is " + totalMatrixQuestionsToMap);
			info.put("QType", "MATRIX");
			if (totalMatrixQuestionsToMap > 0) {
				logger.debug("shareBoardMapping : Creating New Job MATRIX");
				EntityOperationStatus job = new EntityOperationStatus();
				job.numOfSteps = (int) totalMatrixQuestionsToMap;
				job.oType = OperationType.CMDS_QUESTION_SHARING;
				job.message = info.toString();
				entityOperationStatusRepo.save(job);
				ShareMappingResponse resp = new ShareMappingResponse();
				resp.jobId = job._getStringId();
				resp.QType = "MATRIX";
				response.add(resp);
				ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
				thread.start();
			}
			// Creating Job For NUMERIC Questions
			types.clear();
			types.add(QuestionType.NUMERIC.toString());
			long totalNUMERICQuestionsToMap = countByBoard(request.parentOrgId, request.sharedToOrgId, boardIds, types);
			logger.debug("shareBoardMapping : Total NUMERIC questions to share is " + totalNUMERICQuestionsToMap);
			info.put("QType", "NUMERIC");
			if (totalNUMERICQuestionsToMap > 0) {
				logger.debug("shareBoardMapping : Creating New Job NUMERIC");
				EntityOperationStatus job = new EntityOperationStatus();
				job.numOfSteps = (int) totalNUMERICQuestionsToMap;
				job.oType = OperationType.CMDS_QUESTION_SHARING;
				job.message = info.toString();
				entityOperationStatusRepo.save(job);
				ShareMappingResponse resp = new ShareMappingResponse();
				resp.jobId = job._getStringId();
				resp.QType = "NUMERIC";
				response.add(resp);
				ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
				thread.start();
			}
			// Creating Job For TEXT Questions
			types.clear();
			types.add(QuestionType.TEXT.toString());
			long totalTEXTQuestionsToMap = countByBoard(request.parentOrgId, request.sharedToOrgId, boardIds, types);
			logger.debug("shareBoardMapping : Total TEXT questions to share is " + totalTEXTQuestionsToMap);
			info.put("QType", "TEXT");
			if (totalTEXTQuestionsToMap > 0) {
				logger.debug("shareBoardMapping : Creating New Job TEXT");
				EntityOperationStatus job = new EntityOperationStatus();
				job.numOfSteps = (int) totalTEXTQuestionsToMap;
				job.oType = OperationType.CMDS_QUESTION_SHARING;
				job.message = info.toString();
				entityOperationStatusRepo.save(job);
				ShareMappingResponse resp = new ShareMappingResponse();
				resp.jobId = job._getStringId();
				resp.QType = "TEXT";
				response.add(resp);
				ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
				thread.start();
			}

			if (request.addNewPara) {
				// Creating Job For PARA Questions
				types.clear();
				types.add(QuestionType.PARA.toString());
				long totalPARAQuestionsToMap = countByBoard(request.parentOrgId, request.sharedToOrgId, boardIds, types);
				logger.debug("shareBoardMapping : Total PARA questions to share is " + totalPARAQuestionsToMap);
				info.put("QType", "PARA");
				if (totalPARAQuestionsToMap > 0) {
					logger.debug("shareBoardMapping : Creating New Job PARA");
					EntityOperationStatus job = new EntityOperationStatus();
					job.numOfSteps = (int) totalPARAQuestionsToMap;
					job.oType = OperationType.CMDS_QUESTION_SHARING;
					job.message = info.toString();
					entityOperationStatusRepo.save(job);
					ShareMappingResponse resp = new ShareMappingResponse();
					resp.jobId = job._getStringId();
					resp.QType = "PARA";
					response.add(resp);
					ShareQuestionsThread thread = new ShareQuestionsThread(job._getStringId());
					thread.start();
				}
			}

			for (BoardMappings boardMapping : boardMappings.boardMappings) {
				if (boardMapping.boardType.equalsIgnoreCase("COURSE")
						&& boardMapping.parentBoardId.equals(info
						.getString("parentBoardId"))
						&& boardMapping.sharedToBoardId.equals(info
						.getString("sharedToBoardId"))) {
					if (boardMapping.status == false) {
						boardMapping.status = true;
						break;
					}
				}
			}
			saveBoardMappings(boardMappings);
		}
		return response;

	}

	private Organization getOrganizationById(String parentOrgId) {
		Optional<Organization> organizationOptional = organizationRepo.findById(parentOrgId);
		if (organizationOptional.isPresent()) {
			return organizationOptional.get();
		}
		return null;
	}

	public long countByBoard(String orgId, String sharedToOrgId, List<String> boardIds, List<String> types) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and("contentSrc.type").is(EntityType.ORGANIZATION);
		criteria.and("contentSrc.id").is(orgId);
		criteria.and("recordState").is(VedantuRecordState.ACTIVE);
		criteria.and("boardIds").in(boardIds);
		criteria.and("type").in(types);
		List<String> sharedOrgIds = new ArrayList<String>();
		sharedOrgIds.add(sharedToOrgId);
		criteria.and("sharedToOrgIds").nin(sharedOrgIds);
		query.addCriteria(criteria);
		return mongoTemplate.count(query, CMDSQuestion.class);
	}

}
