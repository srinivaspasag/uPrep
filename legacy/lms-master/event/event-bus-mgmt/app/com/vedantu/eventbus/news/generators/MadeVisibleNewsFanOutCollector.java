package com.vedantu.eventbus.news.generators;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.MongoManager;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgSection;

public class MadeVisibleNewsFanOutCollector extends AbstractSocialNewsFanOutCollector {

    private static final ALogger                 LOGGER   = Logger.of(MadeVisibleNewsFanOutCollector.class);
    public static MadeVisibleNewsFanOutCollector INSTANCE = new MadeVisibleNewsFanOutCollector();

    private MadeVisibleNewsFanOutCollector() {

    }

    @Override
    public void getNewsSubscribers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        super.getNewsSubscribers(activity, subscribers, newsUpdateToDeduplicate);
        // MadeVisibleNewsInfo info = (MadeVisibleNewsInfo) activity.info;

        if (CollectionUtils.isNotEmpty(activity.sharedWith)) {
            for (SrcEntity sharedWithEntity : activity.sharedWith) {
                if (sharedWithEntity.type != EntityType.SECTION) {
                    continue;
                }
                OrgSection section = OrgSectionDAO.INSTANCE.getById(sharedWithEntity.id);
                MutableLong hits = new MutableLong();
                //TODO instead use fetch
                List<OrgMember> orgMembers = OrgMemberDAO.INSTANCE.getOrgMembers(section.orgId,
                        null, null, section.programId, section.centerId, section._getStringId(),
                        null, null, MongoManager.NO_START, MongoManager.NO_LIMIT, null, null, hits);

                Set<SrcEntity> memberSrcEntitySet = subscribers
                        .get(NotificationReason.SHARED_WITH_SECTION);
                if (CollectionUtils.isEmpty(memberSrcEntitySet)) {
                    LOGGER.debug("Collecting users ");
                    memberSrcEntitySet = new HashSet<SrcEntity>();
                    for (OrgMember orgMember : orgMembers) {
                        accumulateFanOutEntity(new SrcEntity(EntityType.USER, orgMember.userId),
                                NotificationReason.SHARED_WITH_SECTION, subscribers,
                                newsUpdateToDeduplicate, activity.actor);
                    }

                }
            }
        }
    }
}
