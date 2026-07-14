package com.vedantu.comm.news.details;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.vedantu.comm.managers.news.populators.EntityNewsDetailsFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.utils.ISocialEntity;
import com.vedantu.content.pojos.Source;
import com.vedantu.content.search.details.boards.BoardSearchEntity;

/**
 * Decorated StatusFeedNewsDetails News Entity Details
 * 
 * @author vikram
 * 
 */
public class StatusFeedNewsEntityDetails extends AbstractSocialEntity implements ISocialEntity {

    /**
	 * 
	 */
    private static final long     serialVersionUID = 1L;
    public String                 userId;
    public String                 statusMessage;
    public Source                 sourceContent;

    static {
        EntityNewsDetailsFactory.INSTANCE.register(EntityType.STATUSFEED,
                StatusFeedNewsEntityDetails.class);
    }

    public Set<BoardSearchEntity> boards;

    public StatusFeedNewsEntityDetails() {

    }

    public StatusFeedNewsEntityDetails(String statusFeedId) {

        super(EntityType.STATUSFEED, statusFeedId);

    }

    @Override
    public int hashCode() {

        return (EntityType.STATUSFEED + "_" + id).hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof StatusFeedNewsEntityDetails)) {
            return false;
        }
        StatusFeedNewsEntityDetails e = (StatusFeedNewsEntityDetails) o;
        return super.equals(o) && StringUtils.equals(id, e.id);
    }

    @Override
    public String _getEntityId() {

        // TODO Auto-generated method stub
        return id;
    }

    @Override
    public void _setVoted(boolean voted) {

        this.voted = voted;
    }

    @Override
    public void _setFollowType(FollowType followType) {

    }

}
