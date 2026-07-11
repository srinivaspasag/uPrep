package com.vedantu.cmds.daos;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.vedantu.cmds.models.SDCard;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.VedantuBasicDAO;

public class SDCardDAO extends VedantuBasicDAO<SDCard, ObjectId> {

    private static final String ADD_SIGN = "+";

    private static final ALogger  LOGGER   = Logger.of(SDCardGroupDAO.class);

    public static final SDCardDAO INSTANCE = new SDCardDAO();

    public SDCardDAO() {

        super(SDCard.class);
    }

    public List<SDCard> getSDCards(String id, String orgId, String groupId, int start, int size,
            MutableLong totalHits) {

        Query<SDCard> sdCardsQuery = getQuery();
        if (StringUtils.isNotEmpty(orgId)) {
            sdCardsQuery=     sdCardsQuery.field("contentSrc.id").equal(orgId);
        }

        if (StringUtils.isNotEmpty(orgId)) {
            sdCardsQuery=  sdCardsQuery.field(SDCard.GROUP_ID).equal(groupId);
        }

        if (StringUtils.isNotEmpty(id)) {

            sdCardsQuery=     sdCardsQuery.field(FIELD_ID).equal(new ObjectId(id));
        }
        totalHits.setValue(sdCardsQuery.countAll());
        LOGGER.debug("Query "+ sdCardsQuery.toString()+ "   "+ totalHits.longValue());
        return sdCardsQuery.offset(start).limit(size).asList();
    }

    public SDCard getSDCard(String id, String orgId, String groupId) {

        
        List<SDCard> cards = getSDCards(id, orgId, groupId,0,1,new MutableLong());
        return cards.size() > 0 ? cards.get(0) : null;
    }

    public boolean addSize(List<String> ids, boolean remove, long size) {

        Query<SDCard> sdCardQuery = getQuery();
        sdCardQuery = sdCardQuery.field(FIELD_ID).in(ObjectIdUtils.toObjectIds(ids));
        String sign = ADD_SIGN;
        if (remove) {
            size = -size;
            sign = StringUtils.EMPTY;
        }

        sdCardQuery = sdCardQuery.where("this."+SDCard.CONTENT_SIZE + sign + size + ">=0");
        UpdateOperations<SDCard> updateOperations = getDS().createUpdateOperations(
                SDCard.class);
        LOGGER.debug("SectionQuery" + sdCardQuery.toString() + " size " + size);

        updateOperations = updateOperations.inc(SDCard.CONTENT_SIZE, size);

        UpdateResults<SDCard> sectionUpdates = getDS().update(sdCardQuery, updateOperations);

        if (sectionUpdates.getHadError()) {
            return false;
        }
        return true;
    }

}
