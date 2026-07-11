package com.vedantu.eventbus.processors.cmds;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.daos.ExportRecordDAO;
import com.vedantu.cmds.managers.ExportRecordManager;
import com.vedantu.cmds.models.ExportRecord;
import com.vedantu.cmds.models.event.details.ExportDetails;
import com.vedantu.commons.daos.EntityOperationStatusDAO;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.exceptions.OperationAbortedException;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.mongo.VedantuRecordState;

public class ExportProcessor extends ChainedProcessors implements IProcessor {

    private static final ALogger LOGGER = Logger.of(ExportProcessor.class);

    public ExportProcessor() {

    }

    @Override
    public Status process(IConsumable consumable) {

        if (consumable == null || !(consumable instanceof IConsumable)) {
            LOGGER.debug(" Invalid event " + consumable);
            return Status.FAILURE;
        }

        Event event = (Event) consumable;

        IEventDetails details = event.fetchEventDetails();
        Logger.info("fetched eventDetails " + details);

        if (details == null || !(details instanceof ExportDetails)) {
            LOGGER.debug(" Invalid details " + consumable + " not of type " + ExportDetails.class);
            return Status.FAILURE;
        }

        ExportDetails exportDetails = (ExportDetails) details;

        if (StringUtils.isEmpty(exportDetails.exportId)) {
            LOGGER.error("Export Id not present");
            return Status.FAILURE;
        }
        ExportRecord exportRecord = ExportRecordDAO.INSTANCE.getById(exportDetails.exportId,VedantuRecordState.ACTIVE);
        if (exportRecord == null|| (StringUtils.isEmpty(exportRecord.jobId))) {
            LOGGER.error("Job  not present");
            return Status.FAILURE;
        }

        boolean result = false;
        boolean cleanup = false;
        ExportRecordManager manager = null;
        try {
            EntityOperationStatusDAO.INSTANCE.updateException(exportRecord.jobId, null);
            manager = new ExportRecordManager(exportDetails.exportId);

            result = manager.export();
            cleanup = result;
            manager.cleanUp();

        } catch (ExportException e) {
            LOGGER.error("Export failed ",e);
            EntityOperationStatusDAO.INSTANCE.updateException(exportRecord.jobId, e);
        } catch (OperationAbortedException e) {
            LOGGER.error("Operation aborted",e);
            exportRecord = ExportRecordDAO.INSTANCE.getById(exportDetails.exportId);
            manager.remove(exportRecord);
        }
        if (event.nTries == 3) {
            cleanup = true;
        }
        if (cleanup) {
            if (manager != null) {
                manager.cleanUp();
            }
        }
        if (result) {

            return Status.SUCCESS;
        }

        return Status.FAILURE;

    }
}
