package com.lms.board.components;

import com.lms.board.enums.BoardContextType;
import com.lms.board.model.Board;
import com.lms.board.model.GranteeOrgProgram;
import com.lms.board.pojos.BoardNode;
import com.lms.board.pojos.parses.BoardXLParser;
import com.lms.board.pojos.requests.GetChildrenReq;
import com.lms.board.pojos.requests.UploadGlobalBoardReq;
import com.lms.board.pojos.responces.GetChildrenRes;
import com.lms.board.pojos.responces.GetTreesRes;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.board.pojos.test.BoardTree;
import com.lms.board.repo.BoardRepo;
import com.lms.board.repo.GranteeOrgProgramRepo;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.utils.VedantuStringUtils;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.BoardType;
import com.lms.common.vedantu.enums.GradeType;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class BoardManager {
    private static final Logger logger = LoggerFactory.getLogger(BoardManager.class);
    public static final String OWNER_SYSTEM = "SYSTEM";
    public static final String TREE_SYSTEM = "SYSTEM";
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private BoardRepo boardRepo;
    @Autowired
    private GranteeOrgProgramRepo granteeOrgProgramRepo;
    public VedantuResponse uploadBoards(String ownerId, UploadGlobalBoardReq uploadBoardReq,
                                        BoardContextType contextType, String treeName, Set<GradeType> grades)
            throws VedantuException {
        BoardXLParser boardXLParser = new BoardXLParser(uploadBoardReq.fileName,
                uploadBoardReq.inputFile, contextType);

        if (boardXLParser.hasErrors()) {
            String errors = boardXLParser.getErrors().stream().collect(Collectors.joining("\n"));
            logger.error("errors: " + errors);
            throw new VedantuException(VedantuErrorCode.BOARD_FILE_UNPARSEABLE, errors);
        } else {
            logger.info("no errors found in file: " + uploadBoardReq.fileName + " stored at: "
                    + uploadBoardReq.inputFile.getAbsolutePath());
        }
        logger.info("parsed : " + boardXLParser);

        BoardContextType parentContextType = BoardContextType.getParentContextType(contextType);
        String parentOwnerId = OWNER_SYSTEM;
        if (null != parentContextType) {
            String parentTreeName = getParentTreeName(parentContextType, treeName);
            Set<String> parentContextBoardCNames = boardXLParser.getParentContextBoardCNames();
            verifyExistenceOfBoards("parentContextBoardCNames", parentContextBoardCNames,
                    parentOwnerId, parentContextType, parentTreeName);
        }

        Set<String> unseenCNames = boardXLParser.getUnseenCNames();
        verifyExistenceOfBoards("unseenCNames", unseenCNames, ownerId, contextType, treeName);

        boolean result = addBoards(boardXLParser.getAllBoardNodes(), ownerId,
                contextType, parentOwnerId, parentContextType, treeName, grades);
        logger.info("uploadBoards result : " + result);

        Set<String> boardIds = collectBoardIds(boardXLParser.getAllBoardNodes());

        GetTreesRes getTreesRes = getTreesByIds(boardIds);

        return new VedantuResponse(getTreesRes);

    }

    public String getParentTreeName(BoardContextType parentContextType, String treeName) {

        String parentTreeName = BoardContextType.GLOBAL == parentContextType ? TREE_SYSTEM
                : treeName;
        logger.debug("getParentTreeName parentContextType: " + parentContextType + ", treeName: "
                + treeName + " ==> parentTreeName: " + parentTreeName);
        return parentTreeName;
    }

    private void verifyExistenceOfBoards(String checkType, Set<String> unseenCNames,
                                         String ownerId, BoardContextType contextType, String treeName) throws VedantuException {

        if (CollectionUtils.isNotEmpty(unseenCNames)) {
            long countExisting = countExistingBoards(unseenCNames, ownerId,
                    contextType, treeName);
            logger.debug("for ownerId: " + ownerId + ", context: " + contextType + ", treeName: "
                    + treeName + " count of " + checkType + " boards in db: " + countExisting);
            if (countExisting != unseenCNames.size()) {
                String errorMsg = "there seem to be unknown boards referenced in the file, "
                        + checkType + ":[" + unseenCNames.stream().collect(Collectors.joining(",")) + "], "
                        + checkType + ".size: " + unseenCNames.size() + ", foundInDB: "
                        + countExisting;
                logger.error("errors: " + errorMsg);
                throw new VedantuException(VedantuErrorCode.BOARD_FILE_UNPARSEABLE, errorMsg);
            }

        }
    }

    public long countExistingBoards(Set<String> cNames, String ownerId,
                                    BoardContextType contextType, String treeName) {

        logger.debug("countExistingBoards cNames: {" + cNames.stream().collect(Collectors.joining(", "))
                + "}, ownerId: " + ownerId + ", contextType: " + contextType + ", treeName: "
                + treeName);

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.orOperator(criteria.and("cName").in(cNames), criteria.and("cAliases").in(cNames));
        criteria.and("ownerId").equals(ownerId);
        criteria.and("treeName").equals(treeName);
        criteria.and("context").equals(contextType);
        query.addCriteria(criteria);
        List<Board> boardList = mongoTemplate.find(query, Board.class);
        logger.debug("countExistingBoards query: " + query.toString());

        long count = boardList.stream().count();
        logger.info("countExistingBoards count: " + count);

        return count;
    }

    public boolean addBoards(Set<BoardNode> boardNodes, String ownerId,
                             BoardContextType contextType, String parentContextOwnerId,
                             BoardContextType parentContextType, String treeName, Set<GradeType> grades)
            throws VedantuException {

        logger.debug("addBoards boardNodes: {" + boardNodes
                + "}, ownerId: " + ownerId + ", contextType: " + contextType
                + ", parentContextOwnerId: " + parentContextOwnerId + ", parentContextType: "
                + parentContextType + ", treeName: " + treeName + ", grades: {"
                + grades.stream().count() + "}");

        if (CollectionUtils.isEmpty(boardNodes)) {
            logger.debug("no boards to add");
            return false;
        }

        // local cache of cName to brdIds
        Map<String, String> cNameToBrdId = new HashMap<String, String>();

        // Add new boards, update aliases
        logger.debug("adding new board nodes");
        for (BoardNode boardNode : boardNodes) {
            if (null == boardNode) {
                continue;
            }
            Board board = getBoard(boardNode.getCanonicalName(), ownerId, contextType, treeName);
            boolean isNew = false;
            if (null == board) {
                // do not allow adding of CONSUMER board if parent not found
                board = new Board(boardNode.name, boardNode.code, ownerId, contextType,
                        boardNode.type, treeName, grades);
                boolean couldPopulate = populateParentContext(parentContextOwnerId,
                        parentContextType, boardNode, parentContextType, treeName, cNameToBrdId,
                        board);
                if (BoardContextType.CONSUMER == contextType && !couldPopulate) {
                    String errorMsg = contextType
                            + " creation is not allowed without parent context, check board [row: "
                            + boardNode.rowNum + ", col: " + boardNode.colNum + "]";
                    logger.error("addBoards " + errorMsg);
                    throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND);
                }
                isNew = true;
            }
            boolean isModified = boardNode.populate(board);
            logger.debug("isNew: " + isNew + ", isModified: " + isModified);
            if (isNew || isModified) {
                logger.debug("will save board: " + board.getCName());
                boardRepo.save(board);
            }
            boardNode.brdId = board._getStringId();

            addToCache(cNameToBrdId, board);

        }

        // Update parentId
        logger.debug("updating parentId");
        for (BoardNode boardNode : boardNodes) {
            if (null == boardNode) {
                continue;
            }
            if (null == boardNode.brdId) {
                String errorMsg = "could not find brdId for boardNode: " + boardNode;
                logger.error("addBoards " + errorMsg);
                throw new VedantuException(VedantuErrorCode.BOARD_FILE_UNPARSEABLE, errorMsg);
            }
            if (null == boardNode.parent || StringUtils.isEmpty(boardNode.parent.brdId)) {
                logger.debug("ignoring parent setting for boardNode: " + boardNode);
                continue;
            }
            Board board = getBoardById(boardNode.brdId);
            board.addParent(boardNode.parent.brdId);
            boardRepo.save(board);
        }

        // Update same, similar and parentContext ids
        logger.debug("updating same, similar and parentContext ids");
        for (BoardNode boardNode : boardNodes) {
            if (null == boardNode) {
                continue;
            }
            if (null == boardNode.brdId) {
                String errorMsg = "could not find brdId for boardNode: " + boardNode;
                logger.error("addBoards " + errorMsg);
                throw new VedantuException(VedantuErrorCode.BOARD_FILE_UNPARSEABLE, errorMsg);
            }

            Board board = getBoardById(boardNode.brdId);
            boolean isModified = false;

            // update sameAsBoards
            if (CollectionUtils.isNotEmpty(boardNode.sameAsBoardNames)) {
                for (String tBoardName : boardNode.sameAsBoardNames) {
                    String tBoardId = getFromCacheOrLoad(cNameToBrdId, tBoardName, ownerId,
                            contextType, treeName);
                    if (StringUtils.isEmpty(tBoardId)) {
                        logger.error("no board found for same-board-name: " + tBoardName);
                        continue;
                    }
                    Board tBoard = getBoardById(tBoardId);
                    if (null == tBoard) {
                        logger.error("no board found for same-board-name: " + tBoardName
                                + ", brdId: " + tBoardId);
                        continue;
                    }

                    tBoard.sameBrdIds.add(board._getStringId());
                    boardRepo.save(tBoard);

                    board.sameBrdIds.add(tBoardId);
                    isModified = true;

                }
            }

            // update similarToBoards
            if (CollectionUtils.isNotEmpty(boardNode.similarToBoardNames)) {
                for (String tBoardName : boardNode.similarToBoardNames) {
                    String tBoardId = getFromCacheOrLoad(cNameToBrdId, tBoardName, ownerId,
                            contextType, treeName);
                    if (StringUtils.isEmpty(tBoardId)) {
                        logger.error("no board found for similar-board-name: " + tBoardName);
                        continue;
                    }
                    Board tBoard = getBoardById(tBoardId);
                    if (null == tBoard) {
                        logger.error("no board found for similar-board-name: " + tBoardName
                                + ", brdId: " + tBoardId);
                        continue;
                    }

                    tBoard.similarBrdIds.add(board._getStringId());
                    boardRepo.save(tBoard);

                    board.similarBrdIds.add(tBoardId);
                    isModified = true;

                }
            }

            if (isModified) {
                logger.debug("saving board: " + board);
                boardRepo.save(board);
            }
        }

        return true;

    }

    public Board getBoard(String cName, String ownerId, BoardContextType contextType,
                          String treeName) {

        logger.debug("getBoard cName: " + cName + ", ownerId: " + ownerId + ", contextType: "
                + contextType + ", treeName: " + treeName);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("cName").equals(cName);
        criteria.and("cAliases").equals(cName);
        criteria.and("ownerId").equals(ownerId);
        criteria.and("treeName").equals(treeName);
        criteria.and("context").equals(contextType);
        query.addCriteria(criteria);
        List<Board> boardList = mongoTemplate.find(query, Board.class);
        logger.debug("getBoard query: " + query.toString());

        Board board = null;
        if (boardList != null) {
            board = boardList.get(0);
        }
        logger.info("getBoard board: " + board);

        return board;
    }

    private boolean populateParentContext(String parentContextOwnerId,
                                          BoardContextType parentContextType, BoardNode boardNode, BoardContextType contextType,
                                          String treeName, Map<String, String> cNameToBrdId, Board board) throws VedantuException {

        logger.info("populateParentContext parentContextOwnerId: " + parentContextOwnerId
                + ", parentContextType: " + parentContextType + ", boardNode: " + boardNode
                + ", contextType: " + contextType + ", treeName: " + treeName + ", board: " + board);

        boolean couldPopulate = false;

        // update parentContext
        if (!StringUtils.isEmpty(parentContextOwnerId) && null != parentContextType) {
            if (!StringUtils.isEmpty(boardNode.parentContextBoardName)) {
                String tBoardName = boardNode.parentContextBoardName;

                String parentTreeName = getParentTreeName(parentContextType, treeName);
                String tBoardId = getFromCacheOrLoad(cNameToBrdId,
                        boardNode.parentContextBoardName, parentContextOwnerId, parentContextType,
                        parentTreeName);

                if (StringUtils.isEmpty(tBoardId)) {
                    logger.error("no board found for parentContext-board-name: " + tBoardName
                            + ", parentContextOwnerId: " + parentContextOwnerId
                            + ", parentContextType: " + parentContextType + ", parentTreeName: "
                            + parentTreeName);
                    return false;
                }
                Board tBoard = getBoardById(tBoardId);
                if (null == tBoard) {
                    logger.error("no board found for parentContext-board-name: " + tBoardName
                            + ", parentContextOwnerId: " + parentContextOwnerId
                            + ", parentContextType: " + parentContextType + ", parentTreeName: "
                            + parentTreeName + ", brdId: " + tBoardId);
                    return false;
                }

                switch (parentContextType) {
                    case CONSUMER:
                        board.consumerBrdId = tBoard._getStringId();
                        board.globalBrdId = tBoard.globalBrdId;
                        couldPopulate = true;
                        break;
                    case GLOBAL:
                        board.globalBrdId = tBoard._getStringId();
                        couldPopulate = true;
                    default:
                        break;
                }
            }
        }

        logger.info("populateParentContext couldPopulate: " + couldPopulate);

        return couldPopulate;
    }

    private String getFromCacheOrLoad(Map<String, String> cNameToBrdId, String boardName,
                                      String ownerId, BoardContextType contextType, String treeName) {

        String tCName = VedantuStringUtils.toCanonicalName(boardName);
        String brdId = cNameToBrdId.get(tCName);
        if (StringUtils.isEmpty(brdId)) {
            Board board = getBoard(tCName, ownerId, contextType, treeName);
            if (null != board) {
                addToCache(cNameToBrdId, board);
                return board._getStringId();
            }
            return null;
        } else {
            return brdId;
        }
    }

    private void addToCache(Map<String, String> cNameToBrdId, Board board) {

        if (!cNameToBrdId.containsKey(board.getCName())) {
            cNameToBrdId.put(board.getCName(), board._getStringId());
        }
        if (CollectionUtils.isNotEmpty(board.getCAliases())) {
            for (String cAlias : board.getCAliases()) {
                if (!cNameToBrdId.containsKey(cAlias)) {
                    cNameToBrdId.put(cAlias, board._getStringId());
                }
            }
        }
    }

    public Board getBoardById(String boardId) throws VedantuException {

        logger.debug("getBoardById boardId: " + boardId);
        Optional<Board> board = boardRepo.findById(boardId);
        if (!board.isPresent()) {
            logger.error("getBoardById cannot find board for _id: " + boardId);
            throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND);
        }
        logger.info("getBoardById board: " + board);
        return board.get();
    }

    private Set<String> collectBoardIds(Set<BoardNode> boardNodes) {

        Set<String> boardIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(boardNodes)) {
            for (BoardNode boardNode : boardNodes) {
                boardIds.add(boardNode.brdId);
            }
        }
        logger.info("collectBoardIds boardIds.size: " + CollectionUtils.size(boardIds));
        return boardIds;
    }

    public GetTreesRes getTreesByIds(Collection<String> boardIds) {

        logger.debug("getTreesByIds boardIds.size: " + CollectionUtils.size(boardIds));

        if (CollectionUtils.isEmpty(boardIds)) {
            logger.debug("getTreeByIds no brdIds given");
            return new GetTreesRes();
        }

        List<Board> boards = getBoardsByIds(ObjectIdUtils
                .toObjectIds(new ArrayList<String>(boardIds)));
        logger.debug("getTreesByIds boards.size: " + CollectionUtils.size(boardIds));

        List<BoardBasicInfo> boardBasicInfos = toBasicInfos(boards);
        logger.debug("getTreesByIds boardBasicInfos.size: " + CollectionUtils.size(boardBasicInfos));

        return getTreesByInfos(boardBasicInfos);
    }

    public List<Board> getBoardsByIds(List<ObjectId> brdIds) {


        List<Board> boards = boardRepo.findByIdIn(brdIds);

        return boards;
    }

    public List<BoardBasicInfo> toBasicInfos(List<Board> results) {
        List<BoardBasicInfo> infosMap = new ArrayList<>();
        for (Board board : results) {
            BoardBasicInfo basicInfo = new BoardBasicInfo(board);
            infosMap.add(basicInfo);
        }
        return infosMap;
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

    public GetTreesRes getTreesByInfos(Collection<BoardBasicInfo> boardBasicInfos) {

        logger.debug("getTreesByInfos boardBasicInfos.size: "
                + CollectionUtils.size(boardBasicInfos));

        if (CollectionUtils.isEmpty(boardBasicInfos)) {
            logger.debug("getTreesByInfos no boardBasicInfos given");
            return new GetTreesRes();
        }

        logger.debug("getTreesByInfos boardBasicInfos.size: "
                + CollectionUtils.size(boardBasicInfos));

        Map<String, BoardBasicInfo> boardBasicInfoMap = basicInfosToMap(boardBasicInfos);

        logger.debug("getTreesByInfos boardBasicInfoMap.size: "
                + CollectionUtils.size(boardBasicInfoMap));
        // query 2
        // get all content update state completed false, pull all topicboards, get all content from

        // query 3

        // ILE corresponding to this and set to temporary state
        return getTreesByInfosMap(boardBasicInfoMap);
    }

    public final <B extends ModelBasicInfo> Map<String, B> basicInfosToMap(Collection<B> results) {

        Map<String, B> infosMap = new LinkedHashMap<String, B>();
        if (CollectionUtils.isNotEmpty(results)) {
            for (B b : results) {
                if (null == b) {
                    continue;
                }
                infosMap.put(b.id, b);
            }
        }
        return infosMap;
    }

    private List<BoardTree> toForest(Map<String, BoardBasicInfo> boardBasicInfoMap) {

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

    public GetChildrenRes getChildren(GetChildrenReq getChildrenReq) throws VedantuException {
        if (null == getChildrenReq) {
            String errorMsg = "null getChildrenReq";
            logger.error("getChildren " + errorMsg);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, errorMsg);
        }
        String validation = getChildrenReq.validate();
        if (!StringUtils.isEmpty(validation)) {
            logger.error(validation);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation);
        }
        Set<String> grantedOrgsIds = new LinkedHashSet<String>();
        //Get all the orgIds that gave access to the current organization
        if (getChildrenReq.showSharedSubjects != null && getChildrenReq.showSharedSubjects != "" && getChildrenReq.showSharedSubjects.equals("show")) {
            AtomicLong totalProgramHits = new AtomicLong(0L);
            List<GranteeOrgProgram> granteeOrgPrograms = getGranteeOrgPrograms(getChildrenReq.ownerId, null, totalProgramHits);
            for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
                grantedOrgsIds.add(granteeOrgProgram.providerOrgId);
            }
        }
        Board brd = null;
        if (!StringUtils.isEmpty(getChildrenReq.parentId)) {
            Optional<Board> brd1 = boardRepo.findById(getChildrenReq.getParentId());
            brd = brd1.get();
        }
        //       // Adding the current organization ID
        grantedOrgsIds.add(getChildrenReq.ownerId);
        Set<Board> boards = new LinkedHashSet<Board>();
        for (String orgId : grantedOrgsIds) {
            if (brd != null && !brd.ownerId.equals(orgId)) {
                orgId = brd.ownerId;
            }
            boards.addAll(getBoardBasicInfos(getChildrenReq.context, orgId,
                    getChildrenReq.type, getChildrenReq.parentId, getChildrenReq.recordState));
        }
        List<Board> boardsList = new ArrayList<Board>();
        boardsList.addAll(boards);
        List<BoardBasicInfo> boardBasicInfos = null;//BoardDAO.INSTANCE.toBasicInfos(boardsList);
        GetChildrenRes getChildrenRes = new GetChildrenRes();
        getChildrenRes.list = boardBasicInfos;
        getChildrenRes.totalHits = boardBasicInfos.size();

        logger.info("getChildren list.size" + CollectionUtils.size(getChildrenRes.list));

        return getChildrenRes;

    }

    public List<GranteeOrgProgram> getGranteeOrgPrograms(String providerOrgId, String departmentId,
                                                         AtomicLong totalHits) {
        logger.debug("getGrateeOrgPrograms orgId: " + providerOrgId
                + ", departmentId: " + departmentId);
        List<GranteeOrgProgram> granteeOrgList = granteeOrgProgramRepo.findAllBySubscriberOrgIdAndRecordState(providerOrgId, VedantuRecordState.ACTIVE);

        totalHits.set(granteeOrgList.stream().count());
        return granteeOrgList;
    }

    public List<Board> getBoardBasicInfos(BoardContextType context, String ownerId, BoardType type,
                                          String parentId) throws VedantuException {

        return getBoardBasicInfos(context, ownerId, type, parentId, VedantuRecordState.ACTIVE);
    }

    public List<Board> getBoardBasicInfos(BoardContextType context, String ownerId, BoardType type,
                                          String parentId, VedantuRecordState state) throws VedantuException {

        logger.debug("getBoardBasicInfos context: " + context + ", ownerId: " + ownerId
                + ", type: " + type + ", parentId: " + parentId);


        Criteria criteria = new Criteria();
        Query query = new Query();
        if (!StringUtils.isEmpty(ownerId)) {
            criteria.and("ownerId").is(ownerId);
        }
        if (!StringUtils.isEmpty(parentId)) {
            criteria.and("parentBrdIds").is(parentId);

        } else {
            // give root nodes for GLOBAL context
            if (BoardContextType.GLOBAL == context) {
                criteria.and("parentBrdIds").is(null);
            }
        }
        criteria.and("context").is(context.name());

        if (null != type) {
            criteria.and("type").is(type.name());
        }

        if (state != null) {
            criteria.and("recordState").is(state.name());
        }
        query.addCriteria(criteria);
        List<Board> bords = mongoTemplate.find(query, Board.class);


        if (null != bords) {
            return bords;
        } else {
            logger.info("getBoardBasicInfos no boards found");
            return new ArrayList<Board>();
        }

    }

}
