package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.pojos.SDCardInfo;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "sdcards")

public class SDCard extends AbstractBoardEntityTagModel {
    @Transient
    public final static String CONTENT_SIZE = "contentSize";
    public final static String TARGET = "target";
    public final static String GROUP_ID = "groupId";
    public final static String MAX_SIZE = "maxSize";
    public final static String ENC_LEVEL = "encLevel";
    public final static String COUNT = "count";

    public SrcEntity target;
    public String groupId;
    public long contentSize;
    public long count;

    public long maxSize;
    public EncryptionLevel encLevel;

    @Transient
    private SDCardGroup group;

    public SDCard() {
        size = null;
        this.count = 0;
    }

    public SDCard(SDCardGroup group) {

        this();
        this.group = group;

    }

    public void _setSDCardGroup(SDCardGroup group) {

        this.group = group;
    }

    public void add(long exportableSize) {

        this.contentSize += exportableSize;
        if (group != null) {
            group.size.addOriginal(exportableSize);
        }
    }

    public long getContentSize() {

        return contentSize;
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        SDCardInfo cardInfo = new SDCardInfo(_getStringId(), recordState);
        cardInfo.contentSize = this.contentSize;
        cardInfo.groupId = this.groupId;
        cardInfo.maxSize = this.maxSize;
        cardInfo.target = this.target;
        cardInfo.count = this.count;
        if (group != null) {
            cardInfo.setName(group.__getCardName(_getStringId()));
        }
        return cardInfo;
    }
}
