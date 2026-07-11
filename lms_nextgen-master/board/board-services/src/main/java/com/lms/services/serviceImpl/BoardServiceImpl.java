package com.lms.services.serviceImpl;

import com.lms.board.components.BoardManager;
import com.lms.board.enums.BoardContextType;
import com.lms.board.events.details.BoardRemovalDetails;
import com.lms.board.model.Board;
import com.lms.board.pojos.requests.*;
import com.lms.board.pojos.responces.GetChildrenRes;
import com.lms.board.pojos.responces.GetTreesRes;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.pojos.test.BoardTree;
import com.lms.board.pojos.test.requests.GetTargetsReq;
import com.lms.board.pojos.test.responses.GetTargetsRes;
import com.lms.board.repo.BoardRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.EventUtil;
import com.lms.common.utils.FileUtils;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.entity.storage.UserProfilePicEntityFileStorage;
import com.lms.common.vedantu.enums.BoardType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.event.api.IEventDetails;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.services.BoardService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class BoardServiceImpl implements BoardService {
    private static final Logger logger = LoggerFactory.getLogger(BoardServiceImpl.class);
    @Autowired
    private BoardManager boardManager;
    @Autowired
    private BoardRepo boardRepo;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserProfilePicEntityFileStorage picStorage;
    @Autowired
    private EventUtil eventUtil;

    protected static void deleteFile(String fileName, File file) {

        FileUtils.deleteFile(fileName, file);
    }

    @Override
    public VedantuResponse uploadConsumerBoards(MultipartFile file, UploadConsumerBoardReq uploadBoardReq) {
        if (file == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_FILE);
        }
        VedantuResponse response = null;
        try {
            uploadBoardReq.inputFile = picStorage.convertMultiPartToFile(file);
            response = boardManager.uploadBoards("SYSTEM", uploadBoardReq, BoardContextType.CONSUMER,
                    uploadBoardReq.treeName, uploadBoardReq.grades);

            deleteFile(uploadBoardReq.fileName, uploadBoardReq.inputFile);
        } catch (VedantuException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public VedantuResponse uploadOrgBoards(MultipartFile file, UploadOrgBoardReq uploadBoardReq) {
        if (file == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_FILE);
        }
        VedantuResponse response = null;
        try {
            uploadBoardReq.inputFile = picStorage.convertMultiPartToFile(file);
            response = boardManager.uploadBoards(uploadBoardReq.orgId, uploadBoardReq, BoardContextType.ORG,
                    uploadBoardReq.treeName, uploadBoardReq.grades);

            deleteFile(uploadBoardReq.fileName, uploadBoardReq.inputFile);
        } catch (VedantuException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public VedantuResponse uploadglobalBoards(MultipartFile file, UploadGlobalBoardReq uploadBoardReq) {
        //UploadGlobalBoardReq uploadBoardReq = new UploadGlobalBoardReq(file);
        try {
            uploadBoardReq.inputFile = picStorage.convertMultiPartToFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        VedantuResponse result = validateUploadBoardReq(uploadBoardReq);
        if (null == result) {
            result = boardManager.uploadBoards("SYSTEM", uploadBoardReq, BoardContextType.GLOBAL,
                    "SYSTEM", null);
        }
        deleteFile(uploadBoardReq.fileName, uploadBoardReq.inputFile);
        return result;
    }

    @Override
    public VedantuResponse delete(DeleteBoardReq deleteBoardReq)
    {
        Board nodeToBeDeleted = boardRepo.findById(deleteBoardReq.getBrdId()).get();
        List<String> deltedBrdIds = new ArrayList<String>();
        boolean deleteSucceeded = mark(deleteBoardReq.getBrdId(), deltedBrdIds,
                VedantuRecordState.DELETED, nodeToBeDeleted.recordState);

        if (deleteSucceeded) {
            BoardRemovalDetails details;
            try {

                details = new BoardRemovalDetails();
                details.brdIds.addAll(deltedBrdIds);
                details.userId = deleteBoardReq.getUserId();
                // TODO have better logic to decide which BoardType needs state change
                details.changeState = nodeToBeDeleted.type != BoardType.SUBSUBTOPIC && nodeToBeDeleted.type != BoardType.SUBTOPIC;
                generateEventAysc(deleteBoardReq.getUserId(), details, EventType.REMOVE_BOARD);
            } catch (ClassNotFoundException e) {

                throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED);
            }
            return new VedantuResponse(true);
        }

        return new VedantuResponse(false);

    }

    public void generateEventAysc(final String userId, final IEventDetails details,
                                  final EventType eventType) {

        generateEventAysc(userId, details, eventType, 0);
    }

    public void generateEventAysc(final String userId, final IEventDetails details,
                                  final EventType eventType, final long processTime) {
        CompletableFuture.runAsync(() -> {
            eventUtil.generateEvent(eventType, null, userId, details,
                    details.__getSrcEntity(), UserActionType.EventActionType.ADD, processTime);
        });

    }

    @Override
    public VedantuResponse getTreesOfBoards(GetTreesOfBoardsReq getTreesOfBoardsReq) {
        if (null == getTreesOfBoardsReq) {
            String errorMsg = "null getTreesOfBoardsReq";
            logger.error("getTreesOfBoards " + errorMsg);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, errorMsg);
        }

        List<Board> boards = collectHierarchy(getTreesOfBoardsReq.context,
                getTreesOfBoardsReq.ownerId, new HashSet<String>(getTreesOfBoardsReq.treeRootIds),
                getTreesOfBoardsReq.depth);

        logger.trace("getTreesOfBoards boards.size: " + CollectionUtils.size(boards));
        List<BoardBasicInfo> boardBasicInfos = boardManager.toBasicInfos(boards);
        logger.debug("getTreesOfBoards boardBasicInfos.size: "
                + CollectionUtils.size(boardBasicInfos));

        return new VedantuResponse(getTreesByInfos(boardBasicInfos));
    }
    public List<Board> collectHierarchy(BoardContextType context, String ownerId,
                                        Set<String> boardIds, int depth) {

        logger.debug("collectHierarchy context: " + context + ", ownerId: " + ownerId + ", depth: "
                + depth + ", boardIds: {" + boardIds+ ", " + "}");

        if (CollectionUtils.isEmpty(boardIds) || depth < 0) {
            logger.debug("collectHierarchy empty boardIds or depth < 0");
            return new ArrayList<Board>();
        }

        logger.debug("fetching parent boards first");
        Criteria criteria=new Criteria();
        Query query=new Query();
        criteria.and("_id").in(ObjectIdUtils.toObjectIds(new ArrayList<String>(boardIds)));
        criteria.and("ownerId").is(ownerId);
        criteria.and("context").is(context);
        query.addCriteria(criteria);
        logger.debug("fetching parent boards query: " + query);
        List<Board> boards = mongoTemplate.find(query,Board.class);
        depth--;
        logger.debug("parent boards size: " + CollectionUtils.size(boards) + ", depth: " + depth);

        if (CollectionUtils.isNotEmpty(boards)) {
            List<Board> parentBoards = boards;
            while (depth-- > 0 && CollectionUtils.isNotEmpty(parentBoards)) {
                logger.debug("processing at depth: " + depth);
                Set<String> parentBoardIds = new HashSet<String>();
                for (Board parentBoard : parentBoards) {
                    if (null == parentBoard) {
                        continue;
                    }
                    parentBoardIds.add(parentBoard._getStringId());
                }
                logger.debug("parentBoardIds.size: " + CollectionUtils.size(parentBoardIds));
                Criteria criteria1=new Criteria();
                Query query1=new Query();
                criteria1.and("ownerId").is(ownerId);
                criteria1.and("parentBrdIds").in(parentBoardIds);
                criteria1.and("context").is(context);
                query1.addCriteria(criteria1);
                logger.debug("fetching parent boards query: " + query);

                List<Board> childrenBoards = mongoTemplate.find(query1,Board.class);
                logger.debug("childrenBoards.size: " + CollectionUtils.size(childrenBoards));

                if (CollectionUtils.isNotEmpty(childrenBoards)) {
                    boards.addAll(childrenBoards);
                }
                logger.debug("after cumulation boards.size: " + CollectionUtils.size(boards));
                parentBoards = childrenBoards;
            }
        }

        logger.info("collectHierarchy boards.size: " + CollectionUtils.size(boards));

        return boards;

    }
    public  GetTreesRes getTreesByInfos(Collection<BoardBasicInfo> boardBasicInfos) {

        logger.debug("getTreesByInfos boardBasicInfos.size: "
                + CollectionUtils.size(boardBasicInfos));

        if (CollectionUtils.isEmpty(boardBasicInfos)) {
            logger.debug("getTreesByInfos no boardBasicInfos given");
            return new GetTreesRes();
        }

        logger.debug("getTreesByInfos boardBasicInfos.size: "
                + CollectionUtils.size(boardBasicInfos));

        Map<String, BoardBasicInfo> boardBasicInfoMap = boardManager
                .basicInfosToMap(boardBasicInfos);

        logger.debug("getTreesByInfos boardBasicInfoMap.size: "
                + CollectionUtils.size(boardBasicInfoMap));
        // query 2
        // get all content update state completed false, pull all topicboards, get all content from

        // query 3

        // ILE corresponding to this and set to temporary state
        return getTreesByInfosMap(boardBasicInfoMap);
    }

    public static GetTreesRes getTreesByInfosMap(Map<String, BoardBasicInfo> boardBasicInfoMap) {

        logger.trace("getTreesByInfosMap boardBasicInfoMap.size: "
                + CollectionUtils.size(boardBasicInfoMap));

        GetTreesRes getTreesRes = new GetTreesRes();

        if (MapUtils.isEmpty(boardBasicInfoMap)) {
            logger.debug("getTreesByInfosMap no boardBasicInfoMap given");
            return getTreesRes;
        }

        List<BoardTree> roots = toForest(boardBasicInfoMap);
        getTreesRes.list = roots;
        getTreesRes.totalHits = roots.size();

        logger.trace("getTreesByInfosMap roots.size: " + CollectionUtils.size(roots));

        return getTreesRes;
    }
    private static List<BoardTree> toForest(Map<String, BoardBasicInfo> boardBasicInfoMap) {

        logger.trace("toForest boardBasicInfoMap.size: " + CollectionUtils.size(boardBasicInfoMap));

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
        logger.debug("toForest created treeMap.size: " + CollectionUtils.size(treeMap));

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
        logger.trace("toForest selected root entries, roots.size: " + CollectionUtils.size(roots));

        logger.trace("toForest setting levels for nodes");
        for (BoardTree root : roots) {
            root._setLevel(0);
        }
        logger.debug("toForest setting levels for nodes completed");

        return roots;
    }

    public boolean mark(String boardId, List<String> brdIds, VedantuRecordState state, VedantuRecordState existing)
            throws VedantuException {

        if (brdIds == null) {
            return false;
        }

        Update update = new Update();
        update.set(ConstantsGlobal.RECORD_STATE, state);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where("_id").is(new ObjectId(boardId)), Criteria.where("parentBrdIds").in(Arrays.asList(brdIds)));
        criteria.and(ConstantsGlobal.RECORD_STATE).is(existing);
        query.addCriteria(criteria);
        mongoTemplate.updateMulti(query, update, Board.class);

        logger.debug(" FindQuery " + query.toString());

        return true;
    }
    public VedantuResponse getchildren(GetChildrenReq getChildrenReq) {
        GetChildrenRes getChildrenRes = boardManager.getChildren(getChildrenReq);

        return new VedantuResponse(getChildrenRes);
    }

    private VedantuResponse validateUploadBoardReq(UploadGlobalBoardReq uploadBoardReq) {

        VedantuResponse result = null;
        if (null == uploadBoardReq) {
            result = new VedantuResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS));
        } else {
            String validation = uploadBoardReq.validate();
            if (!StringUtils.isEmpty(validation)) {
                result = new VedantuResponse(new VedantuResponse(VedantuErrorCode.MISSING_PARAMETERS));

            } else if (null == uploadBoardReq.inputFile) {
                result = new VedantuResponse(new VedantuException(VedantuErrorCode.MISSING_FILE));
            }
        }
        return result;
    }

    @Override
    public VedantuResponse getTargets(GetTargetsReq getTargetsReq) {
        if (null == getTargetsReq) {
            String errorMsg = "null getTargetsReq";
            logger.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, errorMsg);
        }
        String validation = getTargetsReq.validate();
        if (!StringUtils.isEmpty(validation)) {
            logger.error(validation);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation);
        }
        // UJJ: There is no concept of targets right now.
        GetTargetsRes getTargetsRes = new GetTargetsRes();
        return new VedantuResponse(getTargetsRes);
    }
}
