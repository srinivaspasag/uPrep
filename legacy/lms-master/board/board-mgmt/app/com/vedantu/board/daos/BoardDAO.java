package com.vedantu.board.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.board.enums.BoardContextType;
import com.vedantu.board.models.Board;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.board.pojos.BoardNode;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.enums.boards.GradeType;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.models.GranteeOrgProgram;

public class BoardDAO extends VedantuBasicDAO<Board, ObjectId> {

    private static final ALogger LOGGER       = Logger.of(BoardDAO.class);

    public static final BoardDAO INSTANCE     = new BoardDAO();

    public static final String   OWNER_SYSTEM = "SYSTEM";
    public static final String   TREE_SYSTEM  = "SYSTEM";

    private BoardDAO() {

        super(Board.class);
    }

    public long countExistingBoards(Set<String> cNames, String ownerId,
            BoardContextType contextType, String treeName) {

        LOGGER.debug("countExistingBoards cNames: {" + StringUtils.join(cNames, ", ")
                + "}, ownerId: " + ownerId + ", contextType: " + contextType + ", treeName: "
                + treeName);

        Query<Board> query = getQuery();
        query.and(query.or(query.criteria("cName").hasAnyOf(cNames), query.criteria("cAliases")
                .hasAnyOf(cNames)), query
                .and(query.criteria("ownerId").equal(ownerId),
                        query.criteria("treeName").equal(treeName), query.criteria("context")
                                .equal(contextType)));
        LOGGER.debug("countExistingBoards query: " + query.toString());

        long count = query.countAll();
        LOGGER.info("countExistingBoards count: " + count);

        return count;
    }

    public Board getBoard(String cName, String ownerId, BoardContextType contextType,
            String treeName) {

        LOGGER.debug("getBoard cName: " + cName + ", ownerId: " + ownerId + ", contextType: "
                + contextType + ", treeName: " + treeName);

        Query<Board> query = getQuery();
        query.and(query.or(query.criteria("cName").equal(cName),
                query.criteria("cAliases").equal(cName)), query.and(query.criteria("ownerId")
                .equal(ownerId), query.criteria("treeName").equal(treeName),
                query.criteria("context").equal(contextType)));
        LOGGER.debug("getBoard query: " + query.toString());

        Board board = query.get();
        LOGGER.info("getBoard board: " + board);

        return board;
    }

