package com.lms.common.vedantu.mongo;

import com.lms.common.utils.EventDetailsFactory;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.event.api.IConsumable;
import com.lms.common.vedantu.event.api.IEventDetails;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "events")
public class Event extends VedantuBaseMongoModel implements IConsumable {

    private static final Logger logger = LoggerFactory.getLogger(Event.class);

    private EventType type;
    private String source;
    public String info;
    private String userId;
    public SrcEntity srcEntity;
    public int nTries;
    public Set<String> processedBy;
    public long lastTriedTime;
    public UserActionType.EventActionType action;

    public Event() {

    }

    private Event(EventType type, String source, String userId,
                  SrcEntity srcEntity) {
        super();
        this.type = type;
        this.source = source;
        this.userId = userId;
        if (srcEntity == null) {
            this.srcEntity = new SrcEntity(EntityType.USER, userId);
        } else {
            this.srcEntity = srcEntity;
        }
        this.processedBy = new HashSet<String>();
    }

    public Event(EventType type, String source, String userId,
                 IEventDetails eventDetails, SrcEntity srcEntity)
            throws JSONException {
        this(type, source, userId, srcEntity);
        if (eventDetails != null) {
EventDetailsFactory eventDetailsFactory = EventDetailsFactory.getInstance();
            /*if (!eventDetailsFactory.verify(type,
                    eventDetails.getClass())) {
                throw new IllegalArgumentException("unsupported class : "
                        + eventDetails.getClass() + " for eventType : " + type);
            }*/

        }
        // this.info = eventDetails.fromJSON().toString();
        logger.debug("event info is : " + this.info);
        if (eventDetails.__getSrcEntity() != null) {
            this.srcEntity = eventDetails.__getSrcEntity();
        } else {
            this.srcEntity = new SrcEntity(EntityType.USER, userId);
        }
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public JSONObject _getInfo() {
        try {
            return new JSONObject(info);
        } catch (JSONException e) {
        }
        return null;
    }

    public void _setInfo(JSONObject info) {
        this.info = info.toString();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public IEventDetails fetchEventDetails() {
        IEventDetails details = null;
        EventDetailsFactory eventDetailsFactory = EventDetailsFactory.getInstance();
        try {
            details = eventDetailsFactory.getDetails(type);
            details.fromJSON(new JSONObject(info));
        } catch (JSONException e) {
            logger.error(e.getMessage(), e);
        } catch (SecurityException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return details;
    }

    @Override
    public String _getConsumableId() {
        return this._getStringId();
    }

    @Override
    public Set<String> _getProcessedBy() {
        return processedBy;
    }

    @Override
    public void addProcessedBy(String processor) {
        if (processedBy == null) {
            processedBy = new HashSet<String>();
        }
        if (processor != null) {
            processedBy.add(processor);
        }
    }

    @Override
    public String toString() {
        return "Event [type=" + type + ", source=" + source + ", info=" + info
                + ", userId=" + userId + ", srcEntity=" + srcEntity
                + ", nTries=" + nTries + ", processedBy=" + processedBy
                + ", lastTriedTime=" + lastTriedTime + ", action=" + action
                + ", id=" + id + ", timeCreated=" + timeCreated
                + ", lastUpdated=" + lastUpdated + "]";
    }


}
