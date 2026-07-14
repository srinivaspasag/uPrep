package com.vedantu.comm.news.details;

import java.util.List;
import java.util.Set;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.BoardTreeRes;
import com.vedantu.commons.pojos.SrcEntity;

public class AbstractSocialEntity extends SrcEntity {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public AbstractSocialEntity() {

    }

    public AbstractSocialEntity(EntityType type, String id) {

        super(type, id);
    }

    public int                upVotes=0;          // total no of upVotes
    public int                views=0;
    public int                followers=0;
    public int                comments=0;
    public boolean            voted=false;

    public double             avgRating;
    public Set<String>        tags;
    public SrcEntity          contentSrc;
    public long               timeCreated;

    public List<BoardTreeRes> boardTree;
    public String             thumbnail;
    public String             description;
    public boolean            attempted = false;
}
