package com.vedantu.content.search.details;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.board.managers.BoardManager;
import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.board.pojos.BoardTree;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.pojos.BoardTreeRes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.models.AbstractContentStatsModel;
import com.vedantu.content.pojos.ContentSize;
import com.vedantu.content.search.details.boards.BoardSearchEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

public abstract class AbstractBoardSearchEntityTagDetails extends AbstractSearchDetail {

    private static final ALogger  LOGGER       = Logger.of(AbstractBoardSearchEntityTagDetails.class);
    public static final String    DIFFICULTY   = "difficulty";
    public static final String    COMPLETED    = "completed";

    // this field will not be indexed, it will be used only for while decorating
    // response for UI
    public List<BoardTreeRes>     boardTree;
    public Set<BoardSearchEntity> boards;
    public Set<BoardSearchEntity> targets;
    public SrcEntity              contentSrc;
    public Set<String>            tags;
    public Scope                  scope;
    public boolean                completed;
    // not used for now
    public double                 avgRating;
    public long                   views;
    public long                   followers;
    public long                   comments;
    public long                   upVotes;
    public Difficulty             difficulty;
    public String                 name;
    public ContentSize            size;
    public long                   startTime;
    public long                   endTime;
    public long                   closeTime;

    private boolean               addBoardInfo = true;
    public String                 userId;

