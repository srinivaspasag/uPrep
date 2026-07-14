package com.vedantu.eventbus.news.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.comm.managers.news.generator.INewsFanOutCollector;
import com.vedantu.commons.ShareWithEntity;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.models.OrgProgram;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.pojos.OrgProgramCenterSections;

public abstract class AbstractNewsFanOutCollector implements INewsFanOutCollector {

    private static final ALogger LOGGER = Logger.of(AbstractNewsFanOutCollector.class);

    @Override
    public void getNewsSubscribers(NewsActivity activity,
            Map<NotificationReason, Set<SrcEntity>> subscribers,
            Set<SrcEntity> newsUpdateToDeduplicate) {

        if (null != activity.srcOwner && !activity.srcOwner.equals(activity.actor)) {
            accumulateFanOutEntity(activity.srcOwner, NotificationReason.OWNER, subscribers,
                    newsUpdateToDeduplicate, activity.actor);
        }

        if (null != activity.src && !activity.src.equals(activity.actor)) {
            accumulateFanOutEntity(activity.src, NotificationReason.SOURCE, subscribers,
                    newsUpdateToDeduplicate, activity.actor);
        }

        // getRowId

        if (CollectionUtils.isNotEmpty(activity.sharedWith)) {

            for (SrcEntity e : activity.sharedWith) {
                // accumulateFanOutEntity(getAllFollowers(e),
                // ActivityReason.FOLLOWING_SHARED_WITH, newsUpdateTo,
                // newsUpdateToDeduplicate, newsActivity.actor);
                LOGGER.debug(activity.src.id + " SharedWith: " + e.id + " " + e.type);

                // open lower one
                Set<SrcEntity> entityList = getActivitySubscribers(activity.actor.id, activity.src,
                        e);
                if (CollectionUtils.isNotEmpty(entityList)) {
                    for (SrcEntity entity : entityList) {
                        LOGGER.debug(activity.src.id + " SharedWith: " + entity.id + " "
                                + entity.type + "with respect to original entity type " + e.type);

                        accumulateFanOutEntity(entity,
                                NotificationReason.getSharedWithActivityReason(e.type),
                                subscribers, newsUpdateToDeduplicate, activity.actor);
                    }
                }
            }
        }
    }

    protected void accumulateFanOutEntity(Collection<SrcEntity> entities,
            NotificationReason reason, Map<NotificationReason, Set<SrcEntity>> newsUpdateTo,
            Set<SrcEntity> newsUpdateToDeduplicate, SrcEntity excludedActorEntity) {

        if (null == entities) {
            return;
        }
        for (SrcEntity entity : entities) {
            accumulateFanOutEntity(entity, reason, newsUpdateTo, newsUpdateToDeduplicate,
                    excludedActorEntity);
        }
    }

    protected void accumulateFanOutEntity(SrcEntity entity, NotificationReason reason,
            Map<NotificationReason, Set<SrcEntity>> newsUpdateTo,
            Set<SrcEntity> newsUpdateToDeduplicate, SrcEntity excludedActorEntity) {

        if (null == entity || (null != excludedActorEntity && excludedActorEntity.equals(entity))
                || newsUpdateToDeduplicate.contains(entity)) {
            return;
        }
        if (!newsUpdateTo.containsKey(reason)) {
            newsUpdateTo.put(reason, new HashSet<SrcEntity>());
        }
        LOGGER.debug("Reason:" + reason + " SrcEntity: " + entity);

        newsUpdateTo.get(reason).add(entity);
        newsUpdateToDeduplicate.add(entity);
    }

    private Set<SrcEntity> getActivitySubscribers(String userId, SrcEntity srcEntity,
            SrcEntity sharedWithEntity) {

        Set<SrcEntity> activityListeners = new HashSet<SrcEntity>();
        // get list of user permissions from share
        // if says programme then getSharableProgrammeInfoForEntityin
        if (sharedWithEntity instanceof ShareWithEntity) {
            ShareWithEntity beneficior = (ShareWithEntity) sharedWithEntity;
            if (beneficior.type == EntityType.USER) {
                activityListeners.add(new SrcEntity(beneficior.type, beneficior.id));
            } else if (beneficior.type == EntityType.PROGRAM) {
                OrgProgram program = OrgProgramDAO.INSTANCE.getById(beneficior.id);
                List<SrcEntity> centers = null;
                if (CollectionUtils.isNotEmpty(beneficior.centers)) {
                    centers = beneficior.centers;
                } else if (CollectionUtils.isNotEmpty(program.centersSections)) {
                    if (centers == null) {
                        centers = new ArrayList<SrcEntity>();
                    }
                    for (OrgProgramCenterSections center : program.centersSections) {
                        centers.add(new SrcEntity(EntityType.CENTER, center.centerId));
                    }
                }
                if (CollectionUtils.isNotEmpty(centers)) {
                    for (SrcEntity center : beneficior.centers) {
                        List<OrgSection> sections = OrgSectionDAO.INSTANCE.getSectionsByOrgIds(
                                program.orgId, program._getStringId(), center.id);
                        for (OrgSection section : sections) {

                            activityListeners.add(new SrcEntity(EntityType.SECTION, section
                                    ._getStringId()));
                        }
                        activityListeners.add(new SrcEntity(EntityType.CENTER, program
                                ._getStringId() + "#" + center.id));
                    }
                }

                activityListeners.add(new SrcEntity(EntityType.PROGRAM, program._getStringId()));
                activityListeners.add(new SrcEntity(EntityType.ORGANIZATION, program.orgId));

            } else if (beneficior.type == EntityType.SECTION) {

                OrgSection section = OrgSectionDAO.INSTANCE.getById(beneficior.id);

                activityListeners.add(new SrcEntity(EntityType.PROGRAM, section.programId));
                activityListeners.add(new SrcEntity(EntityType.ORGANIZATION, section.orgId));

                activityListeners.add(new SrcEntity(EntityType.SECTION, section._getStringId()));
                activityListeners.add(new SrcEntity(EntityType.CENTER, section.programId + "#"
                        + section.centerId));

            }

        }
        return activityListeners;

    }

}
