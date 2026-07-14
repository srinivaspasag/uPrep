package com.vedantu.eventbus.processors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.CmdsContentLinkDAO;
import com.vedantu.cmds.enums.CmdsContentLinkType;
import com.vedantu.cmds.mgmt.publishers.EntityTypePublisherFactory;
import com.vedantu.cmds.mgmt.publishers.IPublisher;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.models.event.details.EntityPublishingDetails;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.AbstractContentModel;
import com.vedantu.eventbus.processors.cmds.EntityPublishingProcessor;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgSectionDAO;

public class UpdateLibraryProcessor implements IProcessor {

    private static final ALogger LOGGER = Logger.of(EntityPublishingProcessor.class);

    @Override
    public Status process(IConsumable consumable) {

        if (consumable == null || !(consumable instanceof IConsumable)) {
            LOGGER.debug(" Invalid event " + consumable);
            return Status.FAILURE;
        }

        Event event = (Event) consumable;

        IEventDetails details = event.fetchEventDetails();
        Logger.info("fetched eventDetails " + details);

        if (details == null || !(details instanceof EntityPublishingDetails)) {
            LOGGER.debug(" Invalid details " + consumable + " not of type "
                    + EntityPublishingDetails.class);
            return Status.FAILURE;
        }

        EntityPublishingDetails publishableEntityDetails = (EntityPublishingDetails) details;
        final IPublisher publisher = EntityTypePublisherFactory.INSTANCE
                .get(publishableEntityDetails.content.type);

        if (publisher == null) {
            LOGGER.debug(" Invalid publisher for type " + publishableEntityDetails.content.type);
            return Status.FAILURE;

        }
        List<CMDSContentLink> links = CmdsContentLinkDAO.INSTANCE.getCmdsContentLinks(
                publishableEntityDetails.content, new SrcEntity(EntityType.SECTION, null),
                CmdsContentLinkType.ADDED, null, Scope.UNKNOWN, MongoManager.NO_START,
                MongoManager.NO_LIMIT, VedantuRecordState.ACTIVE, new MutableLong());

        List<String> sectionIds = new ArrayList<String>();
        for (CMDSContentLink link : links) {
            sectionIds.add(link.target.id);
        }
        LOGGER.debug("Updating sections"+ sectionIds);
        VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> vedantuBasicDAO = EntityTypeDAOFactory.INSTANCE
                .get(publishableEntityDetails.content.type);
        if (vedantuBasicDAO != null) {
            AbstractContentModel model = (AbstractContentModel) vedantuBasicDAO
                    .getById(publishableEntityDetails.content.id);
            if (model != null) {
                
                OrgSectionDAO.INSTANCE.addSize(sectionIds, false, model.getExportableSize());
            }
        }

        return Status.SUCCESS;
    }
}
