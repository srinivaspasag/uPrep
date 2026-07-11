package com.lms.component;

import com.lms.common.utils.FileUtils;
import com.lms.common.vedantu.Repo.EntityOperationStatusRepo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.OperationType;
import com.lms.common.vedantu.mongo.EntityOperationStatus;
import com.lms.managers.AbstractContentManager;
import com.lms.models.CMDSDocument;
import com.lms.models.events.searchdetails.DocumentEncodingDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CmdsDocumentManager extends AbstractContentManager
{
    @Autowired
    private EntityOperationStatusRepo entityOperationStatusRepo;


    public void startProcessingUploadDoc(CMDSDocument document,
                                          Map<OperationType, String> operationJobIdMap) {

        EntityOperationStatus status = new EntityOperationStatus();
        status.oType = OperationType.DOCUMENT_CONVERSION;
        status.id = document._getStringId();
        status.type = EntityType.CMDSDOCUMENT;

        DocumentEncodingDetails details = new DocumentEncodingDetails();
        details.docId = document._getStringId();
        if (!(FileUtils.getExtensionWithoutDOT(document.originalFileName)
                .equalsIgnoreCase(FileUtils.PDF_EXTENSTION_WITHOUT_DOT))) {
            details.convertToPDF = true;

        }
        details.generateLinearizedPDF = true;
        details.generateThumbnail = true;
        details.encryptIfNeeded = details.generateLinearizedPDF;

        status.numOfSteps += details.encryptIfNeeded ? 1 : 0;
        status.numOfSteps += details.generateLinearizedPDF ? 1 : 0;
        status.numOfSteps += details.generateThumbnail ? 1 : 0;
        status.numOfSteps += details.convertToPDF ? 1 : 0;

        if (status.numOfSteps > 0) {
            entityOperationStatusRepo.save(status);
            details.jobId = status._getStringId();
            generateEventAysc(document.userId, details, EventType.CONVERT_DOCUMENT);
            operationJobIdMap.put(status.oType, status._getStringId());
        }
    }

}
