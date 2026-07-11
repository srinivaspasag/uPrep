package com.lms.pojos.news.details;

import com.lms.board.pojos.test.BoardTreeRes;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;

import java.util.List;
import java.util.Set;

public class AbstractSocialEntity extends SrcEntity {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int upVotes = 0;          // total no of upVotes
    public int views = 0;
    public int followers = 0;
    public int comments = 0;
    public boolean voted = false;
    public double avgRating;
    public Set<String> tags;
    public SrcEntity contentSrc;
    public long timeCreated;
    public List<BoardTreeRes> boardTree;
    public String thumbnail;
    public String description;
    public boolean attempted = false;

    public AbstractSocialEntity() {

    }

    public AbstractSocialEntity(EntityType type, String id) {

        super(type, id);
    }
}
