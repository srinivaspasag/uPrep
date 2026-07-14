package com.vedantu.board.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.enums.BoardContextType;
import com.vedantu.board.event.details.BoardRemovalDetails;
import com.vedantu.board.models.Board;
import com.vedantu.board.parsers.BoardXLParser;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.board.pojos.BoardNode;
import com.vedantu.board.pojos.BoardTree;
import com.vedantu.board.pojos.requests.GetBoardBasicInfosReq;
import com.vedantu.board.pojos.requests.GetChildrenReq;
import com.vedantu.board.pojos.requests.GetTargetsReq;
import com.vedantu.board.pojos.requests.GetTreesOfBoardsReq;
import com.vedantu.board.pojos.requests.UploadGlobalBoardReq;
import com.vedantu.board.pojos.responses.GetBoardBasicInfosRes;
import com.vedantu.board.pojos.responses.GetChildrenRes;
import com.vedantu.board.pojos.responses.GetTargetsRes;
import com.vedantu.board.pojos.responses.GetTreesRes;
import com.vedantu.commons.AbstractVedantuManager;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.GranteeOrgProgramDAO;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.enums.boards.GradeType;
import com.vedantu.commons.pojos.BoardTreeRes;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.events.manager.EventScheduler;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.mongo.models.GranteeOrgProgram;

public class BoardManager extends AbstractVedantuManager {

    private static final ALogger LOGGER = Logger.of(BoardManager.class);

    public static GetTreesRes uploadBoards(String ownerId, UploadGlobalBoardReq uploadBoardReq,
            BoardContextType contextType, String treeName, Set<GradeType> grades)
            throws VedantuException {

        BoardXLParser boardXLParser = new BoardXLParser(uploadBoardReq.fileName,
                uploadBoardReq.inputFile, contextType);

        if (boardXLParser.hasErrors()) {
            String errors = StringUtils.join(boardXLParser.getErrors(), "\n");
            LOGGER.error("errors: " + errors);
            throw new VedantuException(VedantuErrorCode.BOARD_FILE_UNPARSEABLE, errors);
        } else {
            LOGGER.info("no errors found in file: " + uploadBoardReq.fileName + " stored at: "
                    + uploadBoardReq.inputFile.getAbsolutePath());
        }
        LOGGER.info("parsed : " + boardXLParser);

        BoardContextType parentContextType = BoardContextType.getParentContextType(contextType);
        String parentOwnerId = BoardDAO.OWNER_SYSTEM;
        if (null != parentContextType) {
            String parentTreeName = BoardDAO.getParentTreeName(parentContextType, treeName);
            Set<String> parentContextBoardCNames = boardXLParser.getParentContextBoardCNames();
            verifyExistenceOfBoards("parentContextBoardCNames", parentContextBoardCNames,
                    parentOwnerId, parentContextType, parentTreeName);
        }

        Set<String> unseenCNames = boardXLParser.getUnseenCNames();
        verifyExistenceOfBoards("unseenCNames", unseenCNames, ownerId, contextType, treeName);

        boolean result = BoardDAO.INSTANCE.addBoards(boardXLParser.getAllBoardNodes(), ownerId,
                contextType, parentOwnerId, parentContextType, treeName, grades);
        LOGGER.info("uploadBoards result : " + result);

        Set<String> boardIds = collectBoardIds(boardXLParser.getAllBoardNodes());

        GetTreesRes getTreesRes = getTreesByIds(boardIds);

        return getTreesRes;
    }

