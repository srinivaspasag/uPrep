package com.vedantu.content.models;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.Difficulty;

public abstract class AbstractBoardEntityTagModel extends AbstractContentModel {

    @Transient
    public static final String BOARD_IDS  = "boardIds";
    @Transient
    public static final String TARGET_IDS = "targetIds";
    @Transient
    public static final String DIFFICULTY = "difficulty";
    @Transient
    public static final String TAGS       = "tags";
    @Transient
    public static final String COMPLETED  = "completed";
  

    public Set<String>         boardIds;
    public Set<String>         targetIds;
    public Difficulty          difficulty;

    @Indexed
    public SrcEntity           contentSrc;
    public Set<String>         tags;
    public boolean             completed;
    

    public AbstractBoardEntityTagModel() {

        super();
        this.boardIds = new HashSet<String>();
        this.targetIds = new HashSet<String>();
        this.difficulty = Difficulty.UNKNOWN;
        this.tags = new HashSet<String>();
    }

    public void addBoard(String boardId) {

        if (StringUtils.isEmpty(boardId)) {
            return;
        }
        if (boardIds == null) {
            boardIds = new HashSet<String>();
        }
        boardIds.add(boardId);
    }

    public void addBoards(Collection<String> boardIds) {

        if (CollectionUtils.isEmpty(boardIds)) {
            return;
        }
        if (this.boardIds == null) {
            this.boardIds = new HashSet<String>();
        }

        this.boardIds.addAll(boardIds);
    }

    public void addTarget(String targetId) {

        if (StringUtils.isEmpty(targetId)) {
            return;
        }
        if (targetIds == null) {
            targetIds = new HashSet<String>();
        }
        targetIds.add(targetId);
    }

    public void addTargets(Collection<String> targetIds) {

        if (CollectionUtils.isEmpty(targetIds)) {
            return;
        }
        if (this.targetIds == null) {
            this.targetIds = new HashSet<String>();
        }
        this.targetIds.addAll(targetIds);
    }

    public void addTags(Collection<String> tags) {

        if (tags == null) {
            return;
        }
        if (this.tags == null) {
            this.tags = new HashSet<String>();
        }
        this.tags.addAll(tags);
    }

    public Set<String> __getAllBoardIds() {

        Set<String> brdIds = new HashSet<String>();
        if (boardIds != null) {
            brdIds.addAll(boardIds);
        }
        if (targetIds != null) {
            brdIds.addAll(targetIds);
        }
        return brdIds;
    }

    public String _getUserId() {

        return userId;
    }

}
