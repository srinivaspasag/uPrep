package com.vedantu.ext.cmds.db.models;

public class FlashRecordInfo extends AbstractDBModel {

    private static final long  serialVersionUID = 1L;

    public static final String FIELD_SD_CARD_ID = "sdCardId";
    public static final String FIELD_COUNT      = "count";
    public static final String FIELD_GROUP_ID   = "groupId";
    public static final String FIELD_SECTION_ID = "sectionId";

    public String              sdCardId;
    public long                count;
    public String              groupId;
    public String              sectionId;

    public FlashRecordInfo(int orgKeyId,String sectionId,String groupId, String sdCardId) {

        super(orgKeyId);
        this.sdCardId = sdCardId;
        this.groupId = groupId;
        this.count = 0;
        this.sectionId = sectionId;

    }

    public FlashRecordInfo() {

        super();
    }

    public String getSdCardId() {

        return sdCardId;
    }

    public void setSdCardId(String sdCardId) {

        this.sdCardId = sdCardId;
    }

    public long getCount() {

        return count;
    }

    public void setCount(long flashCount) {

        this.count = flashCount;
    }

    public String getGroupId() {

        return groupId;
    }

    public void setGroupId(String groupId) {

        this.groupId = groupId;
    }

    public String getSectionId() {

        return sectionId;
    }

    public void setSectionId(String sectionId) {

        this.sectionId = sectionId;
    }

}
