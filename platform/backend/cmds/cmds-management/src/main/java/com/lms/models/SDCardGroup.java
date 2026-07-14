package com.lms.models;

import com.lms.common.utils.ObjectIdUtils;
import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.commons.pojos.requests.SellableItemDetails;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.content.ISellableEntity;
import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.RevenueModel;
import com.lms.pojos.SDCardGroupExtendedInfo;
import com.lms.pojos.SDCardGroupInfo;
import com.lms.pojos.SDCardInfo;
import com.lms.repo.SdcardRepo;
import com.lms.user.vedantu.user.model.User;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Document(value = "cardgroups")
public class SDCardGroup extends BaseRecord implements ISellableEntity {
    @Transient
    public static final String CARDS = "cards";
    @Transient
    public static final String ACCESS_SCOPE = "accessScope";
    @Transient
    public static final String COST_RATE = "costRate";
    private static final Logger logger = LoggerFactory.getLogger(SDCardGroup.class);
    public List<String> cards;
    public AccessScope accessScope;
    public CostRate costRate;
    public long cardSize;
    public RevenueModel revenueModel = RevenueModel.PAID;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private SdcardRepo sdcardRepo;

    public SDCardGroup() {

        cards = new ArrayList<String>();
    }

    public SDCardGroup(String name) {

        this();
        this.name = name;

    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        SDCardGroupInfo groupInfo = new SDCardGroupInfo(_getStringId(), recordState, name, cards,
                cardSize, accessScope, size.getTotalSize(), timeCreated, lastUpdated, this.costRate);
        if (!StringUtils.isEmpty(userId)) {
            groupInfo.userInfo = getBasicInfo(userId);
        }
        groupInfo.target = target;

        return groupInfo;
    }

    @Override
    public ModelExtendedInfo toExtendedInfo() {

        SDCardGroupExtendedInfo groupInfo = new SDCardGroupExtendedInfo(_getStringId(),
                recordState, name, cardSize, cards, accessScope, size.getTotalSize(), timeCreated,
                lastUpdated, costRate);
        if (!StringUtils.isEmpty(userId)) {
            groupInfo.userInfo = getBasicInfo(userId);
        }
        groupInfo.cardInfos = new ArrayList<SDCardInfo>();

        List<SDCard> cards = sdcardRepo.findById(ObjectIdUtils.toObjectIds(this.cards));
        if (CollectionUtils.isNotEmpty(cards)) {
            for (SDCard card : cards) {
                card._setSDCardGroup(this);
                groupInfo.cardInfos.add((SDCardInfo) card.toBasicInfo());
            }
        }
        groupInfo.target = target;
        return groupInfo;
    }

    public String __getCardName(String cardId) {

        if (CollectionUtils.isEmpty(cards)) {
            return "";
        }
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equalsIgnoreCase(cardId)) {
                return "Card" + "-" + (i + 1);
            }
        }
        return "";

    }

    @Override
    public SrcEntity _getSeller() {

        return contentSrc == null ? null : new SrcEntity(EntityType.ORGANIZATION, contentSrc.id);
    }

    @Override
    public CostRate _getCostRate() {

        return costRate;
    }

    @Override
    public String _getItemName() {

        return name;
    }

    @Override
    public SellableItemDetails _getSellableItemDetails() {

        return new SellableItemDetails(_getCostRate(), _getItemName(), _getSeller(), new SrcEntity(
                EntityType.SDCARDGROUP, _getStringId()));
    }

    public <B extends ModelBasicInfo> B getBasicInfo(String id) {

        User result = getById(id);
        logger.debug("......in getBasicDAO......" + result + id);
        B basicInfo = null != result ? (B) result.toBasicInfo() : null;

        return basicInfo;
    }

    public User getById(String id) {

        if (ObjectIdUtils.hasInvalidId(id)) {
            return null;
        }
        return getById(new ObjectId(id));
    }

    public User getById(ObjectId id) {

        if (null == id) {
            return null;
        }
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("_id").is(id);
        query.addCriteria(criteria);
        User t = mongoTemplate.findOne(query, User.class);
        return t;
    }
}
