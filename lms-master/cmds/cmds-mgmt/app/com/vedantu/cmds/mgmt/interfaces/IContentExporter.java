package com.vedantu.cmds.mgmt.interfaces;

import com.vedantu.cmds.managers.ExportRecordManager;
import com.vedantu.cmds.pojos.export.EntityExportRecord;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.exceptions.OperationAbortedException;
import com.vedantu.commons.pojos.SrcEntity;

public interface IContentExporter {

    boolean export(ExportRecordManager manager, EntityExportRecord record, SrcEntity target)
            throws ExportException, OperationAbortedException;
}