    public AbstractBoardSearchEntityTagDetails() {

        super();
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        if (boards != null) {
            JSONArray jsonArray = new JSONArray();
            for (BoardSearchEntity brd : boards) {
                jsonArray.put(brd.toJSON());
            }
            json.put(ConstantsGlobal.BOARDS, jsonArray);
        }
        if (targets != null) {
            JSONArray jsonArray = new JSONArray();
            for (BoardSearchEntity brd : targets) {
                jsonArray.put(brd.toJSON());
            }
            json.put(ConstantsGlobal.TARGETS, jsonArray);
        }
        if (CollectionUtils.isNotEmpty(tags)) {
            json.put(ConstantsGlobal.TAGS, tags);
        }
        json.put(ConstantsGlobal.VIEWS, views);
        json.put(ConstantsGlobal.COMMENTS, comments);
        json.put(ConstantsGlobal.FOLLOWERS, followers);
        json.put(ConstantsGlobal.AVG_RATING, avgRating);
        json.put(ConstantsGlobal.UP_VOTES, upVotes);
        json.put(COMPLETED, completed);
        if (scope != null) {
            json.put(ConstantsGlobal.SCPOE, scope.name());
        }
        if (contentSrc != null) {
            json.put(ConstantsGlobal.CONTENT_SRC, contentSrc.toJSON());
        }
        if (StringUtils.isNotEmpty(name)) {
            json.put(ConstantsGlobal.NAME, name);
        }
        json.put(DIFFICULTY, difficulty);
        json.put(ConstantsGlobal.USER_ID, userId);
        if (size != null) {
            json.put(ConstantsGlobal.SIZE, size.toJSON());
        }

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        boards = getBoradSearchEntityFromJSON(JSONUtils.getJSONArray(json, ConstantsGlobal.BOARDS));
        targets = getBoradSearchEntityFromJSON(JSONUtils
                .getJSONArray(json, ConstantsGlobal.TARGETS));
        tags = JSONUtils.getSet(json, ConstantsGlobal.TAGS);
        views = JSONUtils.getLong(json, ConstantsGlobal.VIEWS);
        comments = JSONUtils.getLong(json, ConstantsGlobal.COMMENTS);
        followers = JSONUtils.getLong(json, ConstantsGlobal.FOLLOWERS);
        avgRating = JSONUtils.getDouble(json, ConstantsGlobal.AVG_RATING);
        upVotes = JSONUtils.getLong(json, ConstantsGlobal.UP_VOTES);
        scope = Scope.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.SCPOE));
        completed = JSONUtils.getBoolean(json, COMPLETED);
        JSONObject contentSrc = JSONUtils.getJSONObject(json, ConstantsGlobal.CONTENT_SRC);
        if (contentSrc != null && contentSrc.length() > 0) {
            this.contentSrc = new SrcEntity();
            this.contentSrc.fromJSON(contentSrc);

        }
        difficulty = Difficulty.valueOfKey(JSONUtils.getString(json, DIFFICULTY));
        userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);

        name = JSONUtils.getString(json, ConstantsGlobal.NAME);
        size = new ContentSize();
        JSONObject sizeJSON = JSONUtils.getJSONObject(json, ConstantsGlobal.SIZE);
        if (sizeJSON.length() > 0) {
            size.fromJSON(sizeJSON);
        }
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        if (mongoModel instanceof AbstractContentStatsModel) {
            AbstractContentStatsModel abstractBoardModel = (AbstractContentStatsModel) mongoModel;
            followers = abstractBoardModel.followers;
            upVotes = abstractBoardModel.upVotes;
            views = abstractBoardModel.views;
            comments = abstractBoardModel.comments;
            size = abstractBoardModel.size;
        }
        AbstractBoardEntityTagModel tagModel = (AbstractBoardEntityTagModel) mongoModel;
        scope = tagModel.scope;
        tags = tagModel.tags;
        contentSrc = tagModel.contentSrc;
        Map<String, BoardSearchEntity> brdEntityMap = addBoardInfo ? toBoardSearchEntityMap(BoardManager
                .getInfosMap(tagModel.__getAllBoardIds())) : null;
        contentSrc = tagModel.contentSrc;
        if (addBoardInfo) {
            boards = getBoardsSet(tagModel.boardIds, brdEntityMap);
            LOGGER.debug(" Boards " + boards + ", " + boards);
            targets = getBoardsSet(tagModel.targetIds, brdEntityMap);
        }
        completed = tagModel.completed;
        difficulty = tagModel.difficulty;
        userId = tagModel.userId;
        name = tagModel.name;

    }

    public BoardSearchEntity __getBoard(BoardType boardType) {

        BoardSearchEntity brd = null;
        if (boards != null && !boards.isEmpty()) {
            for (BoardSearchEntity b : boards) {
                if (b.type == boardType) {
                    brd = b;
                    break;
                }
            }
        }
        return brd;
    }

    public List<BoardSearchEntity> __getBoards(BoardType boardType) {

        List<BoardSearchEntity> brds = new ArrayList<BoardSearchEntity>();
        if (boards != null && !boards.isEmpty()) {
            for (BoardSearchEntity b : boards) {
                if (b.type == boardType) {
                    brds.add(b);
                }
            }
        }
        return brds;
    }

    public Set<String> _getBoardsIds() {

        Set<String> brdIds = new HashSet<String>();
        if (boards != null) {
            for (BoardSearchEntity brd : boards) {
                brdIds.add(brd.id);
            }
        }
        if (targets != null) {
            for (BoardSearchEntity brd : targets) {
                brdIds.add(brd.id);
            }
        }
        return brdIds;
    }

    public void __addBoardDetails(boolean add) {

        this.addBoardInfo = add;
    }

    // it return Course wise boardTree

    public List<BoardTreeRes> fetchBoardTree(Map<String, BoardBasicInfo> boardsInfoMap) {

        Map<String, BoardBasicInfo> boardSearchEntityMap = fetchBoardSearchEntityLocalMap(boardsInfoMap);
        List<BoardTree> boardTree = BoardManager.getTreesByInfosMap(boardSearchEntityMap).list;
        return BoardManager.toBoardTreeRes(boardTree);
    }

    private static Set<BoardSearchEntity> getBoradSearchEntityFromJSON(JSONArray jsonArray) {

        Set<BoardSearchEntity> boards = new HashSet<BoardSearchEntity>();
        if (jsonArray == null) {
            LOGGER.error("empty boardSearchEntity");
            return boards;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            boards.add((BoardSearchEntity) JSONUtils.getJSONAware(new BoardSearchEntity(),
                    jsonArray, i));
        }
        return boards;
    }

    private Set<BoardSearchEntity> getBoardsSet(Set<String> ids,
            Map<String, BoardSearchEntity> boardInfoMap) {

        Set<BoardSearchEntity> boardEntities = new HashSet<BoardSearchEntity>();
        if (ids == null) {
            return boardEntities;
        }
        for (String id : ids) {
            BoardSearchEntity boardEntity = boardInfoMap.get(id);
            if (boardEntity != null) {
                boardEntities.add(boardEntity);
            }
        }
        return boardEntities;
    }

    private Map<String, BoardSearchEntity> toBoardSearchEntityMap(
            Map<String, BoardBasicInfo> boardInfoMap) {

        Map<String, BoardSearchEntity> boardSearchEntityMap = new HashMap<String, BoardSearchEntity>();
        for (Entry<String, BoardBasicInfo> entry : boardInfoMap.entrySet()) {
            BoardSearchEntity searchEntity = new BoardSearchEntity(entry.getValue().name,
                    entry.getValue().id, entry.getValue().type);
            searchEntity.code = entry.getValue().code;
            searchEntity.grades = entry.getValue().grades;
            boardSearchEntityMap.put(entry.getKey(), searchEntity);
        }
        return boardSearchEntityMap;
    }

    private Map<String, BoardBasicInfo> fetchBoardSearchEntityLocalMap(
            Map<String, BoardBasicInfo> boardInfoMap) {

        Map<String, BoardBasicInfo> boardSearchEntityMap = new HashMap<String, BoardBasicInfo>();
        if (boards != null && boardInfoMap != null) {
            for (BoardSearchEntity entity : boards) {
                boardSearchEntityMap.put(entity.id, boardInfoMap.get(entity.id));
            }
        }
        return boardSearchEntityMap;
    }

    @Override
    public String toString() {

        return " [boardTree=" + boardTree + ", boards=" + boards + ", targets=" + targets
                + ", contentSrc=" + contentSrc + ", tags=" + tags + ", scope=" + scope
                + ", avgRating=" + avgRating + ", views=" + views + ", followers=" + followers
                + ", comments=" + comments + ", upVotes=" + upVotes + ", toString()="
                + super.toString() + "]";
    }

}
