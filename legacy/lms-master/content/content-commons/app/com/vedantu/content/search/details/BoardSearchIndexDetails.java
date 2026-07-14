package com.vedantu.content.search.details;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.commons.enums.boards.GradeType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.search.details.boards.BoardSearchEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

public class BoardSearchIndexDetails extends AbstractSearchDetail {

    private static final ALogger  LOGGER = Logger.of(BoardSearchIndexDetails.class);
    public String                 name;
    public BoardType              type;
    public String                 desc;
    public String                 thumb;
    public String                 code;
    public String                 category;
    public Set<BoardSearchEntity> targetOf;                                         /*
                                                                                      * valid only
                                                                                      * for exam for
                                                                                      * all courses
                                                                                      * this is
                                                                                      * targertOf ,
                                                                                      * i . e name =
                                                                                      * IIT ,
                                                                                      * targetOfIds
                                                                                      * = Ids of {
                                                                                      * Physics ,
                                                                                      * Chemistry ,
                                                                                      * Mathmatics }
                                                                                      */
    public BoardSearchEntity      parent;
    public Set<GradeType>         grades;
    public Set<String>            tags;
    public Set<String>            links;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = super.toJSON();
        json.put(Constants.CATEGORY, category);
        json.put(ConstantsGlobal.CODE, code);
        json.put(ConstantsGlobal.DESC, desc);
        if (grades != null) {
            Set<String> grds = new HashSet<String>();
            for (GradeType grade : grades) {
                grds.add(grade.name());
            }
            json.put(Constants.GRADES, grades);
        }
        json.put(ConstantsGlobal.NAME, name);
        JSONUtils.addJSONAwareObject(Constants.PARENT, parent, json);
        JSONUtils.addStringCollection(ConstantsGlobal.TAGS, tags, json);

        if (targetOf != null) {
            JSONArray jsonArray = new JSONArray();
            for (BoardSearchEntity brd : targetOf) {
                jsonArray.put(brd.toJSON());
            }
            json.put(Constants.TARGET_OF, jsonArray);
        }
        json.put(ConstantsGlobal.THUMB, thumb);
        json.put(ConstantsGlobal.TYPE, type.name());
        JSONUtils.addStringCollection(Constants.LINKS, links, json);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        category = JSONUtils.getString(json, Constants.CATEGORY);
        code = JSONUtils.getString(json, ConstantsGlobal.CODE);
        desc = JSONUtils.getString(json, ConstantsGlobal.DESC);
        JSONArray jsArray = JSONUtils.getJSONArray(json, Constants.GRADES);
        if (grades == null) {
            grades = new HashSet<GradeType>();
        }
        if (jsArray != null) {
            for (int i = 0; i < jsArray.length(); i++) {
                try {
                    grades.add(GradeType.valueOf(jsArray.getString(i)));
                } catch (JSONException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        links = JSONUtils.getSet(json, Constants.LINKS);
        name = JSONUtils.getString(json, ConstantsGlobal.NAME);
        parent = new BoardSearchEntity();

        parent.fromJSON(JSONUtils.getJSONObject(json, Constants.PARENT));
        tags = JSONUtils.getSet(json, ConstantsGlobal.TAGS);
        // TODO: targetOf =
        // BoardUtil.getBoradSearchEntity(JSONUtil.getJSONArray(json,
        // Constants.TARGET_OF));
        thumb = JSONUtils.getString(json, ConstantsGlobal.THUMB);
        type = BoardType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.TYPE));
    }

    @Override
    public void fromMongoModel(VedantuBaseMongoModel mongoModel) {

        super.fromMongoModel(mongoModel);
        /*
         * Board board = (Board) mongoModel; brdId = board._getStringId(); category =
         * board.category; code = board.code; desc = board.desc; grades = board.grades; links =
         * board.links; name = board.name; Set<String> brdIds = new HashSet<String>(); if
         * (CollectionUtils.isNotEmpty(board.targetOfIds)) { brdIds.addAll(board.targetOfIds); } if
         * (StringUtils.isNotEmpty(board.parentId)) { brdIds.add(board.parentId); } Map<String,
         * BoardSearchEntity> brdEntityMap = null;// TODO: //
         * BoardUtil.getBoardSearchEntityMap(brdIds); parent = brdEntityMap.get(board.parentId);
         * userId = board.userId; tags = board.tags; // TODO: correct this one // targetOf =
         * BoardUtil.getBoardSearchEntityFromMap(board.targetOfIds, // brdEntityMap); thumb =
         * board.thumb; type = board.type;
         */
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return null;
    }

    // @Override
    // public NewsActivity toNewsActivity() {
    // return null;
    // }

    @Override
    public UniqueId _getUniqueId() {

        return new UniqueId(ConstantsGlobal.ID, id);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("BoardSearchIndexDetails [id:").append(id).append(", name:").append(name)
                .append(", type:").append(type).append(", desc:").append(desc).append(", thumb:")
                .append(thumb).append(", code:").append(code).append(", category:")
                .append(category).append(", targetOf:").append(targetOf).append(", parent:")
                .append(parent).append(", grades:").append(grades).append(", tags:").append(tags)
                .append(", links:").append(links).append("]");
        return builder.toString();
    }

    private static class Constants {

        static final String CATEGORY  = "category";
        static final String GRADES    = "grades";
        static final String PARENT    = "parent";
        static final String TARGET_OF = "targetOf";
        static final String LINKS     = "links";
    }

    @Override
    public boolean getNotificationEnabled() {

        return true;
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public boolean _isIndexable() {

        return StringUtils.isNotBlank(name);
    }

}