    public List<Board> getBoardsByIds(List<ObjectId> brdIds) {

        LOGGER.debug("getBoardsByIds brdIds: {" + StringUtils.join(brdIds, ", ") + "}");

        List<Board> boards = getByIds(brdIds);
        LOGGER.info("getBoardsByIds boards: {" + StringUtils.join(boards, ",\n") + "}");
        return boards;
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

    public boolean addBoards(Set<BoardNode> boardNodes, String ownerId,
            BoardContextType contextType, String parentContextOwnerId,
            BoardContextType parentContextType, String treeName, Set<GradeType> grades)
            throws VedantuException {

        LOGGER.debug("addBoards boardNodes: {" + StringUtils.join(boardNodes, ", ")
                + "}, ownerId: " + ownerId + ", contextType: " + contextType
                + ", parentContextOwnerId: " + parentContextOwnerId + ", parentContextType: "
                + parentContextType + ", treeName: " + treeName + ", grades: {"
                + StringUtils.join(grades, ", ") + "}");

        if (CollectionUtils.isEmpty(boardNodes)) {
            LOGGER.debug("no boards to add");
            return false;
        }

        // local cache of cName to brdIds
        Map<String, String> cNameToBrdId = new HashMap<String, String>();

        // Add new boards, update aliases
        LOGGER.debug("adding new board nodes");
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
                    LOGGER.error("addBoards " + errorMsg);
                    throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND);
                }
                isNew = true;
            }
            boolean isModified = boardNode.populate(board);
            LOGGER.debug("isNew: " + isNew + ", isModified: " + isModified);
            if (isNew || isModified) {
                LOGGER.debug("will save board: " + board.getCName());
                save(board);
            }
            boardNode.brdId = board._getStringId();

            addToCache(cNameToBrdId, board);

        }

        // Update parentId
        LOGGER.debug("updating parentId");
        for (BoardNode boardNode : boardNodes) {
            if (null == boardNode) {
                continue;
            }
            if (null == boardNode.brdId) {
                String errorMsg = "could not find brdId for boardNode: " + boardNode;
                LOGGER.error("addBoards " + errorMsg);
                throw new VedantuException(VedantuErrorCode.BOARD_FILE_UNPARSEABLE, errorMsg);
            }
            if (null == boardNode.parent || StringUtils.isEmpty(boardNode.parent.brdId)) {
                LOGGER.debug("ignoring parent setting for boardNode: " + boardNode);
                continue;
            }
            Board board = getBoardById(boardNode.brdId);
            board.addParent(boardNode.parent.brdId);
            save(board);
        }

        // Update same, similar and parentContext ids
        LOGGER.debug("updating same, similar and parentContext ids");
        for (BoardNode boardNode : boardNodes) {
            if (null == boardNode) {
                continue;
            }
            if (null == boardNode.brdId) {
                String errorMsg = "could not find brdId for boardNode: " + boardNode;
                LOGGER.error("addBoards " + errorMsg);
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
                        LOGGER.error("no board found for same-board-name: " + tBoardName);
                        continue;
                    }
                    Board tBoard = getBoardById(tBoardId);
                    if (null == tBoard) {
                        LOGGER.error("no board found for same-board-name: " + tBoardName
                                + ", brdId: " + tBoardId);
                        continue;
                    }

                    tBoard.sameBrdIds.add(board._getStringId());
                    save(tBoard);

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
                        LOGGER.error("no board found for similar-board-name: " + tBoardName);
                        continue;
                    }
                    Board tBoard = getBoardById(tBoardId);
                    if (null == tBoard) {
                        LOGGER.error("no board found for similar-board-name: " + tBoardName
                                + ", brdId: " + tBoardId);
                        continue;
                    }

                    tBoard.similarBrdIds.add(board._getStringId());
                    save(tBoard);

                    board.similarBrdIds.add(tBoardId);
                    isModified = true;

                }
            }

            if (isModified) {
                LOGGER.debug("saving board: " + board);
                save(board);
            }
        }

        return true;

    }

    private Board getBoardById(String boardId) throws VedantuException {

        LOGGER.debug("getBoardById boardId: " + boardId);
        Board board = getById(boardId);
        if (null == board) {
            LOGGER.error("getBoardById cannot find board for _id: " + boardId);
            throw new VedantuException(VedantuErrorCode.BOARD_NOT_FOUND);
        }
        LOGGER.info("getBoardById board: " + board);
        return board;
    }

    private boolean populateParentContext(String parentContextOwnerId,
            BoardContextType parentContextType, BoardNode boardNode, BoardContextType contextType,
            String treeName, Map<String, String> cNameToBrdId, Board board) throws VedantuException {

        LOGGER.info("populateParentContext parentContextOwnerId: " + parentContextOwnerId
                + ", parentContextType: " + parentContextType + ", boardNode: " + boardNode
                + ", contextType: " + contextType + ", treeName: " + treeName + ", board: " + board);

        boolean couldPopulate = false;

        // update parentContext
        if (StringUtils.isNotEmpty(parentContextOwnerId) && null != parentContextType) {
            if (StringUtils.isNotEmpty(boardNode.parentContextBoardName)) {
                String tBoardName = boardNode.parentContextBoardName;

                String parentTreeName = getParentTreeName(parentContextType, treeName);
                String tBoardId = getFromCacheOrLoad(cNameToBrdId,
                        boardNode.parentContextBoardName, parentContextOwnerId, parentContextType,
                        parentTreeName);

                if (StringUtils.isEmpty(tBoardId)) {
                    LOGGER.error("no board found for parentContext-board-name: " + tBoardName
                            + ", parentContextOwnerId: " + parentContextOwnerId
                            + ", parentContextType: " + parentContextType + ", parentTreeName: "
                            + parentTreeName);
                    return false;
                }
                Board tBoard = getBoardById(tBoardId);
                if (null == tBoard) {
                    LOGGER.error("no board found for parentContext-board-name: " + tBoardName
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

        LOGGER.info("populateParentContext couldPopulate: " + couldPopulate);

        return couldPopulate;
    }

    public static String getParentTreeName(BoardContextType parentContextType, String treeName) {

        String parentTreeName = BoardContextType.GLOBAL == parentContextType ? BoardDAO.TREE_SYSTEM
                : treeName;
        LOGGER.debug("getParentTreeName parentContextType: " + parentContextType + ", treeName: "
                + treeName + " ==> parentTreeName: " + parentTreeName);
        return parentTreeName;
    }

    public Map<String, BoardBasicInfo> getBasicInfosByIds(Set<String> ids) {

        LOGGER.debug("getBasicInfosByIds ids: {" + StringUtils.join(ids, ", ") + "}");
        List<Board> results = getByIds(ObjectIdUtils.toObjectIds(new ArrayList<String>(ids), true));
        Map<String, BoardBasicInfo> basicInfoMap = toBasicInfosMap(results);
        LOGGER.info("getBasicInfosByIds basicInfoMap: {" + StringUtils.join(basicInfoMap, ", ")
                + "}");
        return basicInfoMap;
    }

    public List<Board> getBoardBasicInfos(BoardContextType context, String ownerId, BoardType type,
            String parentId) throws VedantuException {

        return getBoardBasicInfos(context, ownerId, type, parentId, VedantuRecordState.ACTIVE);
    }

    public List<Board> getBoardBasicInfos(BoardContextType context, String ownerId, BoardType type,
            String parentId, VedantuRecordState state) throws VedantuException {

        LOGGER.debug("getBoardBasicInfos context: " + context + ", ownerId: " + ownerId
                + ", type: " + type + ", parentId: " + parentId);

        DBObject query = new BasicDBObject();
        if (StringUtils.isNotEmpty(ownerId)) {
            query.put("ownerId", ownerId);
        }
        if (StringUtils.isNotEmpty(parentId)) {
            query.put("parentBrdIds", parentId);
        } else {
            // give root nodes for GLOBAL context
            if (BoardContextType.GLOBAL == context) {
                query.put("parentBrdIds", null);
            }
        }
        query.put("context", context.name());
        if (null != type) {
            query.put("type", type.name());
        }

        if (state != null) {
            query.put("recordState", state.name());
        }
        DBObject order = new BasicDBObject("cName", MongoManager.SortOrder.ASC.getValue());

        VedantuDBResult<Board> boardsDBResult = getInfos(query, null, MongoManager.NO_START,
                MongoManager.NO_LIMIT, order);

        if (null != boardsDBResult) {
            List<Board> boards = boardsDBResult.results;
            LOGGER.info("getBoardBasicInfos boards: {" + StringUtils.join(boards, ", ") + "}");
            return boards;
        } else {
            LOGGER.info("getBoardBasicInfos no boards found");
            return new ArrayList<Board>();
        }

    }

    public List<Board> collectHierarchy(BoardContextType context, String ownerId,
            Set<String> boardIds, int depth) {

        LOGGER.debug("collectHierarchy context: " + context + ", ownerId: " + ownerId + ", depth: "
                + depth + ", boardIds: {" + StringUtils.join(boardIds, ", ") + "}");

        if (CollectionUtils.isEmpty(boardIds) || depth < 0) {
            LOGGER.debug("collectHierarchy empty boardIds or depth < 0");
            return new ArrayList<Board>();
        }

        LOGGER.debug("fetching parent boards first");

        Query<Board> query = getQuery().field(FIELD_ID)
                .hasAnyOf(ObjectIdUtils.toObjectIds(new ArrayList<String>(boardIds)))
                .filter("ownerId", ownerId).filter("context", context);
        LOGGER.debug("fetching parent boards query: " + query);

        List<Board> boards = query.asList();
        depth--;
        LOGGER.debug("parent boards size: " + CollectionUtils.size(boards) + ", depth: " + depth);

        if (CollectionUtils.isNotEmpty(boards)) {
            List<Board> parentBoards = boards;
            while (depth-- > 0 && CollectionUtils.isNotEmpty(parentBoards)) {
                LOGGER.debug("processing at depth: " + depth);
                Set<String> parentBoardIds = new HashSet<String>();
                for (Board parentBoard : parentBoards) {
                    if (null == parentBoard) {
                        continue;
                    }
                    parentBoardIds.add(parentBoard._getStringId());
                }
                LOGGER.debug("parentBoardIds.size: " + CollectionUtils.size(parentBoardIds));

                query = getQuery().filter("ownerId", ownerId).field("parentBrdIds")
                        .hasAnyOf(parentBoardIds).filter("context", context);
                LOGGER.debug("fetching parent boards query: " + query);

                List<Board> childrenBoards = query.asList();
                LOGGER.debug("childrenBoards.size: " + CollectionUtils.size(childrenBoards));

                if (CollectionUtils.isNotEmpty(childrenBoards)) {
                    boards.addAll(childrenBoards);
                }
                LOGGER.debug("after cumulation boards.size: " + CollectionUtils.size(boards));
                parentBoards = childrenBoards;
            }
        }

        LOGGER.info("collectHierarchy boards.size: " + CollectionUtils.size(boards));

        return boards;

    }

    public boolean mark(String boardId, List<String> brdIds, VedantuRecordState state, VedantuRecordState existing)
            throws VedantuException {

        if (brdIds == null) {
            return false;
        }

        UpdateOperations<Board> boardUpdates = getDS().createUpdateOperations(Board.class);

        boardUpdates.set(ConstantsGlobal.RECORD_STATE, state);

        Query<Board> findQuery = getQuery();

        findQuery.or(findQuery.criteria(VedantuBasicDAO.FIELD_ID).equal(new ObjectId(boardId)),
                findQuery.criteria("parentBrdIds").in(Arrays.asList(boardId)));
        findQuery.filter(ConstantsGlobal.RECORD_STATE, existing);

        LOGGER.debug(" FindQuery " + findQuery.toString());
        List<Key<Board>> keys = findQuery.asKeyList();

        UpdateResults<Board> updateResult = this.update(findQuery, boardUpdates);

        if (updateResult.getHadError()) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED,
                    "Failed to update boards");
        }

        if (CollectionUtils.isNotEmpty(keys)) {
            for (Key<Board> key : keys) {
                LOGGER.debug(" Key log" + key.getId().toString());

                brdIds.add(key.getId().toString());

            }
            LOGGER.debug("Ids" + brdIds);
        }

        return true;
    }

    public List<Board> getAllCourses(String orgId, boolean showSharedSubjects) {
        List<Board> boards = new ArrayList<Board>();
        Set<String> grantedOrgsIds = new LinkedHashSet<String>();
        //Get all the orgIds that gave access to the current organization
        grantedOrgsIds.add(orgId);
        if (showSharedSubjects){
          MutableLong totalProgramHits = new MutableLong(0L);
          List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(orgId, null, totalProgramHits);
          for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            grantedOrgsIds.add(granteeOrgProgram.providerOrgId);
          }
        }
        Query<Board> query = getQuery();
        for(String organizationId : grantedOrgsIds){
            query.filter("type", BoardType.COURSE);
            query.filter("recordState", VedantuRecordState.ACTIVE);
            query.filter("ownerId", organizationId);
            boards.addAll(query.asList());
        }
        return boards;
    }

    public List<String> getAllCoursesIds(String orgId) {
        List<String> brdIds = new ArrayList<String>();
        List<Board> boards = null;
        Query<Board> query = getQuery();
        query.filter("type", BoardType.COURSE);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        query.filter("ownerId", orgId);
        boards = query.asList();
        for(Board board : boards){
            brdIds.add(board._getStringId());
        }
        return brdIds;
    }

    public List<Board> getAllCoursesExcept(List<ObjectId> boardIds, String orgId) {
        List<Board> boards = null;
        Query<Board> query = getQuery();
        query.filter("type", BoardType.COURSE);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        query.filter("ownerId", orgId);
        query.field("_id").notIn(boardIds);
        boards = query.asList();
        return boards;
    }

    public List<Board> getAllChildren(String parentOrgId, String boardId) {
        Query<Board> query = getQuery();
        query.filter("type", BoardType.TOPIC);
        //query.filter("ownerId", parentOrgId);
        query.filter("recordState", VedantuRecordState.ACTIVE);
        List<String> parentBoardIds = new ArrayList<String>();
        parentBoardIds.add(boardId);
        query.field("parentBrdIds").in(parentBoardIds);
        return query.asList();
    }
}
