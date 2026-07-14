package com.vedantu.eventbus.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CmdsContentDAO;
import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.daos.SDCardGroupDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.managers.SDCardManager;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.SDCard;
import com.vedantu.cmds.models.SDCardGroup;
import com.vedantu.cmds.models.event.details.ExportDetails;
import com.vedantu.cmds.models.event.details.SDCardDetails;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.content.interfaces.IContentManager;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.entity.factory.EntityTypeContentManagerFactory;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.models.mongo.EntityOperationStatus;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.SortOrderInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class SDCardSplitter implements IProcessor {

    private static final ALogger LOGGER = Logger.of(SDCardSplitter.class);

    @Override
    public Status process(IConsumable consumable) {

        if (consumable == null || !(consumable instanceof IConsumable)) {
            LOGGER.debug(" Invalid event " + consumable);
            return Status.FAILURE;
        }

        Event event = (Event) consumable;

        IEventDetails details = event.fetchEventDetails();
        Logger.info("fetched eventDetails " + details);

        if (details == null || !(details instanceof SDCardDetails)) {
            LOGGER.debug(" Invalid details " + consumable + " not of type " + ExportDetails.class);
            return Status.FAILURE;
        }

        SDCardDetails sdCardDetails = (SDCardDetails) details;

        if (StringUtils.isEmpty(sdCardDetails.groupId)) {
            LOGGER.error("gruop Id not present");
            return Status.FAILURE;
        }
        try {
            createSDCards(sdCardDetails.groupId);
        } catch (VedantuException vedantuException) {
            LOGGER.error("Can not proceed further", vedantuException);
        }

        return Status.SUCCESS;
    }

    private Status createSDCards(String groupId) throws VedantuException {

        SDCardGroup group = SDCardGroupDAO.INSTANCE.getById(groupId, VedantuRecordState.ACTIVE);
        if (group == null || (StringUtils.isEmpty(group.jobId))) {
            LOGGER.error("Job  not present");
            return Status.FAILURE;
        }
        EntityOperationStatus status = EntityOperationStatusDAO.INSTANCE.getById(group.jobId);
        if (status == null) {
            LOGGER.error("Job  not present");
            return Status.FAILURE;

        }

        MutableLong totalHits = new MutableLong();

        Set<SortOrderInfo> orders = new HashSet<SortOrderInfo>();
        orders.add(new SortOrderInfo(SortOrder.ASC, CMDSContentLink.POSITION));

        MutableLong orgScopedTotalContents = new MutableLong();
        CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(null, group.target,
                CmdsContentLinkType.ADDED, StringUtils.EMPTY, Scope.ORG, 0, 1,
                VedantuRecordState.ACTIVE, orgScopedTotalContents, orders);

        MutableLong totalActiveContent = new MutableLong();
        CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(null, group.target,
                CmdsContentLinkType.ADDED, StringUtils.EMPTY, Scope.UNKNOWN, 0, 1,
                VedantuRecordState.ACTIVE, totalActiveContent, orders);
        LOGGER.debug("Total content " + totalActiveContent + " orgscoped content "
                + orgScopedTotalContents);
        if (orgScopedTotalContents.longValue() < totalActiveContent.longValue()) {
            throw new VedantuException(VedantuErrorCode.NOT_PUBLISHED, "All content not published");
        }

        List<String> cards = new ArrayList<String>();
        List<CMDSContentLink> links = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(null,
                group.target, CmdsContentLinkType.ADDED, StringUtils.EMPTY, Scope.ORG,
                MongoManager.NO_START, MongoManager.NO_LIMIT, VedantuRecordState.ACTIVE, totalHits,
                orders);

        SDCardManager sdCardManager = new SDCardManager();
        sdCardManager.initCard(group);

        for (CMDSContentLink link : links) {

            VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE.get(link.source.type);

            AbstractContentModel publishedModel;
            AbstractContentModel cmdsModel;
            try {

                cmdsModel = (AbstractContentModel) ((CmdsContentDAO<?, ?>) dao)
                        .getById(link.source.id);

                if (cmdsModel == null) {
                    return Status.FAILURE;
                }

                if (!cmdsModel.size.isFinalized()) {

                    IContentManager contentManager = EntityTypeContentManagerFactory.INSTANCE
                            .get(link.source.type);
                    LOGGER.debug("Size is not calculated so calcualting now");
                    if (contentManager == null) {
                        return Status.FAILURE;
                    }
                    contentManager.calculate(link.source.id,false, (VedantuBaseMongoModel[]) null);
                }

                publishedModel = (AbstractContentModel) ((CmdsContentDAO<?, ?>) dao)
                        .getPublishedEntity(link.source.id);

                if (publishedModel == null) {
                    return Status.FAILURE;
                }
                if (sdCardManager.canAdd(publishedModel.getExportableSize())) { // can not add to
                                                                                // new card
                    sdCardManager.add(link.source, publishedModel);

                } else {
                    SDCard lastCard = sdCardManager.confirm(); // confirm last card

                    cards.add(lastCard._getStringId());

                    // initiate new SD card;
                    sdCardManager.initCard(group);
                    if (sdCardManager.canAdd(publishedModel.getExportableSize())) {

                        sdCardManager.add(link.source, publishedModel);
                    } else {
                        LOGGER.error("Can not add content as specified SD crds are smaller than content:  "
                                + publishedModel + " " + group.cardSize);
                    }
                }
                LOGGER.debug("Increment job now");
                EntityOperationStatusDAO.INSTANCE.incCompletion(group.jobId);

            } catch (VedantuException e) {
                LOGGER.error("Failed to create SD Card ", e);
            }

        }
        if (sdCardManager.getCard() != null) {
            SDCard lastCard = sdCardManager.confirm();
            cards.add(lastCard._getStringId());
        }
        group.cards.addAll(cards);
        group.completed = true;
        SDCardGroupDAO.INSTANCE.updateModel(group,
                Arrays.asList(SDCardGroup.SIZE, SDCardGroup.CARDS, SDCardGroup.COMPLETED));
        return Status.SUCCESS;
    }
}
