package com.vedantu.cmds.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.cmds.daos.SDCardDAO;
import com.vedantu.cmds.pojos.export.SDCardGroupExtendedInfo;
import com.vedantu.cmds.pojos.export.SDCardGroupInfo;
import com.vedantu.cmds.pojos.export.SDCardInfo;
import com.vedantu.commons.content.interfaces.ISellableEntity;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.pojos.SellableItemDetails;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;
import com.vedantu.user.daos.UserDAO;

@Entity(value = "cardgroups", noClassnameStored = true)
public class SDCardGroup extends BaseRecord implements ISellableEntity {

    @Transient
    public static final String CARDS        = "cards";
    @Transient
    public static final String ACCESS_SCOPE = "accessScope";
    @Transient
    public static final String COST_RATE    = "costRate";

    public List<String>        cards;

    public AccessScope         accessScope;

    public CostRate            costRate;

    public long                cardSize;

    public RevenueModel        revenueModel = RevenueModel.PAID;

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
        if (StringUtils.isNotEmpty(userId)) {
            groupInfo.userInfo = UserDAO.INSTANCE.getBasicInfo(userId);
        }
        groupInfo.target = target;

        return groupInfo;
    }

    @Override
    public ModelExtendedInfo toExtendedInfo() {

        SDCardGroupExtendedInfo groupInfo = new SDCardGroupExtendedInfo(_getStringId(),
                recordState, name, cardSize, cards, accessScope, size.getTotalSize(), timeCreated,
                lastUpdated, costRate);
        if (StringUtils.isNotEmpty(userId)) {
            groupInfo.userInfo = UserDAO.INSTANCE.getBasicInfo(userId);
        }
        groupInfo.cardInfos = new ArrayList<SDCardInfo>();

        List<SDCard> cards = SDCardDAO.INSTANCE.getByIds(ObjectIdUtils.toObjectIds(this.cards));
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
            return StringUtils.EMPTY;
        }
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equalsIgnoreCase(cardId)) {
                return "Card" + "-" + (i + 1);
            }
        }
        return StringUtils.EMPTY;

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
}
