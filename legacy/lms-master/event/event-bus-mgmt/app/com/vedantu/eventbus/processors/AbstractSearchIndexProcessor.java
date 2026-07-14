package com.vedantu.eventbus.processors;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.utils.ObjectMapperUtils;
import com.vedantu.content.managers.ContentManager;
import com.vedantu.content.search.details.AbstractSearchDetail;
import com.vedantu.content.search.details.UniqueId;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;
import com.vedantu.search.es.ElasticSearchManager;

public abstract class AbstractSearchIndexProcessor implements IProcessor {

    private static final ALogger   LOGGER                                   = Logger.of(AbstractSearchIndexProcessor.class);
    private static long            DEFAULT_THREAD_SLEEP_TIME_AFTER_INDEXING = 2000;
    static {
        try {
            DEFAULT_THREAD_SLEEP_TIME_AFTER_INDEXING = Long.parseLong(Play.application()
                    .configuration().getString("elasticsearch.refresh.time"));
        } catch (Exception e) {
            LOGGER.error("erro on reading elasticsearch.refresh.time property fron conf file,  "
                    + e.getMessage(), e);
        }
    }
    protected AbstractSearchDetail details;
    protected boolean              loadedDetails;
    protected final String         type;                                                                                    // index
                                                                                                                             // _type
    protected final String         searchIndex;                                                                             // index
                                                                                                                             // name

    protected AbstractSearchIndexProcessor(final String type, final String searchIndex) {

        this.type = type;
        this.searchIndex = searchIndex;
    }

    public Status process(IConsumable consumable, AbstractSearchDetail details) {

        return process(consumable, details, true);
    }

    public Status process(IConsumable consumable, AbstractSearchDetail details, boolean shouldLoad) {

        if (!loadedDetails && shouldLoad) {
            boolean loaded = load(consumable, details);
            if (!loaded) {
                return Status.NOT_CONSUMABLE;
            }
        }
        if (loadedDetails) {
            details = this.details;
        }
        // index the event info to elasticsearch
        boolean result = false;
        LOGGER.info("details: " + details);
        LOGGER.info("details action: " + details.getAction());
        EventActionType actionType = EventActionType.valueOfKey(details.getAction());
        if (actionType == EventActionType.ADD) {
            LOGGER.info("adding entity to es for : " + details.getAction());
            result = addToSearchDB(details);
        } else if (actionType == EventActionType.UPDATE) {
            result = updateInSearchBD(details);

        } else if (actionType == EventActionType.REMOVE) {
            result = removeFromSearchDB(details);
        } else {
            LOGGER.error("no action info found in event info details for evenDetails: "
                    + details._getUniqueId());
        }

        return result ? Status.SUCCESS : Status.FAILURE;
    }

    protected boolean load(IConsumable consumable, AbstractSearchDetail details) {

        Event event = (Event) consumable;
        LOGGER.info("processing event for " + this.getClass() + " process for userId :"
                + event.getUserId() + ", eventId : " + event._getStringId());
        try {
            LOGGER.info("loading the details object :" + event._getInfo());
            details.fromJSON(event._getInfo());
            LOGGER.info("event details obj is " + details.toJSON());
        } catch (Exception e) {
            LOGGER.error("could not load from json", e);
            return false;
        }
        return true;
    }

    protected boolean isValid(UniqueId uniqueId) {

        return null != uniqueId && StringUtils.isNotEmpty(uniqueId.getName())
                && StringUtils.isNotEmpty(uniqueId.getValue());
    }

    protected boolean addToSearchDB(AbstractSearchDetail details) {

        return addToSearchDB(details, true);
    }