    private static Set<String> collectBoardIds(Set<BoardNode> boardNodes) {

        Set<String> boardIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(boardNodes)) {
            for (BoardNode boardNode : boardNodes) {
                boardIds.add(boardNode.brdId);
            }
        }
        LOGGER.info("collectBoardIds boardIds.size: " + CollectionUtils.size(boardIds));
        return boardIds;
    }

    public static GetBoardBasicInfosRes getBoardBasicInfos(
            GetBoardBasicInfosReq getBoardBasicInfosReq) {

        List<ObjectId> brdObjIds = ObjectIdUtils.toObjectIds(getBoardBasicInfosReq.brdIds);
        List<Board> boards = BoardDAO.INSTANCE.getBoardsByIds(brdObjIds);
        List<BoardBasicInfo> boardBasicInfos = BoardDAO.INSTANCE.toBasicInfos(boards);
        GetBoardBasicInfosRes getBoardBasicInfosRes = new GetBoardBasicInfosRes();
        getBoardBasicInfosRes.list = boardBasicInfos;
        getBoardBasicInfosRes.totalHits = boardBasicInfos.size();
        return getBoardBasicInfosRes;
    }

    private static void verifyExistenceOfBoards(String checkType, Set<String> unseenCNames,
            String ownerId, BoardContextType contextType, String treeName) throws VedantuException {

        if (CollectionUtils.isNotEmpty(unseenCNames)) {
            long countExisting = BoardDAO.INSTANCE.countExistingBoards(unseenCNames, ownerId,
                    contextType, treeName);
            LOGGER.debug("for ownerId: " + ownerId + ", context: " + contextType + ", treeName: "
                    + treeName + " count of " + checkType + " boards in db: " + countExisting);
            if (countExisting != unseenCNames.size()) {
                String errorMsg = "there seem to be unknown boards referenced in the file, "
                        + checkType + ":[" + StringUtils.join(unseenCNames, ", ") + "], "
                        + checkType + ".size: " + unseenCNames.size() + ", foundInDB: "
                        + countExisting;
                LOGGER.error("errors: " + errorMsg);
                throw new VedantuException(VedantuErrorCode.BOARD_FILE_UNPARSEABLE, errorMsg);
            }
        }
    }

    public static GetChildrenRes getChildren(GetChildrenReq getChildrenReq) throws VedantuException {

        if (null == getChildrenReq) {
            String errorMsg = "null getChildrenReq";
            LOGGER.error("getChildren " + errorMsg);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, errorMsg);
        }
        String validation = getChildrenReq.validate();
        if (StringUtils.isNotEmpty(validation)) {
            LOGGER.error(validation);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation);
        }
        Set<String> grantedOrgsIds = new LinkedHashSet<String>();
        //Get all the orgIds that gave access to the current organization
        if (getChildrenReq.showSharedSubjects != null && getChildrenReq.showSharedSubjects != "" && getChildrenReq.showSharedSubjects.equals("show")){
          MutableLong totalProgramHits = new MutableLong(0L);
          List<GranteeOrgProgram> granteeOrgPrograms = GranteeOrgProgramDAO.INSTANCE.getGranteeOrgPrograms(getChildrenReq.ownerId, null, totalProgramHits);
          for (GranteeOrgProgram granteeOrgProgram : granteeOrgPrograms) {
            grantedOrgsIds.add(granteeOrgProgram.providerOrgId);
          }
        }
        Board brd = null;
        if(StringUtils.isNotEmpty(getChildrenReq.parentId)){
            brd = BoardDAO.INSTANCE.getById(getChildrenReq.parentId);
        }
        //       // Adding the current organization ID
        grantedOrgsIds.add(getChildrenReq.ownerId);
        Set<Board> boards = new LinkedHashSet<Board>();
        for (String orgId : grantedOrgsIds) {
            if (brd != null && !brd.ownerId.equals(orgId)) {
                orgId = brd.ownerId;
            }
            boards.addAll(BoardDAO.INSTANCE.getBoardBasicInfos(getChildrenReq.context, orgId,
                    getChildrenReq.type, getChildrenReq.parentId, getChildrenReq.recordState));
        }
        List<Board> boardsList = new ArrayList<Board>();
        boardsList.addAll(boards);
        List<BoardBasicInfo> boardBasicInfos = BoardDAO.INSTANCE.toBasicInfos(boardsList);
        GetChildrenRes getChildrenRes = new GetChildrenRes();
        getChildrenRes.list = boardBasicInfos;
        getChildrenRes.totalHits = boardBasicInfos.size();

        LOGGER.info("getChildren list.size" + CollectionUtils.size(getChildrenRes.list));

        return getChildrenRes;
    }

    public static GetTargetsRes getTargets(GetTargetsReq getTargetsReq) throws VedantuException {

        if (null == getTargetsReq) {
            String errorMsg = "null getTargetsReq";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, errorMsg);
        }
        String validation = getTargetsReq.validate();
        if (StringUtils.isNotEmpty(validation)) {
            LOGGER.error(validation);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, validation);
        }
        // UJJ: There is no concept of targets right now.
        GetTargetsRes getTargetsRes = new GetTargetsRes();
        return getTargetsRes;
    }

    public static Map<String, BoardBasicInfo> getInfosMap(Set<String> brdIds) {

        return BoardDAO.INSTANCE.getBasicInfosByIds(brdIds);
    }

    public static GetTreesRes getTreesByIds(Collection<String> boardIds) {

        LOGGER.debug("getTreesByIds boardIds.size: " + CollectionUtils.size(boardIds));

        if (CollectionUtils.isEmpty(boardIds)) {
            LOGGER.debug("getTreeByIds no brdIds given");
            return new GetTreesRes();
        }

        List<Board> boards = BoardDAO.INSTANCE.getBoardsByIds(ObjectIdUtils
                .toObjectIds(new ArrayList<String>(boardIds)));
        LOGGER.debug("getTreesByIds boards.size: " + CollectionUtils.size(boardIds));

        List<BoardBasicInfo> boardBasicInfos = BoardDAO.INSTANCE.toBasicInfos(boards);
        LOGGER.debug("getTreesByIds boardBasicInfos.size: " + CollectionUtils.size(boardBasicInfos));

        return getTreesByInfos(boardBasicInfos);
    }

    public static GetTreesRes getTreesByInfos(Collection<BoardBasicInfo> boardBasicInfos) {

        LOGGER.debug("getTreesByInfos boardBasicInfos.size: "
                + CollectionUtils.size(boardBasicInfos));

        if (CollectionUtils.isEmpty(boardBasicInfos)) {
            LOGGER.debug("getTreesByInfos no boardBasicInfos given");
            return new GetTreesRes();
        }

        LOGGER.debug("getTreesByInfos boardBasicInfos.size: "
                + CollectionUtils.size(boardBasicInfos));

        Map<String, BoardBasicInfo> boardBasicInfoMap = BoardDAO.INSTANCE
                .basicInfosToMap(boardBasicInfos);

        LOGGER.debug("getTreesByInfos boardBasicInfoMap.size: "
                + CollectionUtils.size(boardBasicInfoMap));
        // query 2
        // get all content update state completed false, pull all topicboards, get all content from

        // query 3

        // ILE corresponding to this and set to temporary state
        return getTreesByInfosMap(boardBasicInfoMap);
    }

    public static GetTreesRes getTreesByInfosMap(Map<String, BoardBasicInfo> boardBasicInfoMap) {

        LOGGER.trace("getTreesByInfosMap boardBasicInfoMap.size: "
                + CollectionUtils.size(boardBasicInfoMap));

        GetTreesRes getTreesRes = new GetTreesRes();

        if (MapUtils.isEmpty(boardBasicInfoMap)) {
            LOGGER.debug("getTreesByInfosMap no boardBasicInfoMap given");
            return getTreesRes;
        }

        List<BoardTree> roots = toForest(boardBasicInfoMap);
        getTreesRes.list = roots;
        getTreesRes.totalHits = roots.size();

        LOGGER.trace("getTreesByInfosMap roots.size: " + CollectionUtils.size(roots));

        return getTreesRes;
    }

    public static GetTreesRes getTreesOfBoards(GetTreesOfBoardsReq getTreesOfBoardsReq)
            throws VedantuException {

        if (null == getTreesOfBoardsReq) {
            String errorMsg = "null getTreesOfBoardsReq";
            LOGGER.error("getTreesOfBoards " + errorMsg);
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, errorMsg);
        }

        List<Board> boards = BoardDAO.INSTANCE.collectHierarchy(getTreesOfBoardsReq.context,
                getTreesOfBoardsReq.ownerId, new HashSet<String>(getTreesOfBoardsReq.treeRootIds),
                getTreesOfBoardsReq.depth);

        LOGGER.trace("getTreesOfBoards boards.size: " + CollectionUtils.size(boards));
        List<BoardBasicInfo> boardBasicInfos = BoardDAO.INSTANCE.toBasicInfos(boards);
        LOGGER.debug("getTreesOfBoards boardBasicInfos.size: "
                + CollectionUtils.size(boardBasicInfos));

        return getTreesByInfos(boardBasicInfos);
    }

    private static List<BoardTree> toForest(Map<String, BoardBasicInfo> boardBasicInfoMap) {

        LOGGER.trace("toForest boardBasicInfoMap.size: " + CollectionUtils.size(boardBasicInfoMap));

        List<BoardTree> roots = new ArrayList<BoardTree>();

        if (MapUtils.isEmpty(boardBasicInfoMap)) {
            LOGGER.debug("toForest null/empty boardBasicInfoMap given");
            return roots;
        }

        // Create the basic 1-to-1 map of boardTree
        Map<String, BoardTree> treeMap = new HashMap<String, BoardTree>();
        LOGGER.debug("toForest creating treeMap");
        for (Map.Entry<String, BoardBasicInfo> entry : boardBasicInfoMap.entrySet()) {

            final String id = entry.getKey();
            final BoardBasicInfo board = entry.getValue();

            if (StringUtils.isEmpty(id) || null == board) {
                continue;
            }

            treeMap.put(id, new BoardTree(board));
        }
        LOGGER.debug("toForest created treeMap.size: " + CollectionUtils.size(treeMap));

        // Populate parent, children
        LOGGER.debug("toForest populating parent/children");
        for (Map.Entry<String, BoardTree> entry : treeMap.entrySet()) {

            final BoardTree boardTree = entry.getValue();

            LOGGER.debug("toForest populating parent/children for board: " + boardTree.board.name);

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

        LOGGER.trace("toForest selecting root entries");
        // Select root trees of the forest
        for (Map.Entry<String, BoardTree> entry : treeMap.entrySet()) {

            final BoardTree boardTree = entry.getValue();

            if (CollectionUtils.isEmpty(boardTree.parents)) {
                roots.add(boardTree);
            }
        }
        LOGGER.trace("toForest selected root entries, roots.size: " + CollectionUtils.size(roots));

        LOGGER.trace("toForest setting levels for nodes");
        for (BoardTree root : roots) {
            root._setLevel(0);
        }
        LOGGER.debug("toForest setting levels for nodes completed");

        return roots;
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
        LOGGER.error("returning boards tree : " + boardTreeRes);
        return boardTreeRes;
    }

    public static boolean delete(String userId, String boardId, String parentBoardId)
            throws VedantuException {

        // query 1

        Board nodeToBeDeleted = BoardDAO.INSTANCE.getById(boardId);
        List<String> deltedBrdIds = new ArrayList<String>();
        boolean deleteSucceeded = BoardDAO.INSTANCE.mark(boardId, deltedBrdIds,
                VedantuRecordState.DELETED, nodeToBeDeleted.recordState);

        if (deleteSucceeded) {
            BoardRemovalDetails details;
            try {

                details = new BoardRemovalDetails();
                details.brdIds.addAll(deltedBrdIds);
                details.userId = userId;
                // TODO have better logic to decide which BoardType needs state change
                details.changeState = (nodeToBeDeleted.type == BoardType.SUBSUBTOPIC || nodeToBeDeleted.type == BoardType.SUBTOPIC) ? false
                        : true;
                EventScheduler.generateEventAysc(userId, details, EventType.REMOVE_BOARD);
            } catch (ClassNotFoundException e) {

                throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED);
            }
            return true;
        }

        return false;
    }

    public static boolean markActive(String userId, String boardId, String parentBoardId)
            throws VedantuException {

        if (StringUtils.isNotEmpty(parentBoardId)) {
            Board parentNode = BoardDAO.INSTANCE.getById(parentBoardId);
            if (parentNode.recordState != VedantuRecordState.ACTIVE) {
                throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED,
                        "parent node is not active");
            }
        }

        Board nodeToBeActivated = BoardDAO.INSTANCE.getById(boardId);
        if (StringUtils.isEmpty(parentBoardId)
                && CollectionUtils.isNotEmpty(nodeToBeActivated.parentBrdIds)) {
            throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_UPDATED,
                    "parent node is not active");
        }
        List<String> deltedBrdIds = new ArrayList<String>();
        boolean updatedSuccessfully = BoardDAO.INSTANCE.mark(boardId, deltedBrdIds,
                VedantuRecordState.ACTIVE, nodeToBeActivated.recordState);

        return updatedSuccessfully;
    }
}
