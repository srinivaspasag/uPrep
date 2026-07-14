package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.models.SDCard;
import com.lms.models.SDCardGroup;
import com.lms.pojos.SDCardInfo;
import com.lms.pojos.requests.GetSDCardReq;
import com.lms.pojos.responses.GetSDCardInfoRes;
import com.lms.repo.SDCardGroupRepo;
import com.lms.services.CMDSSDCardsService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CMDSSDCarrdServiceImpl implements CMDSSDCardsService {
    private static final Logger logger = LoggerFactory.getLogger(CMDSSDCarrdServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private SDCardGroupRepo sdCardGroupRepo;

    @Override
    public VedantuResponse get(GetSDCardReq request) {
        SDCard sdCard = getSDCard(request.id, request.orgId, request.groupId);
        if (sdCard == null) {
            throw new VedantuException(VedantuErrorCode.NOT_VALID_CMDS_CONTENT);
        }
        SDCardGroup group = sdCardGroupRepo.findById(sdCard.groupId).get();

        GetSDCardInfoRes response = new GetSDCardInfoRes();

        SDCardInfo cardInfo = (SDCardInfo) sdCard.toBasicInfo();

        cardInfo.setName(group.__getCardName(sdCard._getStringId()));
        response.recordInfo = cardInfo;
        return new VedantuResponse(response);
    }

    public SDCard getSDCard(String id, String orgId, String groupId) {


        List<SDCard> cards = getSDCards(id, orgId, groupId, 0, 1, new AtomicLong());
        return cards.size() > 0 ? cards.get(0) : null;
    }

    public List<SDCard> getSDCards(String id, String orgId, String groupId, int start, int size,
                                   AtomicLong totalHits) {

        Query query = new Query();
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(orgId)) {
            criteria.and("contentSrc.id").is(orgId);
        }

        if (!StringUtils.isEmpty(orgId)) {
            criteria.and(SDCard.GROUP_ID).is(groupId);
        }

        if (!StringUtils.isEmpty(id)) {
            criteria.and("_id").is(new ObjectId(id));
        }
        query.addCriteria(criteria);
        List<SDCard> sdCards = mongoTemplate.find(query, SDCard.class);
        totalHits.set(sdCards.size());
        logger.debug("Query " + sdCards.toString() + "   " + totalHits.longValue());
        return sdCards;
    }
}
