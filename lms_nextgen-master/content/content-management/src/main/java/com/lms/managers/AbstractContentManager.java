package com.lms.managers;

import com.lms.board.model.Board;
import com.lms.board.model.GranteeOrgProgram;
import com.lms.board.pojos.responces.GetTreesRes;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.pojos.test.BoardTree;
import com.lms.board.pojos.test.BoardTreeRes;
import com.lms.board.repo.BoardRepo;
import com.lms.common.content.interfaces.IContentManager;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.pojos.DownloadableFileInfo;
import com.lms.common.relationships.EntityUserActionRelationshipSearchDetails;
import com.lms.common.utils.*;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.entity.storage.*;
import com.lms.common.vedantu.enums.*;
import com.lms.common.vedantu.enums.UserActionType.EventActionType;
import com.lms.common.vedantu.event.api.EntityIndexEventMapper;
import com.lms.common.vedantu.event.api.IEventDetails;
import com.lms.common.vedantu.event.api.IMongoAware;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.components.QuestionComponent;
import com.lms.enums.OrgMemberProfile;
import com.lms.enums.SearchResultType;
import com.lms.interfaces.IReverseImageMapperProcessor;
import com.lms.interfaces.Updatable;
import com.lms.models.Module;
import com.lms.models.*;
import com.lms.models.tests.Assignment;
import com.lms.pojo.*;
import com.lms.pojos.requests.AbstractContentSearchReq;
import com.lms.pojos.requests.EditContentReq;
import com.lms.pojos.requests.GetSimilarEntities;
import com.lms.pojos.responce.AbstractContentUserActionRes;
import com.lms.pojos.responce.GetDocumentRes;
import com.lms.pojos.responce.GetVideoRes;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.responce.file.GetFileRes;
import com.lms.pojos.responce.questions.GetQuestionRes;
import com.lms.pojos.responce.questions.GetSolutionsRes;
import com.lms.pojos.responce.tests.GetAssignmentInfoRes;
import com.lms.pojos.search.details.AbstractBoardSearchEntityTagDetails;
import com.lms.pojos.search.details.AbstractFileModelIndexSearchDetails;
import com.lms.pojos.search.details.AbstractSearchDetail;
import com.lms.repository.*;
import com.lms.user.vedantu.user.enums.Gender;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.pojo.UserInfo;
import com.lms.utils.IAttemptableEntity;
import com.lms.utils.ISocialEntity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.collections4.MapUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


