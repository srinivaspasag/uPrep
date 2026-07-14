package com.lms.board.pojos;

import com.lms.board.enums.BoardContextType;
import com.lms.board.model.Board;
import com.lms.common.utils.VedantuStringUtils;
import com.lms.common.vedantu.enums.BoardType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class BoardNode {

    public int rowNum;
    public int colNum;
    public BoardContextType context;
    public BoardType type;
    public String name;
    public String code;
    public String newName;
    public Set<String> addAliases = new HashSet<String>();
    public Set<String> removeAliases = new HashSet<String>();
    public Set<String> sameAsBoardNames = new HashSet<String>();
    public Set<String> similarToBoardNames = new HashSet<String>();
    public Set<BoardNode> children = new HashSet<BoardNode>();
    public BoardNode parent;

    public String brdId;

    // applicable only for ORG, CONSUMER
    // for ORG --> parent CONSUMER boardName
    // for CONSUMER --> parent ORG boardName
    public String parentContextBoardName;

    public BoardNode(int rowNum, int colNum, String cellValue,
                     BoardContextType context) {

        this.rowNum = rowNum;
        this.colNum = colNum;

        this.context = context;
        if (BoardContextType.CONSUMER == context
                || BoardContextType.ORG == context) {
            switch (colNum) {
                case 1:
                    type = BoardType.COURSE;
                    break;
                case 2:
                    type = BoardType.TOPIC;
                    break;
                case 3:
                    type = BoardType.SUBTOPIC;
                    break;
                default:
                    break;
            }
        }

        String[] tokens = StringUtils.split(cellValue.trim(), "\n");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (0 == i) {
                this.name = token;
            } else {
                char firstChar = token.charAt(0);
                String tokenWithoutChar = token.substring(1);
                switch (firstChar) {
                    case '*':
                        setNewName(tokenWithoutChar);
                        break;
                    case '+':
                        addAlias(tokenWithoutChar);
                        break;
                    case '-':
                        removeAlias(tokenWithoutChar);
                        break;
                    case '=':
                        addSameAsBoardName(tokenWithoutChar);
                        break;
                    case '~':
                        addSimilarToBoardName(tokenWithoutChar);
                        break;
                    case '@':
                        setParentContextBoardName(tokenWithoutChar);
                        break;
                    case '#':
                        setCode(tokenWithoutChar);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public boolean hasName() {
        return !StringUtils.isEmpty(name);
    }

    public void addAlias(String alias) {
        addAliases.add(alias.trim());
    }

    public void removeAlias(String alias) {
        removeAliases.add(alias.trim());
    }

    public void addSameAsBoardName(String sameAsBoardName) {
        sameAsBoardNames.add(sameAsBoardName.trim());
    }

    public void addSimilarToBoardName(String similarToBoardName) {
        similarToBoardNames.add(similarToBoardName.trim());
    }

    public void setParentContextBoardName(String parentContextBoardName) {
        this.parentContextBoardName = parentContextBoardName.trim();
    }

    public void setCode(String code) {
        this.code = code.trim();
    }

    public void addChild(BoardNode child) {
        children.add(child);
    }

    public void setNewName(String newName) {
        this.newName = newName.trim();
    }

    public void setParent(BoardNode parent) {
        this.parent = parent;
    }

    public String getCanonicalName() {
        return VedantuStringUtils.toCanonicalName(name);
    }

    @Override
    public String toString() {
        return "[board = " + name + "]";
    }

    public String toTreeAsString(int level, String spacePrefix, String prefix) {
        StringBuilder sb = new StringBuilder("\n");
        if (level > 0) {
            sb.append(spacePrefix.repeat(level));
        }
        sb.append(prefix).append(name);
        for (BoardNode child : children) {
            sb.append(child.toTreeAsString(level + 1, spacePrefix, prefix));
        }
        return sb.toString();
    }


    public boolean populate(Board board) {
        boolean isModified = false;
        if (!StringUtils.isEmpty(newName)) {
            board.setName(newName);
            isModified = true;
        }
        if (CollectionUtils.isNotEmpty(addAliases)) {
            board.addAliases(addAliases);
            isModified = true;
        }
        if (CollectionUtils.isNotEmpty(removeAliases)) {
            board.removeAliases(removeAliases);
            isModified = true;
        }
        return isModified;
    }
}