    protected boolean addToSearchDB(AbstractSearchDetail details, boolean shouldCheckInES) {

        if (!isValid(details._getUniqueId()) || !details._isIndexable()) {
            LOGGER.error(" Details is not valid " + details);
            return false;
        }
        boolean added = false;

        String esRspId = null;
        LOGGER.debug("adding details object to Es for type : " + details._getUniqueId().getName()
                + " and Id: " + details._getUniqueId().getValue());
        UniqueId uniqueId = details._getUniqueId();
        ElasticSearchManager es = ElasticSearchManager.getInstance();
        Map<String, Object> esDetailsMap = getValuesMap(details);
        JSONObject result = !shouldCheckInES ? null : es.getUniqueIndexResult(searchIndex, type,
                uniqueId.getName(), uniqueId.getValue());
        LOGGER.info("result from es.getUniqueIndexResult is : " + result);
        try {
            if (result == null || result.get(ElasticSearchManager.SOURCE) == null) {
                esRspId = es.addIndex(searchIndex, type, esDetailsMap);
                ContentManager.addOrUpdateContentSearchDetails(details);
                LOGGER.debug("putting thread to sleep for : " + details._getUniqueId());
                Thread.sleep(DEFAULT_THREAD_SLEEP_TIME_AFTER_INDEXING);
                LOGGER.info("thread woke up  for : " + details._getUniqueId());
            } else {
                LOGGER.debug("es.getUniqueIndexResult response : "
                        + result.get(ElasticSearchManager.SOURCE));
            }
        } catch (JSONException e) {
            LOGGER.error("source from es is : " + result, e);
        } catch (InterruptedException e) {
            LOGGER.error("can not put thread to sleep mode, " + e.getMessage(), e);
        }

        if (null != esRspId) {
            LOGGER.info(type + " indexed sucessfully, and ES response id : " + esRspId);
            added = true;
        } else if (result != null) {
            LOGGER.error("already index " + type + " in ES : " + details._getUniqueId().getValue());
            added = true;
        } else {
            LOGGER.error("can not index " + type + " to ES : " + details._getUniqueId().getValue());
        }

        if (added) {
            postProcessAddToSearchDB(details);
        }

        return added;
    }

    protected void postProcessAddToSearchDB(AbstractSearchDetail details) {

        // do nothing
    }

    protected boolean removeFromSearchDB(AbstractSearchDetail details) {

        UniqueId uniqueId = details._getUniqueId();
        if (!isValid(uniqueId)) {
            return false;
        }
        ElasticSearchManager es = ElasticSearchManager.getInstance();
        boolean removed = false;

        removed = es.removeEntry(searchIndex, type, uniqueId.getName(), uniqueId.getValue());

        if (removed) {
            LOGGER.info("successfully removed from ES[indexName:" + searchIndex + ", indexType:"
                    + type + "] entityId [" + uniqueId.getValue() + "]");
            ContentManager.removeContentSearchDetails(details);
        } else {
            LOGGER.error("can not remove " + type + " : " + " from ES : " + uniqueId.getValue());
        }

        return removed;
    }

    protected boolean updateInSearchBD(AbstractSearchDetail details) {

        UniqueId uniqueId = details._getUniqueId();
        if (!isValid(uniqueId) || !details._isIndexable()) {
            return false;
        }
        ElasticSearchManager es = ElasticSearchManager.getInstance();
        JSONObject result = es.getUniqueIndexResult(searchIndex, type, uniqueId.getName(),
                uniqueId.getValue());

        if (result == null) {
            LOGGER.error("no object found in es corresponding to : " + uniqueId.getName() + " : "
                    + uniqueId.getValue() + " in, INDEX[ " + searchIndex + "]");
            return addToSearchDB(details, false);
        }

        boolean updated = false;
        try {

            Map<String, Object> esValueMap = getValuesMap(details);
            String esRspId = es.reIndex(searchIndex, esValueMap,
                    result.getString(ElasticSearchManager.ID),
                    result.getString(ElasticSearchManager.TYPE));
            if (null != esRspId) {
                LOGGER.info(type + " reIndexed sucessfully, and ES response id : " + esRspId);
                updated = true;
                ContentManager.addOrUpdateContentSearchDetails(details);
            } else {
                LOGGER.error("can not reIndex " + type + " to ES : "
                        + details._getUniqueId().getValue());
            }
        } catch (JSONException e) {
            LOGGER.error("id or type params missing from, ES reIndex response method", e);
        }
        return updated;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getValuesMap(AbstractSearchDetail details) {

        Map<String, Object> esValueMap = ObjectMapperUtils.convertValue(details, Map.class);
        esValueMap.remove(ConstantsGlobal.USER);// this will be added to the
                                                // model while rendering..
        esValueMap.remove("action");
        esValueMap.remove("isNotificationEnabled");
        esValueMap.remove("boardTree");
        return esValueMap;
    }

}
