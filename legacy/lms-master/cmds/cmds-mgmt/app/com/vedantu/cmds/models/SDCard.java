package com.vedantu.cmds.models;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Transient;
import com.vedantu.cmds.pojos.export.SDCardInfo;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.models.AbstractBoardEntityTagModel;

@Entity(value = "sdcards", noClassnameStored = true)
public class SDCard extends AbstractBoardEntityTagModel {

    @Transient
    public final static String CONTENT_SIZE = "contentSize";
    public final static String TARGET       = "target";
    public final static String GROUP_ID     = "groupId";
    public final static String MAX_SIZE     = "maxSize";
    public final static String ENC_LEVEL    = "encLevel";
    public final static String COUNT        = "count";

    public SrcEntity           target;
    public String              groupId;
    public long                contentSize;
    public long                count;

    public long                maxSize;
    public EncryptionLevel     encLevel;

    @Transient
    private SDCardGroup        group;

    public SDCard() {
        size=null;
        this.count=0;
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
