package com.vedantu.cmds.managers;

import com.vedantu.cmds.pojos.requests.tests.UploadOfflineTestResultReq;
import com.vedantu.cmds.pojos.responses.tests.UploadOfflineTestResultRes;
import com.vedantu.cmds.utils.OfflineTestUtils;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.managers.AbstractContentManager;

public class OfflineTestManager extends AbstractContentManager {

	public static UploadOfflineTestResultRes uploadOfflineTestResult(
			UploadOfflineTestResultReq req) throws VedantuException {
		UploadOfflineTestResultRes res = new UploadOfflineTestResultRes();
		res.processed = true;
		res.jobId = OfflineTestUtils.loadOfflineResult(req.userId, req.orgId,
				req.programId, req.resultFile, req.merge);
		return res;
	}

    public static UploadOfflineTestResultRes uploadOfflineTestResult2(UploadOfflineTestResultReq req)
            throws VedantuException {
        UploadOfflineTestResultRes res = new UploadOfflineTestResultRes();
        res.processed = true;
        SrcEntity target = new SrcEntity(
                req.targetType.equalsIgnoreCase("MODULE") ? EntityType.MODULE : EntityType.SECTION,
                req.targetId);
        res.jobId = OfflineTestUtils.loadOfflineResult(req.userId, req.orgId, req.testId,
                req.resultFile, target);
        return res;
    }
}
