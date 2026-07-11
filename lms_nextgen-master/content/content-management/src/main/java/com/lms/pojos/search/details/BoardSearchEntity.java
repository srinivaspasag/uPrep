package com.lms.pojos.search.details;

import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.BoardType;
import com.lms.common.vedantu.enums.GradeType;
import com.lms.common.vedantu.event.api.JSONAware;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
@Setter
@Getter
public class BoardSearchEntity implements JSONAware {

    public String			name;
    public String			code;
    public String			id;
    public BoardType		type;
    public Set<GradeType>	grades;

    public BoardSearchEntity() {

    }

    public BoardSearchEntity(String name, String id, BoardType type) {
        this.name = name;
        this.code = name;
        this.id = id;
        this.type = type;
    }

    public void _setType(String type) {
        this.type = BoardType.valueOfKey(type.toUpperCase());
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.NAME, name);
        json.put(ConstantsGlobal.CODE, code);
        json.put(ConstantsGlobal.ID, id);
        if (type != null) {
            json.put(ConstantsGlobal.TYPE, type.name());
        }
        if (grades != null) {
            json.put(ConstantsGlobal.GRADES, grades);
        }
        return json;
    }


    @Override
    public void fromJSON(JSONObject json) {
        name = JSONUtils.getString(json, ConstantsGlobal.NAME);
        code = JSONUtils.getString(json, ConstantsGlobal.CODE);
        id = JSONUtils.getString(json, ConstantsGlobal.ID);
        type = BoardType.valueOfKey(JSONUtils.getString(json,
                ConstantsGlobal.TYPE));
        grades = new HashSet<GradeType>();
        Set<String> jGrades = JSONUtils.getSet(json, ConstantsGlobal.GRADES);

        for (String grade : jGrades) {
            GradeType gType = GradeType.valueOfKey(grade);
            grades.add(gType);
        }
    }

    public void fromBoardBasicInfo(BoardBasicInfo info) {
        name = info.name;
        code = info.code;
        id = info.id;
        type = info.type;
        grades = info.grades;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BoardSearchEntity [name:").append(name)
                .append(", code:").append(code).append(", id:").append(id)
                .append(", type:").append(type).append("]");
        return builder.toString();
    }
}
