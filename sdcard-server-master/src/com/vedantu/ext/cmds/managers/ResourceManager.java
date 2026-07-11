package com.vedantu.ext.cmds.managers;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletException;

import com.vedantu.ext.cmds.db.datamanagers.ResourceDataManager;
import com.vedantu.ext.cmds.db.datamanagers.SyncInfoDataManager;
import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.db.models.Resource;
import com.vedantu.ext.cmds.db.models.SyncInfo;
import com.vedantu.ext.cmds.pojo.responses.GetResourceRes;
import com.vedantu.ext.cmds.pojo.responses.GetResourcesRes;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.db.FieldInfo;
import com.vedantu.ext.cmds.utils.web.WebCommunicator;
import com.vedantu.ext.cmds.web.ReqAction;
import com.vedantu.ext.cmds.web.VedantuHttpResponse;

public class ResourceManager extends AbstractManager {

    private static final int            DEFAULT_FETCH_SIZE = 100;

    public static final ResourceManager INSTANCE           = new ResourceManager();

    private ResourceManager() {

        super();
    }

    public void loadRemoteResources(final Organization org, final String targetId,
            final String targetType, final Map<String, Object> httpParams) throws ServletException {

        try {
            getRemoteResources(org, targetId, targetType, httpParams);
        } catch (ServletException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    public GetResourcesRes getRemoteResources(Organization org, String targetId, String targetType,
            Map<String, Object> httpParams) throws ServletException {

        // add filter for addedAfter params
        httpParams.put("linkType", "ADDED");
        httpParams.put("target.id", targetId);
        httpParams.put("target.type", targetType);
        httpParams.put("addContent", true);
        SyncInfo syncInfo = SyncInfoDataManager.INSTANCE.getSyncInfo(getSyncKey("library",
                Arrays.asList(targetId, targetType)));
        if (syncInfo != null) {
            httpParams.put(FIELD_ADDED_AFTER, syncInfo.syncTime);
        }
        boolean remoteOnly = Boolean.parseBoolean((String) httpParams.get("remoteOnly"));
        if (!remoteOnly) {
            fetchRemovedResources(org, httpParams);
        }
        return fetchRemoteResources(org, httpParams, remoteOnly);

    }

    private void fetchRemovedResources(Organization org, Map<String, Object> httpParams)
            throws ServletException {

        httpParams.remove(ConstantGlobal.START);
        httpParams.remove(ConstantGlobal.SIZE);
        VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.GET_REMOVED_CONTENT_LINKS,
                httpParams);
        checkForErrorResponse(webRes);
        GetResourcesRes res = new GetResourcesRes();
        webRes.populateResult(res);
        for (GetResourceRes rs : res.list) {
            try {

                ResourceDataManager.INSTANCE.delete(new FieldInfo(ConstantGlobal.TARGET_ID,
                        rs.targetId), new FieldInfo(ConstantGlobal.TARGET_TYPE, rs.targetType),
                        new FieldInfo(ConstantGlobal.ID, rs.id));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private GetResourcesRes fetchRemoteResources(Organization org, Map<String, Object> httpParams,
            boolean remoteOnly) throws ServletException {

        return fetchRemoteResources(org, httpParams, 0, remoteOnly);
    }

    private GetResourcesRes fetchRemoteResources(Organization org, Map<String, Object> httpParams,
            int start, boolean remoteOnly) throws ServletException {

        LOGGER.debug("Fetching remote resources");
        httpParams.put(ConstantGlobal.START, start);
        httpParams.put(ConstantGlobal.SIZE, DEFAULT_FETCH_SIZE);
        httpParams.put("orderBy", ConstantGlobal.LAST_UPDATED);
        httpParams.put("sortOrder", "ASC");
        VedantuHttpResponse webRes = WebCommunicator.getResult(ReqAction.GET_CONTENT_LINKS, httpParams);
       
        checkForErrorResponse(webRes);

        GetResourcesRes res = new GetResourcesRes();
        webRes.populateResult(res);
        if (!remoteOnly) {
            for (GetResourceRes rRes : res.list) {
                Resource resource = new Resource(org._id, org.adminUserId, rRes.id, rRes.type,
                        rRes.name,rRes.targetId, rRes.targetType, rRes.timeCreated, rRes.subType,
                        rRes.thumbnail, rRes.size, rRes.extraInfo);
                LOGGER.debug("Fetched resource from web "+ resource.toString());
                try {
                    resource = ResourceDataManager.INSTANCE.upsert(resource);
                    LOGGER.debug("Inserting reosource to db "+ resource.id + "  "+ resource.type);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

        }
        int totalFetchedResultCount = start + DEFAULT_FETCH_SIZE;

        if (res.totalHits > totalFetchedResultCount) {
            GetResourcesRes remaining = fetchRemoteResources(org, httpParams,
                    totalFetchedResultCount, remoteOnly);
            res.list.addAll(remaining.list);
        }

        if (start == 0 && !remoteOnly) {
            String syncKey = getSyncKey(
                    "library",
                    Arrays.asList((String) httpParams.get("target.id"),
                            (String) httpParams.get("target.type")));
            SyncInfo syncInfo = new SyncInfo(org._id, syncKey, res.serverTime);
            try {
                syncInfo = SyncInfoDataManager.INSTANCE.upsert(syncInfo);
            } catch (Exception e) {
                LOGGER.debug("Error fetching sync info failed");
                LOGGER.error(e.getMessage(), e);
            }
        }
        return res;
    }
}