public abstract class AbstractContentManager implements
        IContentManager, Updatable {

    protected static final int ELASTIC_SEARCH_REFRESH_TIME = 500;
    protected static final int ES_ENSURE_QUERY_STATE_MAX_TRY_COUNT = 5;
    private static final Logger logger = LoggerFactory.getLogger(AbstractContentManager.class);
    @Autowired
    private BoardRepo boardRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private OrgProgramRepo orgProgramRepo;
    @Autowired
    private OrgCenterRepo orgCenterRepo;
    @Autowired
    private OrgSectionRepo orgSectionRepo;
    @Autowired
    private OrgDepartmentRepo orgDepartmentRepo;
    @Autowired
    private UserProfilePicEntityFileStorage userStorage;
    @Autowired
    private QuestionRepo questionRepo;
    @Autowired
    private VideoRepo videoRepo;
    @Autowired
    private DocumentsRepo documentsRepo;
    @Autowired
    private EntityUserActionMappingRepo entityUserActionMappingRepo;
    private static final String IMG_IDENTIFIER = "v-uid";
    private static final String IMG_SRC = "src";
    private static final String IMG_SRC_PERMANENT = "v-perm";
    private static final String IMG_CLASS_NAME = "vImageUrl";
    private static final boolean directFileServingEnabled = false;
    @Autowired
    private QuestionComponent questionComponent;
    @Autowired
    private SolutionEntityFileStorage solutionEntityFileStorage;
    @Autowired
    private CommentRepo commentRepo;
    @Autowired
    private DiscussionRepo discussionRepo;
    @Autowired
    private VideoEntityFileStorage videoEntityFileStorage;
    @Autowired
    private QuestionEntityFileStorage questionEntityFileStorage;
    @Autowired
    private FileStorage fileStorage;
    private static final boolean videoFileSecurityEnabled = false;
    @Autowired
    private FilesRepo filesRepo;
    @Autowired
    private StatusFeedEntityFileStorage statusFeedEntityFileStorage;
    @Autowired
    private ModuleRepo moduleRepo;
    @Autowired
    private MessageEntityFileStorage messageEntityFileStorage;
    @Autowired
    private EventUtil eventUtil;

    public void isSocialActionAllowed(EntityType entityType, String entityId)
            throws VedantuException {

        //VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(entityType);
        if (entityType == null) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED);
        }
        List<VedantuBaseMongoModel> model = new ArrayList<>();
        if (entityType == EntityType.COMMENT) {
            model = commentRepo.findByParentIdAndRecordState(entityId, VedantuRecordState.ACTIVE);

        }else if(entityType == EntityType.MODULE) {
         model = moduleRepo.findByIdAndRecordState(entityId, VedantuRecordState.ACTIVE)	;
        }
        if (model == null || model.isEmpty()) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, "no " + entityType
                    + " found for id[" + entityId + "]");

        }
    }

    protected void updateParentCommentsCount(String userId, SrcEntity parent)
            throws VedantuException {
        VedantuBaseMongoModel model = null;
        if (parent.getType() == EntityType.DISCUSSION)
            model = incCommentsCount(parent.id, parent.getType());
        if (parent.getType() == EntityType.COMMENT)
            model = incCommentsCount(parent.id, parent.getType());
        if (parent.getType() == EntityType.VIDEO)
            model = incCommentsCount(parent.id, parent.getType());
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        logger.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (eventType != null) {
            //    generateEventAysc(userId, model, EventActionType.UPDATE, eventType,UserActionType.COMMENTED, false);
        }
    }

    public VedantuBaseMongoModel incCommentsCount(String id, EntityType type) {
        if (type == EntityType.DISCUSSION) {
            Optional<Discussion> discussion = discussionRepo.findById(id);
            discussion.get().setComments(discussion.get().getComments() + 1);
            return discussion.get();
        }
        if (type == EntityType.COMMENT) {
            Optional<Comment> comment = commentRepo.findById(id);
            comment.get().setComments(comment.get().getComments() + 1);
            return comment.get();
        }
        if (type == EntityType.VIDEO) {
            Optional<Video> discussion = videoRepo.findById(id);
            discussion.get().setComments(discussion.get().getComments() + 1);
            return discussion.get();
        }
        return null;
    }


    /* protected static void updateParentUpVotesCount(String userId, SrcEntity parent)
             throws VedantuException {

         IUpVotable dao = (IUpVotable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
         LOGGER.info("updating " + parent + ", vote count");
         VedantuBaseMongoModel model = dao.incUpVotesCount(parent.id);
         LOGGER.debug("updated model : " + model);
         // if (model instanceof IIndexable) {
         // EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
         // Logger.debug("eventType: " + eventType + ", for parentType : " + parent.type);
         // if (eventType != null) {
         // generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
         // UserActionType.VOTED, false);
         // }
         // }
 =======
 >>>>>>> 280223f9234304b3c27aedcbc736e9c6752fdeb7

     /* public static IListResponseObj collectResourceInfo() {

          return null;
      }

      protected static void addAttemptInfo(String userId, Map<String, Boolean> entityAttemptMap,
              IAttemptableEntity entity) {

          entity._setAttempted(entityAttemptMap != null
                  && entityAttemptMap.get(entity._getEntityId()) != null ? entityAttemptMap.get(
                  entity._getEntityId()).booleanValue() : false);
      }

      protected static void addSocialActionInfo(String userId, String entityOwnerId,
              Map<String, Boolean> entityVoteMap, Map<String, FollowType> followTypeMap,
              ISocialEntity entity) {

          entity._setVoted(entityVoteMap != null && entityVoteMap.get(entity._getEntityId()) != null ? entityVoteMap
                  .get(entity._getEntityId()).booleanValue() : false);
          FollowType followType = StringUtils.equals(userId, entityOwnerId) ? FollowType.OWNER
                  : (followTypeMap != null && followTypeMap.get(entity._getEntityId()) != null ? followTypeMap
                          .get(entity._getEntityId()) : FollowType.NONE);
          entity._setFollowType(followType);
      }

      protected static void annotateUserSocialActionInfos(String orgId, String userId,
              EntityType entityType, List<? extends AbstractContentUserActionRes> entites,
              Set<String> userIds, Set<String> entityIds) {

          Map<String, ModelBasicInfo> userInfoMap = getUserInfoMap(orgId, userIds);
          Map<String, Boolean> entityVoteMap = EntityUserActionDAO.INSTANCE.getEntityUpVoteMap(
                  userId, entityIds);
          Map<String, FollowType> followTypeMap = EntityUserActionDAO.INSTANCE
                  .getEntityFollowTypeMap(userId, entityType, entityIds);
          for (AbstractContentUserActionRes res : entites) {
              String usrId = res.user.id;
              addSocialActionInfo(userId, usrId, entityVoteMap, followTypeMap, res);
              res.user = (UserInfo) userInfoMap.get(usrId);
          }
      }

      public static String removeTempImageSrcAndSaveToFS(EntityType entityType, String content,
              boolean moveImage, String folder) throws EntityFileStorageException {

          return removeTempImageSrcAndSaveToFS(entityType, content, moveImage, null, null, folder);
      }

      public static String removeTempImageSrcAndSaveToFS(EntityType entityType, String content,
              boolean moveImage, Set<String> uuids, Map<String, String> tags, String folder)
              throws EntityFileStorageException {

          LOGGER.debug("removeTempImageSrcAndSaveToFS content: " + content);
          IEntityFileStorage entityStorage = EntityStorageFactory.INSTANCE.get(entityType);
          LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();
          if (uuids == null) {
              uuids = new HashSet<String>();
          }
          content = ImageHTMLUtils.removeImageSrcUrl(content, uuids);
          if (moveImage) {

              for (String uuid : uuids) {
                  String filePath = tempFs.getFilePath(StringUtils.isEmpty(folder) ? "images" : folder, uuid
                          + ImageDisplayURLUtil.JPG_EXTENTION);
                  File file = new File(filePath);
                  entityStorage.storeImage(uuid, file, FileCategory.CONVERTED, ImageSize.ORIGINAL,
                          null);
                  FileUtils.deleteFile(filePath, file);
              }
          }

          LOGGER.debug("removeTempImageSrcAndSaveToFS updated content: " + content);
          return content;
      }

      public static UserInfo getUserInfo(String orgId, String userId) {

          Map<String, ModelBasicInfo> userInfos = getUserInfoMap(orgId, Arrays.asList(userId));

          return (UserInfo) userInfos.get(userId);
      }

      public static UserInfo getUserInfo(String orgId, String userId, boolean excludeOrgMappingInfo) {

          Map<String, ModelBasicInfo> userInfos = getUserInfoMap(orgId, Arrays.asList(userId),
                  excludeOrgMappingInfo);

          return (UserInfo) userInfos.get(userId);
      }

      public static Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
              Collection<String> userIds) {

          return getUserInfoMap(orgId, userIds, false);
      }

      public static Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
              Collection<String> userIds, boolean excludeOrgMappingInfo) {

          return OrgMemberManager.getUserInfoMap(orgId, userIds, excludeOrgMappingInfo);
      }

      protected void annotateLinkInfo(AbstractFileModelIndexSearchDetails model) {

          if (model.linkInfo != null) {
              model.linkInfo.populate();
          }
      }

      protected void annotateLinkInfo(List<? extends AbstractFileModelIndexSearchDetails> models) {

          if (!CollectionUtils.isEmpty((models)) {
              for (AbstractFileModelIndexSearchDetails model : models) {
                  if (model.linkInfo != null) {
                      model.linkInfo.populate();
                  }
              }
          }
      }*/
    protected void generateEventAysc(final String userId, final VedantuBaseMongoModel model,
                                     final EventActionType action, final EventType eventType,
                                     final UserActionType userAction, final boolean notificationEnabled)
            throws VedantuException {

        try {
            AbstractSearchDetail details = (AbstractSearchDetail) EventDetailsFactory.getInstance()
                    .getDetails(eventType);

            details.userAction = userAction;
            details.isNotificationEnabled = notificationEnabled;

            details.setAction(action.name());
            IMongoAware mongoDetails = details;
            mongoDetails.fromMongoModel(model);
            logger.debug("loaded IndexDetails from mongomodel: " + details);
            generateEventAysc(userId, details, eventType);
        } catch (Exception exception) {
            throw new VedantuException(VedantuErrorCode.EVENT_NOT_SCHEDULED);
        }

    }

    /*@SuppressWarnings("rawtypes")
    protected static void isSocialActionAllowed(EntityType entityType, String entityId)
            throws VedantuException {

        VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(entityType);
        if (dao == null) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED);
        }

        VedantuBaseMongoModel model = dao.getById(entityId, VedantuRecordState.ACTIVE);
        if (model == null) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, "no " + entityType
                    + " found for id[" + entityId + "]");

        }
    }*/

   /* protected static void updateParentCommentsCount(String userId, SrcEntity parent)
            throws VedantuException {

        ICommentable dao = (ICommentable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.debug("updating " + parent + ", comment count");
        VedantuBaseMongoModel model = dao.incCommentsCount(parent.id);
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        LOGGER.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (eventType != null) {
            generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
                    UserActionType.COMMENTED, false);
        }
    }*/

    private static List<BoardTree> toForest(Map<String, BoardBasicInfo> boardBasicInfoMap) {

        logger.trace("toForest boardBasicInfoMap.size: " + boardBasicInfoMap.size());

        List<BoardTree> roots = new ArrayList<BoardTree>();

        if (MapUtils.isEmpty(boardBasicInfoMap)) {
            logger.debug("toForest null/empty boardBasicInfoMap given");
            return roots;
        }

        // Create the basic 1-to-1 map of boardTree
        Map<String, BoardTree> treeMap = new HashMap<String, BoardTree>();
        logger.debug("toForest creating treeMap");
        for (Map.Entry<String, BoardBasicInfo> entry : boardBasicInfoMap.entrySet()) {

            final String id = entry.getKey();
            final BoardBasicInfo board = entry.getValue();

            if (StringUtils.isEmpty(id) || null == board) {
                continue;
            }

            treeMap.put(id, new BoardTree(board));
        }
        logger.debug("toForest created treeMap.size: " + treeMap.size());

        // Populate parent, children
        logger.debug("toForest populating parent/children");
        for (Map.Entry<String, BoardTree> entry : treeMap.entrySet()) {

            final BoardTree boardTree = entry.getValue();

            logger.debug("toForest populating parent/children for board: " + boardTree.board.name);

            if (CollectionUtils.isEmpty(boardTree.board.parentIds)) {
                continue;
            }

            for (String parentId : boardTree.board.parentIds) {
                BoardTree parentBoardTree = treeMap.get(parentId);
                if (null == parentBoardTree) {
                    continue;
                }

                parentBoardTree.children.add(boardTree);
                boardTree.parents.add(parentId);
            }

        }

        logger.trace("toForest selecting root entries");
        // Select root trees of the forest
        for (Map.Entry<String, BoardTree> entry : treeMap.entrySet()) {

            final BoardTree boardTree = entry.getValue();

            if (CollectionUtils.isEmpty(boardTree.parents)) {
                roots.add(boardTree);
            }
        }
        logger.trace("toForest selected root entries, roots.size: " + roots.size());

        logger.trace("toForest setting levels for nodes");
        for (BoardTree root : roots) {
            root._setLevel(0);
        }
        logger.debug("toForest setting levels for nodes completed");

        return roots;
    }

    public void generateEventAysc(final String userId, final IEventDetails details,
                                  final EventType eventType) {

        generateEventAysc(userId, details, eventType, 0);
    }

    public void generateEventAysc(final String userId, final IEventDetails details,
                                  final EventType eventType, final long processTime) {
        CompletableFuture.runAsync(() -> {
            eventUtil.generateEvent(eventType, null, userId, details,
                    details.__getSrcEntity(), EventActionType.ADD, processTime);
        });

    }

    protected static void addSocialActionInfo(String userId, String entityOwnerId,
                                              Map<String, Boolean> entityVoteMap, Map<String, FollowType> followTypeMap,
                                              ISocialEntity entity) {

        entity._setVoted(entityVoteMap != null && entityVoteMap.get(entity._getEntityId()) != null && entityVoteMap
                .get(entity._getEntityId()).booleanValue());
        FollowType followType = userId.equals(entityOwnerId) ? FollowType.OWNER
                : (followTypeMap != null && followTypeMap.get(entity._getEntityId()) != null ? followTypeMap
                .get(entity._getEntityId()) : FollowType.NONE);
        //entity._setFollowType(followType);
    }

    /* /**
     *
     * @param getEntityReq
     * @param entityType
     * @param respObj
     * @param boolQuery
     * @param boolFilter
     * @param facets
     * @param returnedEntityIds
     *            --> provide an empty collections all returned entityIds will be added here
     * @return
     */
  /*  protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            AbstractContentSearchReq getEntityReq, EntityType entityType, Class<T> respObj,
            BoolQueryBuilder boolQuery, BoolFilterBuilder boolFilter,
            AbstractFacetBuilder[] facets, Set<String> returnedEntityIds) {

        return getEntityInfos(getEntityReq.orderBy, getEntityReq.sortOrder, getEntityReq.start,
                getEntityReq.size, entityType, respObj, boolQuery, boolFilter, facets,
                returnedEntityIds);

    }

    protected static Map<String, Boolean> getUpVotesMap(String userId, Set<String> ids,
            EntityType entityType) {

        return null;
    }

    @SuppressWarnings("rawtypes")
    protected static <T extends IListResponseObj> SearchListResponse<T> getSimilarEntityInfos(
            GetSimilarEntities similarEntityReq, Class<T> respObj, Set<String> returnedEntityIds) {

        VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(similarEntityReq.entity.type);
        if (dao == null) {
            return new SearchListResponse<T>();
        }

        AbstractBoardEntityTagModel model = (AbstractBoardEntityTagModel) dao
                .getById(similarEntityReq.entity.id);
        if (model == null) {
            return new SearchListResponse<T>();
        }
        Set<String> brdIds = model.__getAllBoardIds();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // TODO: in case of question we also need to add type filter...
        // if (q.type != null) {
        // esQuery.should(QueryBuilders.fieldQuery(ConstantsGlobal.TYPE,
        // q.type.name().toLowerCase()));
        // }
        boolQuery.mustNot(QueryBuilders.fieldQuery(ConstantsGlobal.ID, similarEntityReq.entity.id));

        if (!brdIds.isEmpty()) {
            boolQuery.should(QueryBuilders.inQuery(ConstantsGlobal.BOARDS_ID, brdIds.toArray()));
            boolQuery.should(QueryBuilders.inQuery(ConstantsGlobal.TARGETS_ID, brdIds.toArray()));
        }
        if (CollectionUtils.isNotEmpty(model.tags)) {
            boolQuery.should(QueryBuilders.inQuery(ConstantsGlobal.TAGS, VedantuStringUtils
                    .toLowerCase(model.tags).toArray()));
        }
        if (model.contentSrc != null) {
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.ID, model.contentSrc.id));
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.TYPE, model.contentSrc.type.name().toLowerCase()));
        }
        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
        ElasticSearchUtils.addScopeFilter(similarEntityReq.userId, boolFilter);

        return getEntityInfos(similarEntityReq.orderBy, similarEntityReq.sortOrder,
                similarEntityReq.start, similarEntityReq.size, similarEntityReq.entity.type,
                respObj, boolQuery, boolFilter, null, returnedEntityIds);

    }

    protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            String orderBy, String sortOrder, int start, int size, EntityType entityType,
            Class<T> respObj, BoolQueryBuilder boolQuery, BoolFilterBuilder boolFilter,
            AbstractFacetBuilder[] facets, Set<String> returnedEntityIds) {

        QueryBuilder esQuery = QueryBuilders.filteredQuery(boolQuery.hasClauses() ? boolQuery
                : QueryBuilders.matchAllQuery(),
                boolFilter != null ? boolFilter : FilterBuilders.matchAllFilter());
        return getEntityInfos(orderBy, sortOrder, start, size, entityType, respObj, esQuery,
                facets, returnedEntityIds);
    }

    protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            String orderBy, String sortOrder, int start, int size, EntityType entityType,
            Class<T> respObj, QueryBuilder esQuery, AbstractFacetBuilder[] facets,
            Set<String> returnedEntityIds) {

        return getEntityInfos(orderBy, sortOrder, start, size, respObj, esQuery, facets,
                returnedEntityIds, entityType.getIndexName(), entityType.getIndexType());
    }

    protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            String orderBy, String sortOrder, int start, int size, Class<T> respObj,
            QueryBuilder esQuery, AbstractFacetBuilder[] facets, Set<String> returnedEntityIds,
            String indexName, String indexType) {

        SearchResponse response = ElasticSearchUtils.getSearchResponse(esQuery, orderBy, sortOrder,
                start, size, indexName, indexType, null, facets);

        if (response == null || response.getHits().getTotalHits() == 0) {
            LOGGER.error("empty search response for query : " + esQuery);
            return new SearchListResponse<T>();
        }

        SearchListResponse<T> listResponse = new SearchListResponse<T>();

        SearchHits allHits = response.getHits();
        listResponse.totalHits = allHits.getTotalHits();
        LOGGER.debug("totalHits: " + listResponse.totalHits);
        for (SearchHit hits : allHits.getHits()) {
            LOGGER.trace("hits : " + hits.sourceAsString());
            T model = ObjectMapperUtils.convertValue(hits.sourceAsMap(), respObj);
            if (returnedEntityIds != null && hits.sourceAsMap().get(ConstantsGlobal.ID) != null) {
                returnedEntityIds.add(hits.sourceAsMap().get(ConstantsGlobal.ID).toString());
            }
            listResponse.list.add(model);
        }
        if (facets != null && facets.length > 0) {
            ElasticSearchUtils.addCommonFacetDetails(listResponse.facet, response);
        }
        return listResponse;
    }

    public static AbstractBoardSearchEntityTagDetails annotateExtraInfo(String userId,
            String orgId, EntityType entityType, AbstractBoardSearchEntityTagDetails entity) {

        List<? extends AbstractBoardSearchEntityTagDetails> entities = Arrays.asList(entity);
        annotateExtraInfo(userId, orgId, entityType, entities);
        return entities.get(0);
    }

    public static void annotateExtraInfo(String userId, String orgId, EntityType entityType,
            List<? extends AbstractBoardSearchEntityTagDetails> entities) {

        annotateExtraInfo(StringUtils.EMPTY, userId, orgId, entityType, entities, false);
    }

    public static void annotateExtraInfo(String secId, String userId, String orgId, EntityType entityType,
            List<? extends AbstractBoardSearchEntityTagDetails> entities) {

        annotateExtraInfo(secId, userId, orgId, entityType, entities, false);
    }

    public static void annotateExtraInfo(String secId, String userId, String orgId, EntityType entityType,
            List<? extends AbstractBoardSearchEntityTagDetails> entities,
            boolean excludeOrgMappingInfo) {

        Set<String> brdIds = new HashSet<String>();

        Set<String> userIds = new HashSet<String>();
        Set<String> entityIds = new HashSet<String>();

        for (AbstractBoardSearchEntityTagDetails details : entities) {
            userIds.add(details.userId);
            entityIds.add(details.id);
            brdIds.addAll(details._getBoardsIds());
        }
        Map<String, BoardBasicInfo> boardsInfoMap = BoardManager.getInfosMap(brdIds);
        // trying to get user details without orgId
        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(null, userIds,
                excludeOrgMappingInfo);

        Map<String, Boolean> entityVoteMap = null;//EntityUserActionDAO.INSTANCE.getEntityUpVoteMap(
                //userId, entityIds);

        Map<String, Boolean> entityAttemptMap = null;
        Map<String, Map<String, Long>> entityStartEndTime = null;
        if (entityType == EntityType.QUESTION || entityType == EntityType.TEST) {
            entityAttemptMap = AnalyticsManager.getEntityAttemptsMap(entityIds, userId);
        }
        if(!StringUtils.isEmpty(secId) && entityType == EntityType.TEST){
            entityStartEndTime = LibraryContentLinksDAO.INSTANCE.getEntityStartEndTime(secId, entityIds);
        }
        Map<String, FollowType> followTypeMap = EntityUserActionDAO.INSTANCE
                .getEntityFollowTypeMap(userId, entityType, entityIds);

        for (AbstractBoardSearchEntityTagDetails details : entities) {
            LOGGER.debug("user info : " + userInfos.get(details.userId) + ", userId: "
                    + details.userId);
            details.boardTree = details.fetchBoardTree(boardsInfoMap);
            LOGGER.debug("details boardTree info : " + details.boardTree);
            details.boards = null;
            if(!StringUtils.isEmpty(secId) && entityType == EntityType.TEST){
                details.startTime = entityStartEndTime.get(details.id).get("startTime");
                details.endTime = entityStartEndTime.get(details.id).get("endTime");
                details.closeTime = entityStartEndTime.get(details.id).get("closeTime");
            }
            if (details instanceof IAttemptableEntity) {
                addAttemptInfo(userId, entityAttemptMap, (IAttemptableEntity) details);
            }
            if (details instanceof ISocialEntity) {
                addSocialActionInfo(userId, details.userId, entityVoteMap, followTypeMap,
                        (ISocialEntity) details);
            }
            if (details instanceof IReverseImageMapperProcessor) {
                ((IReverseImageMapperProcessor) details).addImageSrcUrl();
            }
            details.user = userInfos.get(details.userId);
        }
    }

    public static String addLiveEntityToSearchIndex(final AbstractSearchDetail details,
            final EntityType entityType) {

        return addLiveEntityToSearchIndex(details, entityType, false);
    }

    @SuppressWarnings("unchecked")
    public static String addLiveEntityToSearchIndex(final AbstractSearchDetail details,
            final EntityType entityType, final boolean ensureQueryState) {

        QueryBuilder esQuery = QueryBuilders.termQuery(details._getUniqueId().getName(), details
                ._getUniqueId().getValue());
        SearchHit searchHit = ElasticSearchUtils.findOne(entityType.getIndexName(),
                entityType.getIndexType(), esQuery);
        LOGGER.debug(" searchHit " + searchHit);
        if (searchHit != null) {
            LOGGER.debug("entity already indexed ");
            String id = ElasticSearchManager.getInstance().reIndex(searchHit.getIndex(),
                    ObjectMapperUtils.convertValue(details, Map.class), searchHit.getId(),
                    searchHit.getType());
            if (StringUtils.isNotEmpty(id)) {
                ContentManager.addOrUpdateContentSearchDetails(details);
            }

            return id;
        }
        LOGGER.debug("Indexing entity Live now ");
        String esId = ElasticSearchManager.getInstance().addIndex(entityType.getIndexName(),
                entityType.getIndexType(), ObjectMapperUtils.convertValue(details, Map.class));

        if (StringUtils.isNotEmpty(esId)) {
            ContentManager.addOrUpdateContentSearchDetails(details, ensureQueryState);
        }

        if (StringUtils.isNotEmpty(esId) && ensureQueryState) {
            boolean isQueriable = false;

            int tryCount = 0;
            while (!isQueriable && tryCount < ES_ENSURE_QUERY_STATE_MAX_TRY_COUNT) {
                tryCount++;
                LOGGER.debug("tryCount: " + tryCount + ", query:" + esQuery);
                searchHit = ElasticSearchUtils.findOne(entityType.getIndexName(),
                        entityType.getIndexType(), esQuery);
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
        return esId;
    }

    public static boolean removeLiveEntityToSearchIndex(final String id,
            final EntityType entityType, final boolean ensureQueryState) {

        boolean result = ElasticSearchManager.getInstance().removeEntry(entityType.getIndexName(),
                entityType.getIndexType(), ConstantsGlobal.ID, id);

        FieldQueryBuilder esQuery = QueryBuilders.fieldQuery(ConstantsGlobal.ID, id);
        SearchHit searchHit = null;
        boolean isQueriable = true;
        if (result && ensureQueryState) {
            int tryCount = 0;
            while (isQueriable && tryCount < ES_ENSURE_QUERY_STATE_MAX_TRY_COUNT) {
                tryCount++;
                LOGGER.debug("tryCount: " + tryCount);
                searchHit = ElasticSearchUtils.findOne(entityType.getIndexName(),
                        entityType.getIndexType(), esQuery);
                if (searchHit == null) {
                    isQueriable = false;
                } else {
                    try {

                        Thread.sleep(ELASTIC_SEARCH_REFRESH_TIME);

                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
        return !isQueriable;
    }

    protected static void generateEventAysc(final String userId, final VedantuBaseMongoModel model,
            final EventActionType action, final EventType eventType,
            final UserActionType userAction, final boolean notificationEnabled)
            throws VedantuException {

        try {
            AbstractSearchDetail details = (AbstractSearchDetail) EventDetailsFactory.getInstance()
                    .getDetails(eventType);

            details.userAction = userAction;
            details.isNotificationEnabled = notificationEnabled;

            details.setAction(action.name());
            IMongoAware mongoDetails = details;
            mongoDetails.fromMongoModel(model);
            LOGGER.debug("loaded IndexDetails from mongomodel: " + details);
            generateEventAysc(userId, details, eventType);
        } catch (Exception exception) {
            throw new VedantuException(VedantuErrorCode.EVENT_NOT_SCHEDULED);
        }

    }

    @SuppressWarnings("unchecked")
    protected static boolean
            delete(final String userId, EventType indexEventType, SrcEntity content)
                    throws VedantuException {

        @SuppressWarnings({ "rawtypes" })
        VedantuBasicDAO contentDAO = EntityTypeDAOFactory.INSTANCE.get(content.type);
        VedantuBaseMongoModel basicMongoModel = contentDAO.getById(content.id);
        if (basicMongoModel == null) {
            return false;
        }
        basicMongoModel.recordState = VedantuRecordState.DELETED;
        contentDAO.save(basicMongoModel);

        if (basicMongoModel instanceof IIndexable) {

            generateEventAysc(userId, basicMongoModel, EventActionType.REMOVE, indexEventType,
                    UserActionType.DELETED, false);
        }

        NewsRemoveDetails newsRemoveDetails = new NewsRemoveDetails();
        newsRemoveDetails.content = content;
        AbstractVedantuEventManager.generateEventAysc(userId, newsRemoveDetails,
                EventType.REMOVE_NEWS);
        return true;
    }*/

    protected static void addAttemptInfo(String userId, Map<String, Boolean> entityAttemptMap,
                                         IAttemptableEntity entity) {

        entity._setAttempted(entityAttemptMap != null
                && entityAttemptMap.get(entity._getEntityId()) != null && entityAttemptMap.get(
                entity._getEntityId()).booleanValue());
    }

    /*@SuppressWarnings("rawtypes")
    protected static void isSocialActionAllowed(EntityType entityType, String entityId)
            throws VedantuException {

        VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(entityType);
        if (dao == null) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED);
        }

        VedantuBaseMongoModel model = dao.getById(entityId, VedantuRecordState.ACTIVE);
        if (model == null) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, "no " + entityType
                    + " found for id[" + entityId + "]");

        }
    }*/

   /* protected static void updateParentCommentsCount(String userId, SrcEntity parent)
            throws VedantuException {

        ICommentable dao = (ICommentable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.debug("updating " + parent + ", comment count");
        VedantuBaseMongoModel model = dao.incCommentsCount(parent.id);
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        LOGGER.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (eventType != null) {
            generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
                    UserActionType.COMMENTED, false);
        }
    }*/

   /* protected static void updateParentUpVotesCount(String userId, SrcEntity parent)
            throws VedantuException {

        IUpVotable dao = (IUpVotable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.info("updating " + parent + ", vote count");
        VedantuBaseMongoModel model = dao.incUpVotesCount(parent.id);
        LOGGER.debug("updated model : " + model);
        // if (model instanceof IIndexable) {
        // EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        // Logger.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        // if (eventType != null) {
        // generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
        // UserActionType.VOTED, false);
        // }
        // }

    }*/

   /* protected static void updateParentFollowersCount(String userId, SrcEntity parent, int inc)
            throws VedantuException {

        IFollowable dao = (IFollowable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.debug("updating " + parent + ", follower count");
        VedantuBaseMongoModel model = dao.incFollowersCount(parent.id, inc);
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        LOGGER.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (eventType != null && inc < 0) {
            // inc > 0 FOLLOW && inc < 0 UNFOLLOW in case of UNFOLLOW we need not send news feed so
            // just re-index the entity, in case of follow EntityUserActionUtils.updateEntityCount
            // method will be used
            generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
                    UserActionType.FOLLOWING, false);
        }
    }

    protected static void updateAttemptsCount(String userId, SrcEntity parent, int inc)
            throws VedantuException {

        IAttemptable dao = (IAttemptable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.info("updating " + parent + ", attempt count");
        VedantuBaseMongoModel model = dao.incAttemptsCount(parent.id, 1);
        LOGGER.debug("updated model: " + model);

    }

    protected static void updateViewsCount(String userId, SrcEntity parent, int inc)
            throws VedantuException {

        IViewable dao = (IViewable) EntityTypeDAOFactory.INSTANCE.get(parent.type);
        LOGGER.debug("updating " + parent + ", view count");
        VedantuBaseMongoModel model = dao.incViewsCount(parent.id, 1);
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        LOGGER.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (eventType != null) {
            generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
                    UserActionType.ATTEMPTED, false);
        }
    }

    protected static BoolQueryBuilder buildSearchQuery(AbstractContentSearchReq searchReq,
            EntityType entityType) {

        MutableLong totalProgramHits = new MutableLong(0L);
        List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE
                .getGranteeOrgPrograms(searchReq.orgId, null, totalProgramHits);
        List<String> grantedOrgs = new ArrayList<String>();
        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            grantedOrgs.add(granteeOrgProgram.providerOrgId);
        }
        BoolQueryBuilder boolQuery = buildSearchQuery(searchReq.resultType,
                searchReq._getResultForUserId(), searchReq.contentSrc, searchReq.includeTypes,
                searchReq.excludeTypes, searchReq.excludeIds, searchReq.query, entityType,
                grantedOrgs);
        try {
            addOrgStructureFilter(searchReq, boolQuery);
        } catch (VedantuException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return boolQuery;
    }

    protected static BoolQueryBuilder buildSearchQuery_(SearchResultType resultType, String userId,
            SrcEntity contentSrc, Collection<String> includeTypes, Collection<String> excludeTypes,
            List<String> excludeIds, String query, EntityType entityType) {
        return buildSearchQuery(resultType,
                userId, contentSrc, includeTypes,
                excludeTypes, excludeIds, query, entityType,null);

    };

    protected static BoolQueryBuilder buildSearchQuery(SearchResultType resultType, String userId,
            SrcEntity contentSrc, Collection<String> includeTypes, Collection<String> excludeTypes,
            List<String> excludeIds, String query, EntityType entityType, List<String> grantorOrgIds) {

        LOGGER.debug(" Getting " + entityType + " for user  :" + userId + " with query " + query);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (contentSrc != null) {
            if (contentSrc.type.name() != EntityType.ORGANIZATION.toString()) {
                boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID, contentSrc.id));
            } else if (entityType == EntityType.DISCUSSION) {
                Organization org = OrganizationDAO.INSTANCE.getById(contentSrc.id);
                ArrayList<String> allOrgs = new ArrayList<String>();
                allOrgs.add(contentSrc.id);
                switch (org.doubtsForumMode) {
                case PRIVATE:
                    break;
                case PUBLIC:
                    allOrgs.add(Play.application().configuration().getString("learnpedia.id"));
                    break;
                default:
                    break;
                }
                boolQuery.must(QueryBuilders.termsQuery(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID, allOrgs.toArray()));
            } else {
                ArrayList<String> allOrgs = new ArrayList<String>();
                allOrgs.add(contentSrc.id);
                if (grantorOrgIds != null && grantorOrgIds.size() > 0) {
                    allOrgs.addAll(grantorOrgIds);
                }
                boolQuery.must(QueryBuilders.termsQuery(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID, allOrgs.toArray()));
            }
            boolQuery.must(QueryBuilders.termQuery(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.TYPE, contentSrc.type.name().toLowerCase()));

        }

        resultType.addSearchQueryFlter(boolQuery, userId);

        if (CollectionUtils.isNotEmpty(excludeIds)) {
            boolQuery.mustNot(QueryBuilders.inQuery(ConstantsGlobal.ID, excludeIds.toArray()));
        }
        if (CollectionUtils.isNotEmpty(includeTypes)) {
            boolQuery.must(QueryBuilders.inQuery(ConstantsGlobal.TYPE, VedantuStringUtils
                    .toLowerCase(includeTypes).toArray()));
        }
        if (CollectionUtils.isNotEmpty(excludeTypes)) {
            boolQuery.mustNot(QueryBuilders.inQuery(ConstantsGlobal.TYPE, VedantuStringUtils
                    .toLowerCase(excludeTypes).toArray()));
        }

        if (StringUtils.isNotEmpty(query)) {
            boolQuery.must(QueryBuilders.queryString(query.toLowerCase()));
        }
        return boolQuery;
    }

    protected static BoolFilterBuilder buildSearchFilter(AbstractContentSearchReq searchReq,
            EntityType entityType) {

        return buildSearchFilter(searchReq, entityType, false);
    }

    protected static BoolFilterBuilder buildSearchFilter(AbstractContentSearchReq searchReq,
            EntityType entityType, boolean noScopeFilter) {

        String userId = searchReq._getResultForUserId();

        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
        ElasticSearchUtils.addBoardAndTargetFilter(searchReq.brdIds, searchReq.allBrds, boolFilter);
        if (!noScopeFilter) {
            ElasticSearchUtils.addScopeFilter(userId, boolFilter);
        } else {
            boolFilter.must(FilterBuilders.termsFilter(ConstantsGlobal.SCPOE, Scope.PUBLIC.name()
                    .toLowerCase(), Scope.ORG.name().toLowerCase()));
        }
        return boolFilter;
    }

    private static void addOrgStructureFilter(AbstractContentSearchReq searchReq,
            BoolQueryBuilder boolQuery) throws VedantuException {

    	// checking if current OrgId and  programId are not empty
        if (StringUtils.isNotEmpty(searchReq.orgId) && StringUtils.isNotEmpty(searchReq.programId)) {
        	//Marking current orgId to programOrgId
        	 String programOrgId=searchReq.orgId;
        	 MutableLong totalProgramHits = new MutableLong(0L);
        	 LOGGER.debug("Current orgId"+searchReq);
        	 // getting all the rows of the current orgId as OrgId as key
             List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(searchReq.orgId, null, totalProgramHits);
             for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            	 // if programId from search request matches the programId from the list above assign the granteeId as the programId
            	 if(granteeOrgProgram.programId.equalsIgnoreCase(searchReq.programId)){
            		 programOrgId=granteeOrgProgram.providerOrgId;
            	 }
     		}

            Collection<String> sectionIds = StringUtils.isNotEmpty(searchReq.sectionId) ? Arrays
                    .asList(searchReq.sectionId) : OrgProgramManager.getProgramSections(
                    programOrgId,
                    searchReq.programId,
                    StringUtils.isEmpty(searchReq.centerId) ? null : Arrays
                            .asList(searchReq.centerId));
            String childType = UserActionType.ADDED.getSearchIndexType();
            boolQuery.must(QueryBuilders.hasChildQuery(childType,
                    QueryBuilders.inQuery(childType + ".dst.id", sectionIds.toArray())));
        }
    }

    protected static <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            AbstractContentSearchReq searchReq, EntityType entityType, Class<T> respObj,
            Set<String> returnedEntityIds) {

        AbstractFacetBuilder[] facets = ElasticSearchUtils.getBoardsTagFacets(searchReq.size);
        BoolQueryBuilder boolQuery = buildSearchQuery(searchReq, entityType);
        BoolFilterBuilder boolFilter = buildSearchFilter(searchReq, entityType);
        if (CollectionUtils.isNotEmpty(searchReq.includeModes)) {
            boolFilter.must(FilterBuilders.inFilter(ConstantsGlobal.MODE, VedantuStringUtils
                    .toLowerCase(searchReq.includeModes).toArray()));
        }
        if (CollectionUtils.isNotEmpty(searchReq.includeTypes)) {
            boolFilter.must(FilterBuilders.inFilter(ConstantsGlobal.TYPE, VedantuStringUtils
                    .toLowerCase(searchReq.includeTypes).toArray()));
        }
        if (CollectionUtils.isNotEmpty(searchReq.includeDifficulty)) {
            boolFilter.must(FilterBuilders.inFilter(ConstantsGlobal.DIFFICULTY, VedantuStringUtils
                    .toLowerCase(searchReq.includeDifficulty).toArray()));
        }
        if (CollectionUtils.isNotEmpty(searchReq.scope)) {
            boolFilter.must(FilterBuilders.inFilter(ConstantsGlobal.SCPOE, VedantuStringUtils
                    .toLowerCase(searchReq.scope).toArray()));
        }
        return getEntityInfos(searchReq.orderBy, searchReq.sortOrder, searchReq.start,
                searchReq.size, entityType, respObj, boolQuery, boolFilter, facets,
                returnedEntityIds);

    }*/


    public void updateUserActionMappintToEs(EntityUserActionRelationshipSearchDetails details,
                                            SrcEntity parent, UserActionType actionType, EventActionType eventAction) {

        updateUserActionMappintToEs(details, parent, actionType, eventAction, null);
    }

    public void updateUserActionMappintToEs(EntityUserActionRelationshipSearchDetails details,
                                            SrcEntity parent, UserActionType actionType, EventActionType eventAction,
                                            String parentEsId) {

        updateUserActionMappintToEs(details, parent, actionType.getSearchIndexType(), eventAction,
                parentEsId);
    }


    public void updateUserActionMappintToEs(EntityUserActionRelationshipSearchDetails details,
                                            SrcEntity parent, String indexType, EventActionType eventAction, String parentEsId) {

        updateUserActionMappintToEs(details, parent, parent.type.getIndexName(),
                parent.type.getIndexType(), indexType, eventAction, parentEsId);

    }

    public void updateUserActionMappintToEs(EntityUserActionRelationshipSearchDetails details,
                                            SrcEntity parent, String indexName, String indexType, String mappingIndexType,
                                            EventActionType eventAction, String parentEsId) {

        if (StringUtils.isEmpty(indexName) || StringUtils.isEmpty(indexType)) {
            return;
        }
       /* QueryBuilder esQuery = QueryBuilders.termQuery(ConstantsGlobal.ID, parent.id);

        // we already have the es parent _id
        SearchHit searchHit = StringUtils.isNotEmpty(parentEsId) ? null : ElasticSearchUtils.findOne(indexName, indexType, esQuery);
        if (StringUtils.isNotEmpty(parentEsId) || searchHit != null) {
            if (searchHit != null) {
                parentEsId = searchHit.getId();
            }
            if (eventAction == EventActionType.ADD || eventAction == EventActionType.UPDATE) {
                logger.info("adding details to es mapping : " + details + " with parent type "
                        + parent.type + "  and parent es Id " + parentEsId);
                //ElasticSearchUtils.addMappingToES(indexName, mappingIndexType.toLowerCase(), details, parentEsId, true);
            } else if (eventAction == EventActionType.REMOVE) {

                logger.debug("removing details to es mapping : " + details + " with parent type "
                        + parent.type + "  and parent es Id " + parentEsId);

                //ElasticSearchManager.getInstance().removeEntry(searchHit.getIndex(), mappingIndexType.toLowerCase(), details._getEsQuery());

            }
        } else {
            logger.debug("no hits found for query:" + esQuery);
        }*/
    }


    public AbstractBoardSearchEntityTagDetails annotateExtraInfo(String userId, String orgId,
                                                                 EntityType entityType, AbstractBoardSearchEntityTagDetails entity) {

        List<? extends AbstractBoardSearchEntityTagDetails> entities = Arrays.asList(entity);
        annotateExtraInfo(userId, orgId, entityType, entities);
        return entities.get(0);
    }

    public void annotateExtraInfo(String userId, String orgId, EntityType entityType,
                                  List<? extends AbstractBoardSearchEntityTagDetails> entities) {

        annotateExtraInfo("", userId, orgId, entityType, entities, false);
    }

    public void annotateExtraInfo(String secId, String userId, String orgId, EntityType entityType,
                                  List<? extends AbstractBoardSearchEntityTagDetails> entities) {

        annotateExtraInfo(secId, userId, orgId, entityType, entities, false);
    }

    public static List<BoardTreeRes> toBoardTreeRes(Collection<BoardTree> boardTree) {

        List<BoardTreeRes> boardTreeRes = new ArrayList<BoardTreeRes>();
        for (BoardTree board : boardTree) {
            BoardTreeRes b = new BoardTreeRes();
            b.name = board.board.name;
            b.id = board.board.id;
            b.code = board.board.code;
            b.treeName = board.board.treeName;
            b.type = board.board.type;
            b.children = toBoardTreeRes(board.children);
            boardTreeRes.add(b);
        }
        logger.error("returning boards tree : " + boardTreeRes);
        return boardTreeRes;
    }

    protected void validateBoardIds(Set<String> allBordIds) throws VedantuException {

        if (CollectionUtils.isEmpty(allBordIds)) {
            int count = boardRepo.findAllByIdIn(allBordIds).size();
            if (count != allBordIds.size()) {
                logger.error("some boards ids from the list [" + allBordIds + "] are not valid");
                throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND);
            }
        }
    }

    public Map<String, BoardBasicInfo> getBasicInfosByIds(Set<String> ids) {

        //logger.debug("getBasicInfosByIds ids: {" + StringUtils.join(ids, ", ") + "}");
        List<Board> results = getBoardByIds(ids);
        Map<String, BoardBasicInfo> basicInfoMap = toBasicInfosMap(results);
        //logger.info("getBasicInfosByIds basicInfoMap: {" + StringUtils.join(basicInfoMap, ", ")+ "}");
        return basicInfoMap;
    }

    private Map<String, BoardBasicInfo> toBasicInfosMap(List<Board> results) {
        Map<String, BoardBasicInfo> infosMap = new LinkedHashMap<String, BoardBasicInfo>();
        for (Board board : results) {
            BoardBasicInfo basicInfo = new BoardBasicInfo(board);
            infosMap.put(board._getStringId(), basicInfo);
        }
        return infosMap;
    }

    private List<Board> getBoardByIds(Set<String> ids) {
        // TODO Auto-generated method stub
        return boardRepo.findAllByIdIn(ids);
    }

    public Map<String, ModelBasicInfo> getUserInfoMap(String orgId, Collection<String> userIds,
                                                      boolean excludeOrgMappingInfo) {

        logger.info("getUserInfoMap orgId:" + orgId + ", userIds: " + userIds);
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<String, ModelBasicInfo>();
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        DBObject memberQuery = new BasicDBObject();

        boolean isOrgReq = !StringUtils.isEmpty(orgId);

        if (isOrgReq) {
            criteria.and(ConstantsGlobal.ORG_ID).is(orgId);
            criteria.and(ConstantsGlobal.USER_ID).in(userIds);

        } else {
            criteria.and(ConstantsGlobal._ID).in(userIds);
        }
        query.addCriteria(criteria);
        List<OrgMember> orgMemberList = mongoTemplate.find(query, OrgMember.class);
        List<User> users = mongoTemplate.find(query, User.class);

        Map<String, ModelBasicInfo> userIdToBasicInfoMap = isOrgReq
                ? populateOrgMemberInfo(orgMemberList, excludeOrgMappingInfo)
                : toBasicUserInfosMap(users);

        logger.debug("userIds map : " + userIdToBasicInfoMap);
        return userIdToBasicInfoMap;

    }

    public void annotateExtraInfo(String secId, String userId, String orgId, EntityType entityType,
                                  List<? extends AbstractBoardSearchEntityTagDetails> entities,
                                  boolean excludeOrgMappingInfo) {

        Set<String> brdIds = new HashSet<String>();

        Set<String> userIds = new HashSet<String>();
        Set<String> entityIds = new HashSet<String>();

        for (AbstractBoardSearchEntityTagDetails details : entities) {
            userIds.add(details.userId);
            entityIds.add(details.id);
            brdIds.addAll(details._getBoardsIds());
        }
        Map<String, BoardBasicInfo> boardsInfoMap = getBasicInfosByIds(brdIds);
        // trying to get user details without orgId
        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(null, userIds,
                excludeOrgMappingInfo);

        Map<String, Boolean> entityVoteMap = getEntityUpVoteMap(userId, entityIds);
        Map<String, Boolean> entityAttemptMap = null;
        Map<String, Map<String, Long>> entityStartEndTime = null;
        if (entityType == EntityType.QUESTION || entityType == EntityType.TEST) {
            entityAttemptMap = getEntityAttemptsMap(entityIds, userId);
        }
        if (!StringUtils.isEmpty(secId) && entityType == EntityType.TEST) {
            entityStartEndTime = getEntityStartEndTime(secId, entityIds);
        }
        Map<String, FollowType> followTypeMap =
                getEntityFollowTypeMap(userId, entityType, entityIds);

        for (AbstractBoardSearchEntityTagDetails details : entities) {
            logger.debug("user info : " + userInfos.get(details.userId) + ", userId: "
                    + details.userId);
            Map<String, BoardBasicInfo> boardSearchEntityMap = details.fetchBoardSearchEntityLocalMap(boardsInfoMap);
            details.boardTree = fetchBoardTree(boardSearchEntityMap);
            logger.debug("details boardTree info : " + details.boardTree);
            details.boards = null;
            if (!StringUtils.isEmpty(secId) && entityType == EntityType.TEST) {
                details.startTime = entityStartEndTime.get(details.id).get("startTime");
                details.endTime = entityStartEndTime.get(details.id).get("endTime");
                details.closeTime = entityStartEndTime.get(details.id).get("closeTime");
            }
            if (details instanceof IAttemptableEntity) {
                addAttemptInfo(userId, entityAttemptMap, (IAttemptableEntity) details);
            }
            if (details instanceof ISocialEntity) {
                addSocialActionInfo(userId, details.userId, entityVoteMap, followTypeMap,
                        (ISocialEntity) details);
            }
            if (details instanceof IReverseImageMapperProcessor) {
                ((IReverseImageMapperProcessor) details).addImageSrcUrl();
            }
            details.user = userInfos.get(details.userId);
        }
    }

    public Map<String, FollowType> getEntityFollowTypeMap(String userId, EntityType entityType,
                                                          Set<String> ids) {

        Map<String, FollowType> userEntityFollowTypeMap = new HashMap<String, FollowType>();
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(ids)) {
            return userEntityFollowTypeMap;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ACTION_TYPE).is(userId);
        criteria.and(ConstantsGlobal.TARGET + "." + ConstantsGlobal.ID).in(ids.toArray());
        query.addCriteria(criteria);
        query.fields().include(ConstantsGlobal.TARGET);
        List<EntityUserActionMapping> results = mongoTemplate.find(query, EntityUserActionMapping.class);
        Set<String> followingEntityIds = new HashSet<String>();
        for (EntityUserActionMapping eMapping : results) {
            followingEntityIds.add(eMapping.target.id);
        }

        Set<String> followerEntityIds = new HashSet<String>();
        if (entityType == EntityType.USER) {
            // this block ensure addition of followType, entityType=USER
            Query query1 = new Query();
            Criteria criteria1 = new Criteria();
            criteria1.and(ConstantsGlobal.USER_ID).in(ids.toArray());
            criteria1.and(ConstantsGlobal.TARGET + "." + ConstantsGlobal.ID).is(userId);
            query1.addCriteria(criteria1);
            query1.fields().include(ConstantsGlobal.USER_ID);
            List<EntityUserActionMapping> entityUserActionMappings = mongoTemplate.find(query1, EntityUserActionMapping.class);

            for (EntityUserActionMapping eMapping : entityUserActionMappings) {
                followerEntityIds.add(eMapping.userId);
            }
        }

        for (String id : ids) {
            boolean isFollowing = followingEntityIds.contains(id);
            boolean isFollower = followerEntityIds.contains(id);
            FollowType followType = userId.equals(id) ? FollowType.YOU : (isFollowing
                    && isFollower ? FollowType.BOTH_WAYS : (isFollowing ? FollowType.FOLLOWING
                    : (isFollower ? FollowType.FOLLOWER : FollowType.NONE)));
            userEntityFollowTypeMap.put(id, followType);
        }
        return userEntityFollowTypeMap;
    }


    public final <B extends ModelBasicInfo> Map<String, B> toBasicInfosMapOrgProgram(List<OrgProgram> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (!CollectionUtils.isEmpty(results)) {
            for (OrgProgram orgProgram : results) {
                if (null == orgProgram) {
                    continue;
                }
                infosMap.put(orgProgram._getStringId(), (B) toProgramBasicInfo(orgProgram));

            }
        }
        return infosMap;
    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicCenterInfosMap(List<OrgCenter> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (!CollectionUtils.isEmpty(results)) {
            for (OrgCenter orgCenter : results) {
                if (null == orgCenter) {
                    continue;
                }
                infosMap.put(orgCenter._getStringId(), (B) new OrgStructureBasicInfo(orgCenter));

            }
        }
        return infosMap;
    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicSectionInfosMap(List<OrgSection> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (!CollectionUtils.isEmpty(results)) {
            for (OrgSection orgSection : results) {
                if (null == orgSection) {
                    continue;
                }
                infosMap.put(orgSection._getStringId(), (B) new OrgStructureBasicInfo(orgSection));

            }
        }
        return infosMap;
    }

    private Map<String, ModelBasicInfo> populateOrgMemberInfo(List<OrgMember> orgMembers,
                                                              boolean excludeOrgMappingInfo) {

        Set<String> centerIds = new HashSet<String>();
        Set<String> sectionIds = new HashSet<String>();
        Set<String> programIds = new HashSet<String>();

        if (!excludeOrgMappingInfo) {
            for (OrgMember orgMember : orgMembers) {
                if (orgMember.mappings == null) {
                    continue;
                }
                for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                    if (null == mapping) {
                        continue;
                    }
                    programIds.add(mapping.programId);
                    centerIds.add(mapping.centerId);
                    sectionIds.add(mapping.sectionId);
// if (mapping.courseIds != null) {
// courseIds.addAll(mapping.courseIds);
// }
                }
            }
        }

        logger.debug("programIds : " + programIds + " excludeMappingInfo : " + excludeOrgMappingInfo);
        logger.debug("centerIds : " + centerIds);
        logger.debug("sectionIds : " + sectionIds);
        Map<String, ModelBasicInfo> orgComponentBasicInfoMap = new HashMap<String, ModelBasicInfo>();
// collect program info
        if (!excludeOrgMappingInfo) {

            List<OrgProgram> orgPrograms = orgProgramRepo.findAllByIdIn(programIds);
            if (!orgPrograms.isEmpty())
                orgComponentBasicInfoMap.putAll(toBasicInfosMapOrgProgram(orgPrograms));

// collect center info
            List<OrgCenter> orgCenters = orgCenterRepo.findAllByIdIn(centerIds);
            if (!orgCenters.isEmpty())
                orgComponentBasicInfoMap.putAll(toBasicCenterInfosMap(orgCenters));

// collect section info

            List<OrgSection> orgSections = orgSectionRepo.findAllByIdIn(sectionIds);
            if (!orgSections.isEmpty())
                orgComponentBasicInfoMap.putAll(toBasicSectionInfosMap(orgSections));

        }
        Map<String, ModelBasicInfo> userInfoMap = new HashMap<String, ModelBasicInfo>();

        for (OrgMember orgMember : orgMembers) {
            OrgMemberBasicInfo orgMemberBasicInfo = toBasicInfoforOrgMember(orgMember);
            if (!excludeOrgMappingInfo && orgMember.mappings != null) {
                for (OrgMemberMappingInfo mapping : orgMember.mappings) {
                    if (null == mapping) {
                        continue;
                    }
                    OrgStructureBasicInfo program = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.programId);
                    logger.debug("programId : " + mapping.programId);
                    if (program == null) {
                        continue;
                    }

                    OrgProgramBasicInfo programInfo = orgMemberBasicInfo.mappings._getOrAddProgram(program);

                    OrgStructureBasicInfo progCenter = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.centerId);
                    OrgProgramCenterBasicInfo progCenterInfo = programInfo._getOrAddProgramCenter(progCenter);

                    OrgStructureBasicInfo progSection = (OrgStructureBasicInfo) orgComponentBasicInfoMap
                            .get(mapping.sectionId);
                    if (progSection != null) {
                        OrgProgramSectionBasicInfo progSectionInfo = progCenterInfo._getOrAddProgramSection(progSection);
                        logger.debug("OrgProgramSectionBasicInfo :" + progSectionInfo);
                    }
                }
            }
            userInfoMap.put(orgMember.userId, orgMemberBasicInfo);
        }

        return userInfoMap;
    }

    public final <B extends ModelBasicInfo> Map<String, B> toBasicUserInfosMap(List<User> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (!CollectionUtils.isEmpty(results)) {
            for (User user : results) {
                if (null == user) {
                    continue;
                }
                infosMap.put(user._getStringId(), (B) user.toBasicInfo());

            }
        }
        return infosMap;
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
                    logger.debug("answerJSON: " + answerJSON);
                    info.put("answer", answerJSON);

                    logger.debug("solutionJSON: " + solutionsRes);

                    if (solutionsRes != null && !CollectionUtils.isEmpty(solutionsRes.list)) {
                        JSONObject solutionJSON = new JSONObject(ObjectMapperUtils.convertValue(
                                solutionsRes.list.get(0), Map.class));
                        info.put("solution", solutionJSON);
                    }

                } catch (JSONException e) {
                }
                sDetails.setInfo(info.toString());
            }
        }
    }

    protected void annotateUserSocialActionInfos(String orgId, String userId, EntityType entityType,
                                                 List<? extends AbstractContentUserActionRes> entites, Set<String> userIds, Set<String> entityIds) {

        Map<String, ModelBasicInfo> userInfoMap = getUserInfoMap(orgId, userIds, false);
        Map<String, Boolean> entityVoteMap = getEntityUpVoteMap(userId, entityIds);
        Map<String, FollowType> followTypeMap = getEntityFollowTypeMap(userId, entityType, entityIds);
        for (AbstractContentUserActionRes res : entites) {
            String usrId = res.user.id;
            addSocialActionInfo(userId, usrId, entityVoteMap, followTypeMap, res);
            res.user = (UserInfo) userInfoMap.get(usrId);
        }
    }

    public Map<String, Boolean> getEntityUpVoteMap(String userId, Set<String> ids) {

        Map<String, Boolean> userActionMap = new HashMap<String, Boolean>();
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(ids)) {
            return userActionMap;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.TARGET + "." + ConstantsGlobal.ID).in(ids);
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        criteria.and(ConstantsGlobal.ACTION_TYPE).is(UserActionType.VOTED.name());
        query.addCriteria(criteria);
        query.fields().include(ConstantsGlobal.TARGET);
        query.skip(0).limit(0);
        List<EntityUserActionMapping> results = mongoTemplate.find(query, EntityUserActionMapping.class);
        for (EntityUserActionMapping e : results) {
            userActionMap.put(e.target.id, true);
        }
        return userActionMap;
    }

    private List<BoardTreeRes> fetchBoardTree(Map<String, BoardBasicInfo> boardBasicInfoMap) {
        List<BoardTree> boardTree = getTreesByInfosMap(boardBasicInfoMap).list;
        return toBoardTreeRes(boardTree);
    }

    private GetTreesRes getTreesByInfosMap(Map<String, BoardBasicInfo> boardBasicInfoMap) {
        GetTreesRes getTreesRes = new GetTreesRes();

        if (MapUtils.isEmpty(boardBasicInfoMap)) {
            logger.debug("getTreesByInfosMap no boardBasicInfoMap given");
            return getTreesRes;
        }

        List<BoardTree> roots = toForest(boardBasicInfoMap);
        getTreesRes.list = roots;
        getTreesRes.totalHits = roots.size();

        logger.trace("getTreesByInfosMap roots.size: " + roots.size());

        return getTreesRes;
    }

    public Map<String, Boolean> getEntityAttemptsMap(Set<String> entityIds, String userId) {

        Map<String, Boolean> entityAttemptsMap = new HashMap<String, Boolean>();
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(entityIds)) {
            logger.error("empty entityIds : " + entityIds);
            return entityAttemptsMap;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("entity.id").in(entityIds);
        criteria.and(ConstantsGlobal.USER_ID).is(userId);
        logger.debug("getEntityAttemptsMap query : " + query);
        query.addCriteria(criteria);
        List<UserEntityAttempt> userEntityAttempts = mongoTemplate.find(query, UserEntityAttempt.class);
        for (UserEntityAttempt entityAttempt : userEntityAttempts) {
            entityAttemptsMap.put(entityAttempt.entity.id, Boolean.valueOf(true));
        }
        logger.debug("returning attempts entity map : " + entityAttemptsMap);
        return entityAttemptsMap;
    }

    public Map<String, Map<String, Long>> getEntityStartEndTime(String secId, Set<String> entityIds) {
        // TODO Auto-generated method stub
        Map<String, Map<String, Long>> entityStartTimeMap = new HashMap<String, Map<String, Long>>();
        if (StringUtils.isEmpty(secId) || CollectionUtils.isEmpty(entityIds)) {
            logger.error("empty secId or entityIds : ");
            return entityStartTimeMap;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("source.id").in(entityIds);
        criteria.and(ConstantsGlobal.TARGET_DOT_ID).is(secId);
        criteria.and(ConstantsGlobal.RECORD_STATE).is(VedantuRecordState.ACTIVE.name());
        logger.debug("getEntityStartTime query : " + query);
        List<LibraryContentLink> libraryContentLinks = mongoTemplate.find(query, LibraryContentLink.class);
        for (LibraryContentLink entityStartTime : libraryContentLinks) {
            Map<String, Long> time = new HashMap<String, Long>();
            if (entityStartTime.getSchedule() != null) {
                time.put("startTime", entityStartTime.getSchedule().startTime == null ? Long.MIN_VALUE : entityStartTime.getSchedule().startTime.getTime());
                time.put("endTime", entityStartTime.getSchedule().endTime == null ? Long.MIN_VALUE : entityStartTime.getSchedule().endTime.getTime());
                time.put("closeTime", entityStartTime.getSchedule().closeTime == null ? Long.MIN_VALUE : entityStartTime.getSchedule().closeTime.getTime());
            } else {
                time.put("startTime", Long.MIN_VALUE);
                time.put("endTime", Long.MIN_VALUE);
                time.put("closeTime", Long.MIN_VALUE);
            }
            entityStartTimeMap.put(entityStartTime.source.id, time);
        }
        logger.debug("returning attempts entity map : " + entityStartTimeMap);
        return entityStartTimeMap;
    }

    private OrgMemberBasicInfo toBasicInfoforOrgMember(OrgMember orgMember) {
        OrgMemberBasicInfo basicInfo = new OrgMemberBasicInfo(orgMember._getStringId(), orgMember.userId, orgMember.orgId,
                orgMember.memberId, orgMember.firstName, orgMember.lastName, orgMember.profile, _getThumbnailUrl(orgMember), orgMember.contactNumber,
                orgMember.recordState, orgMember.canImpersonate);
        OrgMemberMappingExtendedInfo tMappings = new OrgMemberMappingExtendedInfo();
        basicInfo.setMappings(tMappings);

        return basicInfo;
    }

    public String _getThumbnailUrl(OrgMember orgMember) {

        if (!StringUtils.isEmpty(orgMember.thumbnail)) {
            return getEntityImageURL(EntityType.USER, orgMember.thumbnail, ImageSize.SMALL);
        }

        List<String> suffixComponents = new ArrayList<String>();
        suffixComponents.add(orgMember.profile.name());

        if (OrgMemberProfile.STUDENT == orgMember.profile || OrgMemberProfile.TEACHER == orgMember.profile) {
            Gender tGender = (null != orgMember.gender && Gender.UNKNOWN != orgMember.gender) ? orgMember.gender : Gender.UNKNOWN;
            suffixComponents.add(tGender.name());
        }

        return ImageDisplayURLUtil.getEntityStaticThumbnail(EntityType.USER, suffixComponents);
    }

    public static void addScopeFilter(String userId, Query query, Criteria criteria) {
        criteria.orOperator(new Criteria().and(ConstantsGlobal.SCPOE).all(Scope.ORG.name()).and(ConstantsGlobal.USER_ID).is(userId));

    }

    public ModelBasicInfo toProgramBasicInfo(OrgProgram orgProgram) {
        Optional<OrgDepartment> department = orgDepartmentRepo.findById(orgProgram.getDepartmentId().trim());
        if (!department.isPresent())
            throw new VedantuException(VedantuErrorCode.INVALID_ACCESS_CODE, "Department is not found");

        return new OrgProgramBasicInfo(orgProgram._getStringId(), orgProgram.getRecordState(), orgProgram.getcName(), orgProgram.getCode(),
                orgProgram._getEntityType(), orgProgram.getDepartmentId(), department.get().getName(), department.get().getCode(), orgProgram.getCourseIds(), orgProgram.isOffline);
    }

    public String getEntityImageURL(EntityType entityType, String uid, ImageSize size) {
        IEntityFileStorage fileEntityStorage = null;
        if (entityType == EntityType.USER) {
            userStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = userStorage;
        } else if (entityType == EntityType.VIDEO) {
            videoEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = videoEntityFileStorage;
        }

        return ImageDisplayURLUtil.DEFAULT_FILE_SERVING_HOST_URL + fileEntityStorage.computeDisplayUrlComponent(uid, FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                FileCategory.CONVERTED, size);
    }

    public void getBoardsTagFacets(int size, Query query, Criteria criteria) {

        getBoardsTagFacets(null, null, null, size, query, criteria);
    }

    private static String getEncryptedEntityUrl(String urlComponent, Map<String, String> sessionParamsMap) {

        if (sessionParamsMap == null) {
            logger.error("null sessionParamsMap from webRequest");
            return HardCodedConstants.emptyString;
        }
        String userId = sessionParamsMap.get("userId");
        String sessionId = sessionParamsMap.get("___ID");

        if (sessionId.isEmpty() || userId.isEmpty()) {
            return HardCodedConstants.emptyString;
        }
        String passphrase = sessionId + userId;
        FileMaskProcessor processor = new FileMaskProcessor(passphrase, passphrase.getBytes().length);
        byte[] actual = urlComponent.getBytes();
        byte[] result = new byte[actual.length];
        processor.process(actual, 0, actual.length, result);


        String encryptedUrl = Base64.getEncoder().encodeToString(result);

        return encryptedUrl;
    }



    public void getBoardsTagFacets(Collection<String> excludeBrdIds,
                                   Collection<String> excludeTargetIds, Collection<String> excludeTags, int size, Query query, Criteria criteria) {

        if (!CollectionUtils.isEmpty(excludeBrdIds)) {
            criteria.and("boardIds").nin(excludeBrdIds.toArray());
        }

        if (!CollectionUtils.isEmpty(excludeTargetIds)) {
            criteria.and("target").nin(excludeTargetIds.toArray());
        }


        if (!CollectionUtils.isEmpty(excludeTags)) {
            //  VedantuStringUtils.toLowerCase(excludeTags);
            criteria.and(ConstantsGlobal.TAGS).nin(excludeTags.toArray());

        }

    }

    protected <T extends IListResponseObj> SearchListResponse<T> getEntityContentInfos(
            String orderBy, String sortOrder, int start, int size, Class<T> respObj,
            Query esQuery, Set<String> returnedEntityIds,
            String indexName, String indexType) {

       /* SearchResponse response = ElasticSearchUtils.getSearchResponse(esQuery, orderBy, sortOrder,
                start, size, indexName, indexType, null, facets);

        if (response == null || response.getHits().getTotalHits() == 0) {
            logger.error("empty search response for query : " + esQuery);
            return new SearchListResponse<T>();
        }

        SearchListResponse<T> listResponse = new SearchListResponse<T>();

        SearchHits allHits = response.getHits();
        listResponse.totalHits = allHits.getTotalHits();
        logger.debug("totalHits: " + listResponse.totalHits);
        for (SearchHit hits : allHits.getHits()) {
            logger.trace("hits : " + hits.sourceAsString());
            T model = ObjectMapperUtils.convertValue(hits.sourceAsMap(), respObj);
            if (returnedEntityIds != null && hits.sourceAsMap().get(ConstantsGlobal.ID) != null) {
                returnedEntityIds.add(hits.sourceAsMap().get(ConstantsGlobal.ID).toString());
            }
            listResponse.list.add(model);
        }
        if (facets != null && facets.length > 0) {
            ElasticSearchUtils.addCommonFacetDetails(listResponse.facet, response);
        }
        return listResponse;*/
        return null;
    }


    protected void buildSearchQuery(AbstractContentSearchReq searchReq,
                                    EntityType entityType, Query query, Criteria criteria) {

        AtomicLong totalProgramHits = new AtomicLong(0L);
        List<GranteeOrgProgram> granteeOrgPrograms = new ArrayList<>();//GranteeOrgProgramDAO.INSTANCE
        // .getGranteeOrgPrograms(searchReq.orgId, null, totalProgramHits);
        List<String> grantedOrgs = new ArrayList<String>();
        for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            grantedOrgs.add(granteeOrgProgram.providerOrgId);
        }
        buildSearchQuery(searchReq.resultType,
                searchReq._getResultForUserId(), searchReq.contentSrc, searchReq.includeTypes,
                searchReq.excludeTypes, searchReq.excludeIds, searchReq.query, entityType,
                grantedOrgs, query, criteria);
        try {
            addOrgStructureFilter(searchReq, query, criteria);
        } catch (VedantuException e) {
            logger.error(e.getMessage(), e);
        }

    }

    protected void buildSearchFilter(AbstractContentSearchReq searchReq,
                                     EntityType entityType, Query query, Criteria criteria) {

        buildSearchFilter(searchReq, entityType, false, query, criteria);
    }


    public void buildSearchQuery(SearchResultType resultType, String userId,
                                 SrcEntity contentSrc, Collection<String> includeTypes, Collection<String> excludeTypes,
                                 List<String> excludeIds, String query, EntityType entityType, List<String> grantorOrgIds, Query query2, Criteria criteria) {

        logger.debug(" Getting " + entityType + " for user  :" + userId + " with query " + query);


        if (contentSrc != null) {
            if (contentSrc.type.name() != EntityType.ORGANIZATION.toString()) {
                criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID).is(contentSrc.id);

            } else if (entityType == EntityType.DISCUSSION) {
                Organization org = null; //OrganizationDAO.INSTANCE.getById(contentSrc.id);
                ArrayList<String> allOrgs = new ArrayList<String>();
                allOrgs.add(contentSrc.id);
                switch (org.doubtsForumMode) {
                    case PRIVATE:
                        break;
                    case PUBLIC:
                        // allOrgs.add(Play.application().configuration().getString("learnpedia.id"));
                        break;
                    default:
                        break;
                }
                criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID).in(allOrgs.toArray());

            } else {
                ArrayList<String> allOrgs = new ArrayList<String>();
                allOrgs.add(contentSrc.id);
                if (grantorOrgIds != null && grantorOrgIds.size() > 0) {
                    allOrgs.addAll(grantorOrgIds);
                }
                criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                        + ConstantsGlobal.ID).in(allOrgs.toArray());

            }
            criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.TYPE).is(contentSrc.type.name());



        }

        // resultType.addSearchQueryFlter(boolQuery, userId);

        if (!CollectionUtils.isEmpty(excludeIds)) {
            criteria.and(ConstantsGlobal.ID).nin(excludeIds.toArray());
        }
        if (!CollectionUtils.isEmpty(includeTypes)) {
            criteria.and(ConstantsGlobal.TYPE).in(includeTypes.toArray());

        }
        if (!CollectionUtils.isEmpty(excludeTypes)) {
            criteria.and(ConstantsGlobal.TYPE).nin(VedantuStringUtils
                    .toLowerCase(excludeTypes).toArray());

        }


    }

    public void addBoardAndTargetFilter(Collection<String> brdIds, boolean allBrds, Query query, Criteria criteria
    ) {
        boolean blFilter = !CollectionUtils.isEmpty(brdIds);
        if (blFilter) {
            if (allBrds) {
                for (String brdId : brdIds) {
                    criteria.orOperator(new Criteria().and(ConstantsGlobal.BOARDS_ID).is(brdId).and(ConstantsGlobal.TARGETS_ID).is(brdId));

                }
            } else {
                criteria.orOperator(new Criteria().and(ConstantsGlobal.BOARDS_ID).in(brdIds).and(ConstantsGlobal.TARGETS_ID).in(brdIds));
            }
        }
    }

    protected void buildSearchFilter(AbstractContentSearchReq searchReq,
                                     EntityType entityType, boolean noScopeFilter, Query query, Criteria criteria) {

        String userId = searchReq._getResultForUserId();

        addBoardAndTargetFilter(searchReq.brdIds, searchReq.allBrds, query, criteria);
        if (!noScopeFilter) {
            addScopeFilter(userId, query, criteria);
        } else {

            // criteria.and(ConstantsGlobal.SCPOE).all(Scope.PUBLIC.name().toLowerCase(), Scope.ORG.name().toLowerCase());
        }
    }

    private void addOrgStructureFilter(AbstractContentSearchReq searchReq, Query query, Criteria criteria
    ) throws VedantuException {

        // checking if current OrgId and  programId are not empty
        if (!StringUtils.isEmpty(searchReq.orgId) && !StringUtils.isEmpty(searchReq.programId)) {
            //Marking current orgId to programOrgId
            String programOrgId = searchReq.orgId;
            AtomicLong totalProgramHits = new AtomicLong(0L);
            logger.debug("Current orgId" + searchReq);
            // getting all the rows of the current orgId as OrgId as key
            List<GranteeOrgProgram> granteeOrgPrograms = null; //GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(searchReq.orgId, null, totalProgramHits);
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
                // if programId from search request matches the programId from the list above assign the granteeId as the programId
                if (granteeOrgProgram.programId.equalsIgnoreCase(searchReq.programId)) {
                    programOrgId = granteeOrgProgram.providerOrgId;
                }
            }

          /*  Collection<String> sectionIds = !StringUtils.isEmpty(searchReq.sectionId) ? Arrays
                    .asList(searchReq.sectionId) : OrgProgramManager.getProgramSections(
                    programOrgId,
                    searchReq.programId,
                    StringUtils.isEmpty(searchReq.centerId) ? null : Arrays
                            .asList(searchReq.centerId));
            String childType = UserActionType.ADDED.getSearchIndexType();
           boolQuery.must(QueryBuilders.hasChildQuery(childType,
                    QueryBuilders.inQuery(childType + ".dst.id", sectionIds.toArray())));*/
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends IListResponseObj> SearchListResponse<T> getEntityInfos(
            AbstractContentSearchReq searchReq, EntityType entityType, Class<T> respObj,
            Set<String> returnedEntityIds) {

        Query query1 = new Query();
        Criteria criteria = new Criteria();
        getBoardsTagFacets(searchReq.size, query1, criteria);
        buildSearchQuery(searchReq, entityType, query1, criteria);
        buildSearchFilter(searchReq, entityType, query1, criteria);
        if (!CollectionUtils.isEmpty(searchReq.includeModes)) {
            criteria.and(ConstantsGlobal.MODE).in(VedantuStringUtils
                    .toLowerCase(searchReq.includeModes).toArray());

        }
       /* if (!CollectionUtils.isEmpty(searchReq.includeTypes)) {
            criteria.and(ConstantsGlobal.TYPE).in(VedantuStringUtils
                    .toLowerCase(searchReq.includeTypes).toArray());

        }*/
        if (!CollectionUtils.isEmpty(searchReq.includeDifficulty)) {
            criteria.and(ConstantsGlobal.DIFFICULTY).in(VedantuStringUtils
                    .toLowerCase(searchReq.includeDifficulty).toArray());

        }
        if (!CollectionUtils.isEmpty(searchReq.scope)) {
            criteria.and(ConstantsGlobal.SCPOE).in(searchReq.scope.toArray());

        }
        query1.addCriteria(criteria);
       /* return getEntityInfos(searchReq.orderBy, searchReq.sortOrder, searchReq.start,
                searchReq.size, entityType, respObj, boolQuery, boolFilter, facets,
                returnedEntityIds);*/
        SearchListResponse<T> listResponse = new SearchListResponse<T>();
        if (entityType == EntityType.QUESTION) {
            List<Question> questions = mongoTemplate.find(query1, Question.class);
            listResponse.totalHits = questions.size();
            logger.debug("totalHits: " + listResponse.totalHits);
            List<GetQuestionRes> resList = questions.stream().map(temp -> {
                GetQuestionRes res = new GetQuestionRes();
                res.setScope(temp.getScope());
                res.setContent(temp.getContent());
                res.setAttempts(temp.getAttempts());
                res.setId(temp._getStringId());
                res.setCode(temp.getCode());
                res.setDifficulty(temp.getDifficulty());
                res.setLatexType(temp.getLatexType());
                res.setRecordState(temp.getRecordState());
                res.setLastUpdated(temp.getLastUpdated());
                res.setSolutions(temp.getSolutions());
                res.setContentSrc(temp.getContentSrc());
                res.setTags(temp.getTags());
                res.setComments(temp.getComments());
                res.setOptions(temp.getOptions());
                return res;
            }).collect(Collectors.toList());
            listResponse.totalHits = questions.size();
            listResponse.list.addAll((Collection<? extends T>) resList);

        } else if (entityType == EntityType.ASSIGNMENT) {
            List<Assignment> assignments = mongoTemplate.find(query1, Assignment.class);
            List<GetAssignmentInfoRes> assignmentInfoResList = assignments.stream().map(assignment -> {
                GetAssignmentInfoRes assignmentInfoRes = new GetAssignmentInfoRes();
                assignmentInfoRes.setId(assignment._getStringId());
                assignmentInfoRes.setAttempts(assignment.getAttempts());
                assignmentInfoRes.setDesc(assignment.getDesc());
                assignmentInfoRes.setCode(assignment.getCode());
                assignmentInfoRes.setComments(assignment.getComments());
                assignmentInfoRes.setRecordState(assignment.getRecordState());
                return assignmentInfoRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = assignments.size();
            listResponse.list.addAll((Collection<? extends T>) assignmentInfoResList);
        } else if (entityType == EntityType.VIDEO) {
            List<Video> videos = mongoTemplate.find(query1, Video.class);
            List<GetVideoRes> getVideoResList = videos.stream().map(video -> {
                GetVideoRes getVideoRes = new GetVideoRes();
                getVideoRes.setId(video._getStringId());
                getVideoRes.setComments(video.getComments());
                getVideoRes.setAvgRating(video.getAverage());
                getVideoRes.setDifficulty(video.getDifficulty());
                getVideoRes.setDescription(video.getDescription());
                getVideoRes.setOriginalFileName(video.getOriginalFileName());
                getVideoRes.setUserId(video._getUserId());
                getVideoRes.setTimeCreated(video.getTimeCreated());
                getVideoRes.setThumbnail(video.getThumbnail());
                getVideoRes.setLinkType(video.getLinkType());
                getVideoRes.setExtension(video.getExtension());
                return getVideoRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = videos.size();
            listResponse.list.addAll((Collection<? extends T>) getVideoResList);
        } else if (entityType == EntityType.DOCUMENT) {
            List<Documents> documents = mongoTemplate.find(query1, Documents.class);
            List<GetDocumentRes> getDocumentResList = documents.stream().map(video -> {
                GetDocumentRes getDocumentRes = new GetDocumentRes();
                return getDocumentRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = documents.size();
            listResponse.list.addAll((Collection<? extends T>) getDocumentResList);
        } else if (entityType == EntityType.FILE) {
            List<Files> files = mongoTemplate.find(query1, Files.class);
            List<GetFileRes> getFileResList = files.stream().map(file -> {
                GetFileRes getFileRes = new GetFileRes();
                getFileRes.setAvgRating(file.average);
                getFileRes.setCmdsFileId(file.getCMDSFileId());
                getFileRes.setDifficulty(file.getDifficulty());
                getFileRes.setDescription(file.getDescription());
                getFileRes.setId(file._getStringId());
                return getFileRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = files.size();
            listResponse.list.addAll((Collection<? extends T>) getFileResList);
        } else if (entityType == EntityType.DISCUSSION) {
            List<Discussion> discussions = mongoTemplate.find(query1, Discussion.class);
            List<GetDocumentRes> getDocumentResList = discussions.stream().map(video -> {
                GetDocumentRes getDocumentRes = new GetDocumentRes();
                return getDocumentRes;
            }).collect(Collectors.toList());
            listResponse.totalHits = discussions.size();
            listResponse.list.addAll((Collection<? extends T>) getDocumentResList);
        }

        return listResponse;

    }

    protected void annotateLinkInfo(AbstractFileModelIndexSearchDetails model) {

        if (model.linkInfo != null) {
            model.linkInfo.populate();
        }
    }

    public String addImageSrcUrl(EntityType entityType, String html) {

        if (StringUtils.isEmpty(html)) {
            return "";
        }
        Document doc = Jsoup.parseBodyFragment(html);
        Elements elements = doc.getElementsByAttribute(IMG_IDENTIFIER);
        Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            Element es = it.next();
            String url = getImgUrl(entityType, es.attr(IMG_IDENTIFIER));
            if (!StringUtils.isEmpty(url)) {
                es.attr(IMG_SRC, url);
                es.attr("class", IMG_CLASS_NAME);
                es.attr(IMG_SRC_PERMANENT, Boolean.toString(true));
            }
            logger.info("html element url : " + url + " and element: " + es);
        }
        String htmlBody = doc.body().html();
        return htmlBody;
    }

    private String getImgUrl(EntityType entityType, String dataUid) {

        logger.info(" dataUid: " + dataUid);
        String url = getEntityImageURL(entityType, dataUid);
        return url;
    }

    public String getEntityImageURL(EntityType entityType, String uid) {

        return getEntityImageURLUtil(entityType, uid, ImageSize.ORIGINAL);
    }

    public String getEntityImageURLUtil(EntityType entityType, String uid, ImageSize size) {
        IEntityFileStorage fileEntityStorage = null;
        //IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        if (entityType == EntityType.SOLUTION) {
            solutionEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = solutionEntityFileStorage;
        } else if (entityType == EntityType.QUESTION) {
            questionEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = questionEntityFileStorage;
        }else if(entityType == EntityType.USER) {
        	userStorage.AbstractEntityFileStorageEntity(entityType);
        	fileEntityStorage = userStorage;
        }else if (entityType == EntityType.STATUSFEED) {
            statusFeedEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = statusFeedEntityFileStorage;
        } else if (entityType == EntityType.MESSAGE) {
            messageEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = messageEntityFileStorage;
        }
        if (directFileServingEnabled) {
            return fileEntityStorage.getSecuredURL(uid, entityType,
                    FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                    FileCategory.CONVERTED, size).getSecuredURL();
        }


        return ImageDisplayURLUtil.DEFAULT_FILE_SERVING_HOST_URL + fileEntityStorage.computeDisplayUrlComponent(uid, FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                FileCategory.CONVERTED, size);
    }

    public String getEntityVideoURL(EntityType entityType, String uid, String fileExt,
                                    FileCategory fileCategory) {
        IEntityFileStorage fileEntityStorage = null;
        //IEntityFileStorage fileEntityStorage = EntityStorageFactory.INSTANCE.get(entityType);
        if (entityType == EntityType.VIDEO) {
            videoEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = videoEntityFileStorage;
        }
        return ImageDisplayURLUtil.DEFAULT_FILE_STREAMING_HOST_URL
                + fileEntityStorage.computeDisplayUrlComponent(uid, fileExt, MediaType.VIDEO, fileCategory, null);
    }

    public String getEntityThumbnail(EntityType entityType, String uid) {

        return getEntityImageURL(entityType, uid, ImageSize.SMALL);
    }

    @SuppressWarnings("unchecked")
    protected <T extends IListResponseObj> SearchListResponse<T> getSimilarEntityInfos(
            GetSimilarEntities similarEntityReq, Class<T> respObj, Set<String> returnedEntityIds) {

        // VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(similarEntityReq.entity.type);
        AbstractBoardEntityTagModel model = null;
        if (similarEntityReq.entity.type == EntityType.QUESTION) {
            Optional<Question> questionOptional = questionRepo.findById(similarEntityReq.entity.id);
            if (questionOptional.isPresent()) {
                model = questionOptional.get();
            }
        } else if (similarEntityReq.entity.type == EntityType.VIDEO) {
            Optional<Video> videoOptional = videoRepo.findById(similarEntityReq.entity.id);
            if (videoOptional.isPresent()) {
                model = videoOptional.get();
            }
        } else if (similarEntityReq.entity.type == EntityType.FILE) {
            Optional<Files> fileOptional = filesRepo.findById(similarEntityReq.entity.id);
            if (fileOptional.isPresent()) {
                model = fileOptional.get();
            }
        } else if (similarEntityReq.entity.type == EntityType.DOCUMENT) {
            Optional<Documents> document = documentsRepo.findById(similarEntityReq.entity.id);
            if (document.isPresent()) {
                model = document.get();
            }
        } else if (similarEntityReq.entity.type == EntityType.DISCUSSION) {
            Optional<Discussion> discussion = discussionRepo.findById(similarEntityReq.entity.id);
            if (discussion.isPresent()) {
                model = discussion.get();
            }
        }
        if (model == null) {
            return new SearchListResponse<T>();
        }
        Set<String> brdIds = model.__getAllBoardIds();
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ConstantsGlobal.ID).ne(similarEntityReq.entity.id);
        //.mustNot(QueryBuilders.fieldQuery(ConstantsGlobal.ID, similarEntityReq.entity.id));

        if (!brdIds.isEmpty()) {
            criteria.and("boardIds").in(brdIds.toArray());
            //boolQuery.should(QueryBuilders.inQuery(ConstantsGlobal.BOARDS_ID, brdIds.toArray()));
            criteria.and("targetIds").in(brdIds.toArray());
            //boolQuery.should(QueryBuilders.inQuery(ConstantsGlobal.TARGETS_ID, brdIds.toArray()));
        }
        if (!CollectionUtils.isEmpty(model.tags)) {
            criteria.and("ConstantsGlobal.TAGS").in(model.tags.toArray());
            //boolQuery.should(QueryBuilders.inQuery(ConstantsGlobal.TAGS, VedantuStringUtils.toLowerCase(model.tags).toArray()));
        }
        if (model.contentSrc != null) {
            criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.ID).in(model.contentSrc.id);
            criteria.and(ConstantsGlobal.CONTENT_SRC + "."
                    + ConstantsGlobal.TYPE).in(model.contentSrc.type.name());

        }
        addScopeFilter(similarEntityReq.userId, query, criteria);
        query.addCriteria(criteria);

        SearchListResponse<T> listResponse = new SearchListResponse<T>();
        if (similarEntityReq.entity.type == EntityType.QUESTION) {
            List<Question> questions = mongoTemplate.find(query, Question.class);
            List<GetQuestionRes> resList = questions.stream().map(temp -> {
                GetQuestionRes res = new GetQuestionRes();
                res.setScope(temp.getScope());
                res.setContent(temp.getContent());
                res.setAttempts(temp.getAttempts());
                return res;
            }).collect(Collectors.toList());
            listResponse.list.addAll((Collection<? extends T>) resList);
        } else if (similarEntityReq.entity.type == EntityType.VIDEO) {
            List<Video> videos = mongoTemplate.find(query, Video.class);
            List<GetVideoRes> getVideoResList = videos.stream().map(video -> {
                GetVideoRes getVideoRes = new GetVideoRes();
                return getVideoRes;
            }).collect(Collectors.toList());
            listResponse.list.addAll((Collection<? extends T>) getVideoResList);
        } else if (similarEntityReq.entity.type == EntityType.FILE) {
            List<Files> files = mongoTemplate.find(query, Files.class);
            List<GetFileRes> getFileResList = files.stream().map(file -> {
                GetFileRes getFileRes = new GetFileRes();
                return getFileRes;
            }).collect(Collectors.toList());
            listResponse.list.addAll((Collection<? extends T>) getFileResList);
        } else if (similarEntityReq.entity.type == EntityType.DOCUMENT) {
            List<Documents> docs = mongoTemplate.find(query, Documents.class);
            List<GetFileRes> getFileResList = docs.stream().map(file -> {
                GetFileRes getFileRes = new GetFileRes();
                return getFileRes;
            }).collect(Collectors.toList());
            listResponse.list.addAll((Collection<? extends T>) getFileResList);
        } else if (similarEntityReq.entity.type == EntityType.DISCUSSION) {
            List<Discussion> discs = mongoTemplate.find(query, Discussion.class);
            List<GetFileRes> getFileResList = discs.stream().map(file -> {
                GetFileRes getFileRes = new GetFileRes();
                return getFileRes;
            }).collect(Collectors.toList());
            listResponse.list.addAll((Collection<? extends T>) getFileResList);
        }
        return listResponse;

    }

    public String getEntityDownloadURL(EntityType entityType, String uid, String fileExt,
                                       MediaType mediaType, FileCategory fileCategory, String id) {
        IEntityFileStorage fileEntityStorage = null;
        if (entityType == EntityType.FILE) {
            fileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = fileStorage;
        }
        return ImageDisplayURLUtil.DEFAULT_FILE_DOWNLOAD_HOST_URL
                + fileEntityStorage.computeDisplayUrlComponent(uid, fileExt, mediaType, fileCategory, null) + "/"
                + id;
    }

    public String getEntityVideoSecureURL(EntityType entityType, String uid,
                                          Map<String, String> sessionParamsMap, boolean isWebReq) {

        IEntityFileStorage fileEntityStorage = null;
        if (entityType == EntityType.VIDEO) {
            videoEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = videoEntityFileStorage;
        }

        String componentUrl = fileEntityStorage.computeDisplayUrlComponent(uid, FileUtils.WEBM_EXTENTION_WITHOUT_DOT,
                MediaType.VIDEO, FileCategory.CONVERTED, null);
        return isWebReq && videoFileSecurityEnabled
                ? (ImageDisplayURLUtil.DEFAULT_FILE_ESTREAMING_HOST_URL + getEncryptedEntityUrl(componentUrl, sessionParamsMap))
                : (ImageDisplayURLUtil.DEFAULT_FILE_STREAMING_HOST_URL + componentUrl);
    }

    public String getEntityVideoSecureURL(EntityType entityType, String uid, String fileExt,
                                          FileCategory fileCategory, Map<String, String> sessionParamsMap, boolean isWebReq) {

        IEntityFileStorage fileEntityStorage = null;
        if (entityType == EntityType.VIDEO) {
            videoEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = videoEntityFileStorage;
        }
        String componentUrl = fileEntityStorage.computeDisplayUrlComponent(uid, fileExt, MediaType.VIDEO,
                fileCategory, null);

        return isWebReq && videoFileSecurityEnabled
                ? (ImageDisplayURLUtil.DEFAULT_FILE_ESTREAMING_HOST_URL + getEncryptedEntityUrl(componentUrl, sessionParamsMap))
                : (ImageDisplayURLUtil.DEFAULT_FILE_STREAMING_HOST_URL + componentUrl);
    }

    public String getEntityVideoS3URL(EntityType entityType, String uid, String fileExt,
                                      FileCategory fileCategory) {

        IEntityFileStorage fileEntityStorage = null;
        if (entityType == EntityType.VIDEO) {
            videoEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
            fileEntityStorage = videoEntityFileStorage;
        }
        String componentUrl = fileEntityStorage.computeDisplayS3UrlComponent(uid, fileExt, MediaType.VIDEO, fileCategory);

        return ("https://" + fileEntityStorage.getStorageId() + ".s3.amazonaws.com/" + componentUrl);
    }

    public String getEntityPoster(EntityType entityType, String uid) {

        return getEntityImageURL(entityType, uid, ImageSize.MEDIUM);
    }

    public UserInfo getUserInfo(String orgId, String userId) {

        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(orgId, Arrays.asList(userId));

        return (UserInfo) userInfos.get(userId);
    }

    public UserInfo getUserInfo(String orgId, String userId, boolean excludeOrgMappingInfo) {

        Map<String, ModelBasicInfo> userInfos = getUserInfoMap(orgId, Arrays.asList(userId),
                excludeOrgMappingInfo);

        return (UserInfo) userInfos.get(userId);
    }

    public Map<String, ModelBasicInfo> getUserInfoMap(String orgId,
                                                      Collection<String> userIds) {

        return getUserInfoMap(orgId, userIds, false);
    }

    protected void updateParentFollowersCount(String userId, SrcEntity parent, int inc)
            throws VedantuException {
        VedantuBaseMongoModel model = null;
        if (parent.type == EntityType.MODULE) {
            Optional<Module> moduleOptional = moduleRepo.findById(parent.id);
            if (moduleOptional.isPresent()) {
                Module module = moduleOptional.get();
                module.setFollowers(module.getFollowers() + inc);
                moduleRepo.save(module);
                model = module;
            }
        }
        logger.debug("updating " + parent + ", follower count");
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        logger.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (model != null && eventType != null && inc < 0) {
            // inc > 0 FOLLOW && inc < 0 UNFOLLOW in case of UNFOLLOW we need not send news feed so
            // just re-index the entity, in case of follow EntityUserActionUtils.updateEntityCount
            // method will be used
            generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
                    UserActionType.FOLLOWING, false);
        }
    }

    protected void updateViewsCount(String userId, SrcEntity parent, int inc)
            throws VedantuException {
        VedantuBaseMongoModel model = null;
        if (parent.type == EntityType.MODULE) {
            Optional<Module> moduleOptional = moduleRepo.findById(parent.id);
            if (moduleOptional.isPresent()) {
                Module module = moduleOptional.get();
                module.setViews(module.getViews() + inc);
                moduleRepo.save(module);
                model = module;
            }
        }
        logger.debug("updating " + parent + ", view count");
        EventType eventType = EntityIndexEventMapper.INSTANCE.get(parent.type);
        logger.debug("eventType: " + eventType + ", for parentType : " + parent.type);
        if (eventType != null) {
            generateEventAysc(userId, model, EventActionType.UPDATE, eventType,
                    UserActionType.ATTEMPTED, false);
        }
    }

    protected void updateParentUpVotesCount(String userId, SrcEntity parent)
            throws VedantuException {

        logger.info("updating " + parent + ", vote count");
        VedantuBaseMongoModel model = null;
        if (parent.type == EntityType.MODULE) {
            Optional<Module> moduleOptional = moduleRepo.findById(parent.id);
            if (moduleOptional.isPresent()) {
                Module module = moduleOptional.get();
                module.setUpVotes(module.getUpVotes() + 1);
                moduleRepo.save(module);
                model = module;
            }
        }
        logger.debug("updated model : " + model);


    }


    @Override
    public boolean update(EditContentReq request) throws VedantuException {

        // This is a dummy implementation
        return false;
    }

    @Override
    public boolean calculate(String id, boolean recalculate, VedantuBaseMongoModel... contents) throws VedantuException {
        return false;
    }


    @Override
    public List<DownloadableFileInfo> getFiles(EntityType entityType, String entityId) throws VedantuException {

        // TODO Auto-generated method stub
        return null;
    }


}
