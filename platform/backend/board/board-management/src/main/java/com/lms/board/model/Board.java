package com.lms.board.model;

import com.lms.board.enums.BoardContextType;
import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.common.utils.VedantuStringUtils;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.BoardType;
import com.lms.common.vedantu.enums.GradeType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(value = "boards")
@CompoundIndexes({ @CompoundIndex(name = "cName, ownerId, treeName, context", unique = true),
        @CompoundIndex(name = "cAliases, ownerId, treeName, context", unique = true),
        @CompoundIndex(name = "ownerId, parentBrdIds, type, context"),
        @CompoundIndex(name = "type, parentBrdIds, recordState")
})
@Setter
@Getter
public class Board extends VedantuBaseMongoModel {
    public static final String   OWNER_SYSTEM = "SYSTEM";
    public static final String   TREE_SYSTEM  = "SYSTEM";

    public String           name;
    public String           code;
    // Canonical name
    private String          cName;
    private List<String> aliases       = new ArrayList<String>();
    // Canonical aliases
    private List<String>    cAliases      = new ArrayList<String>();

    // should be specified for ORG, CONSUMER
    public String           treeName      = TREE_SYSTEM;
    public BoardContextType context;
    public BoardType type;

    public String           ownerId       = OWNER_SYSTEM;

    public Set<GradeType> grades;

    // applicable only in case of ORG
    public String           consumerBrdId;
    // applicable only in case of ORG, CONSUMER
    public String           globalBrdId;
    // ORG will have just one
    public List<String>     parentBrdIds  = new ArrayList<String>();

    public Set<String>      sameBrdIds    = new HashSet<String>();
    public Set<String>      similarBrdIds = new HashSet<String>();
    public String           year;

    public Board() {

    }

    public Board(String name, String code, String ownerId, BoardContextType contextType,
                 BoardType type, String treeName, Set<GradeType> grades) {

        setName(name);
        this.code = (code==null)?name:code;
        this.ownerId = ownerId;
        this.context = contextType;
        this.type = type;

        if (BoardContextType.isTreeNameNeeded(context)) {
            this.treeName = treeName;
        }
        if (BoardContextType.isGradeNeeded(context)) {
            this.grades = grades;
        }
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
        this.cName = VedantuStringUtils.toCanonicalName(name);
        addAlias(name);
    }

    public String getCName() {

        return cName;
    }

    public List<String> getAliases() {

        return aliases;
    }

    public void setAliases(List<String> aliases) {

        this.aliases = aliases;
    }

    public void addAlias(String alias) {

        if (alias.isEmpty()) {
            return;
        }
        String cAlias = VedantuStringUtils.toCanonicalName(alias);
        if (!cAliases.contains(cAlias)) {
            aliases.add(alias);
            cAliases.add(cAlias);
        }
    }

    public void addAliases(Collection<String> aliases) {

        for (String alias : aliases) {
            addAlias(alias);
        }
    }

    public void removeAlias(String alias) {

        if (alias.isEmpty()) {
            return;
        }
        String cAlias = VedantuStringUtils.toCanonicalName(alias);
        if (cName.equals(cAlias)) {
            return;
        }
        int index = cAliases.indexOf(cAlias);
        if (index >= 0) {
            aliases.remove(index);
            cAliases.remove(index);
        }
    }

    public void removeAliases(Collection<String> aliases) {

        for (String alias : aliases) {
            removeAlias(alias);
        }
    }

    public List<String> getCAliases() {

        return cAliases;
    }

    public void setCAliases(List<String> cAliases) {

        this.cAliases = cAliases;
    }

    public void addParent(String parentBrdId) {

        switch (context) {
            case ORG:
            case CONSUMER:
                if (!parentBrdIds.isEmpty()) {
                    parentBrdIds.set(0, parentBrdId);
                } else {
                    parentBrdIds.add(parentBrdId);
                }
                break;

            default:
                if (!parentBrdIds.contains(parentBrdId)) {
                    parentBrdIds.add(parentBrdId);
                }
                break;
        }
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        BoardBasicInfo boardBasicInfo = new BoardBasicInfo(_getStringId(), recordState, name, code,
                type, treeName, grades, parentBrdIds, year);
        return boardBasicInfo;
    }

    @Override
    public String toString() {

        return "Board [name=" + name + ", code=" + code + ", cName=" + cName + ", aliases="
                + aliases + ", cAliases=" + cAliases + ", treeName=" + treeName + ", context="
                + context + ", type=" + type + ", ownerId=" + ownerId + ", grades=" + grades
                + ", consumerBrdId=" + consumerBrdId + ", globalBrdId=" + globalBrdId
                + ", parentBrdIds=" + parentBrdIds + ", sameBrdIds=" + sameBrdIds
                + ", similarBrdIds=" + similarBrdIds + "]";
    }

}

