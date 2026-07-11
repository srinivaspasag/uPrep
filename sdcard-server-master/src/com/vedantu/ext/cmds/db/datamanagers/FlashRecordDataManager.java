package com.vedantu.ext.cmds.db.datamanagers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vedantu.ext.cmds.db.models.FlashRecordInfo;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.db.FieldInfo;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class FlashRecordDataManager extends AbstractDataManager<FlashRecordInfo> {

    private static final String                TABLE    = "flash_records";
    public static final FlashRecordDataManager INSTANCE = new FlashRecordDataManager();

    private FlashRecordDataManager() {

        super(FlashRecordInfo.class);
    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    @Override
    protected String getUpsertQueryCondition(FlashRecordInfo model) {

        StringBuilder sb = new StringBuilder();
        sb.append(FlashRecordInfo.FIELD_SD_CARD_ID);
        sb.append("='");
        sb.append(model.sdCardId);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(FlashRecordInfo.FIELD_GROUP_ID);
        sb.append("='");
        sb.append(model.groupId);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(FlashRecordInfo.FIELD_SECTION_ID);
        sb.append("='");
        sb.append(model.sectionId);
        sb.append("'");
        return sb.toString();
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(FlashRecordInfo.FIELD_SD_CARD_ID + " text not null,");
        sb.append(FlashRecordInfo.FIELD_GROUP_ID + " text not null,");
        sb.append(FlashRecordInfo.FIELD_SECTION_ID + " text not null,");
        sb.append(FlashRecordInfo.FIELD_COUNT + " UNSIGNED BIG INT not null");

        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());
        tableQuery.add(createIndexQuery(TABLE, "flash_record_index", true,
                FlashRecordInfo.FIELD_SD_CARD_ID, FlashRecordInfo.FIELD_GROUP_ID,
                FlashRecordInfo.FIELD_SECTION_ID));
    }

    public void increment(FlashRecordInfo model, long incrementBy) {

        StringBuilder sb = new StringBuilder();

        FieldInfo fieldInfo = new FieldInfo(FlashRecordInfo.FIELD_COUNT,
                FlashRecordInfo.FIELD_COUNT + (incrementBy > 0 ? "+" : "-") + Math.abs(incrementBy));

        createUpdateSetQuery(sb, TABLE, fieldInfo);

        sb.append(KEYWORD_WHERE);
        sb.append(FlashRecordInfo.FIELD_SD_CARD_ID);
        sb.append("='");
        sb.append(model.sdCardId);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(FlashRecordInfo.FIELD_GROUP_ID);
        sb.append("='");
        sb.append(model.groupId);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(FlashRecordInfo.FIELD_SECTION_ID);
        sb.append("='");
        sb.append(model.sectionId);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(ConstantGlobal.ORG_KEY_ID);
        sb.append("='");
        sb.append(model.orgKeyId);
        sb.append("'");

        int count = rawUpdate(sb.toString());
        LOGGER.debug("Updated number of records" + count);

    }

    // public int getFlashCounts(int orgId, String groupId, String sdCardId,String sectionId,String
    // groupBy) {
    //
    // StringBuilder sb = new StringBuilder();
    // List<String> columns = new ArrayList<String>();
    //
    // addSelectQuery(sb, null,"sum(count)");
    // sb.append(KEYWORD_FROM);
    // sb.append(TABLE);
    // sb.append(" ");
    // List<FieldInfo> fields = new ArrayList<FieldInfo>();
    // if (StringUtils.isNotEmpty(groupId)) {
    // fields.add(new FieldInfo(FlashRecordInfo.FIELD_GROUP_ID, groupId));
    // columns.add("groupId");
    // }
    // if (StringUtils.isNotEmpty(groupId)) {
    // fields.add(new FieldInfo(FlashRecordInfo.FIELD_SD_CARD_ID, sdCardId));
    // }
    //
    // if (StringUtils.isNotEmpty(sectionId)) {
    // fields.add(new FieldInfo(FlashRecordInfo.FIELD_SECTION_ID, sectionId));
    // }
    //
    // if (orgId != -1) {
    // fields.add(new FieldInfo("orgKeyId", Integer.toString(orgId)));
    // }
    //
    // createWhereFields(sb, TABLE, fields.toArray(new FieldInfo[fields.size()]));
    // createGroupBy(sb,groupBy );
    //
    // ResultSet resultSet = this.rawQuery(sb.toString());
    // ResultSet rs = rawQuery(sb.toString());
    // try {
    // while (rs.next()) {
    // return rs.getInt(0);
    //
    // }
    // } catch (SQLException e) {
    // LOGGER.error(e.getMessage(), e);
    // } finally {
    // closeResultSet(rs);
    //
    // }
    // return 0;
    // }

    public Map<String, Integer> getFlashCounts(int orgId, List<String> groupIds,
            List<String> sdCardIds, List<String> sectionIds, String groupBy) {

        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, null, "sum(count) as flashCount," + groupBy);
        sb.append(KEYWORD_FROM);
        sb.append(TABLE);
        sb.append(" ");
        List<FieldInfo> fields = new ArrayList<FieldInfo>();
        if (groupIds != null && !groupIds.isEmpty()) {

            fields.add(new FieldInfo(FlashRecordInfo.FIELD_GROUP_ID, groupIds));
        }
        if (sdCardIds != null && !sdCardIds.isEmpty()) {
            fields.add(new FieldInfo(FlashRecordInfo.FIELD_SD_CARD_ID, sdCardIds));
        }

        if (sectionIds != null && sectionIds.isEmpty()) {
            fields.add(new FieldInfo(FlashRecordInfo.FIELD_SECTION_ID, sectionIds));
        }

        if (orgId != -1) {
            fields.add(new FieldInfo("orgKeyId", Integer.toString(orgId)));
        }

        createWhereFields(sb, TABLE, fields.toArray(new FieldInfo[fields.size()]));
        createGroupBy(sb, groupBy);

        ResultSet rs = rawQuery(sb.toString());
        Map<String, Integer> resourceCounts = new HashMap<String, Integer>();

        try {
            while (rs.next()) {

                resourceCounts.put(rs.getString(groupBy), Integer.parseInt(rs.getNString("count")));

            }
            return resourceCounts;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);

        }
        return resourceCounts;
    }

    public int getFlashCount(int orgId, String groupIds, String sdCardId, String sectionId,
            String groupBy) {

        final String FLASH_COUNT = "flashCount";
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, Arrays.asList("sum(count) as " + FLASH_COUNT).toArray(new String[1]),
                null);

        sb.append(" ");
        List<FieldInfo> fields = new ArrayList<FieldInfo>();
        if (groupIds != null && !groupIds.isEmpty()) {

            fields.add(new FieldInfo(FlashRecordInfo.FIELD_GROUP_ID, groupIds));
        }
        if (sdCardId != null && !sdCardId.isEmpty()) {
            fields.add(new FieldInfo(FlashRecordInfo.FIELD_SD_CARD_ID, sdCardId));
        }

        if (sectionId != null && sectionId.isEmpty()) {
            fields.add(new FieldInfo(FlashRecordInfo.FIELD_SECTION_ID, sectionId));
        }

        if (orgId != -1) {
            fields.add(new FieldInfo("orgKeyId", Integer.toString(orgId)));
        }

        createWhereFields(sb, TABLE, fields.toArray(new FieldInfo[fields.size()]));
        createGroupBy(sb, groupBy);

        ResultSet rs = rawQuery(sb.toString());

        try {
            while (rs.next()) {

                return rs.getInt(FLASH_COUNT);

            }

        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);

        }
        return 0;
    }

    public FlashRecordInfo getFlashRecord(int orgId, String groupId, String sdCardId,
            String sectionId) {

        FlashRecordInfo flash_record_info = null;

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);

        createWhereFields(sb, KEYWORD_AND,
                new FieldInfo(ConstantGlobal.ORG_KEY_ID, Integer.toString(orgId)), new FieldInfo(
                        FlashRecordInfo.FIELD_GROUP_ID, groupId), new FieldInfo(
                        FlashRecordInfo.FIELD_SECTION_ID, sectionId), new FieldInfo(
                        FlashRecordInfo.FIELD_SECTION_ID, sectionId));

        ResultSet rs = rawQuery(sb.toString());
        try {
            if (rs.next()) {
                flash_record_info = SQLDBUtils.convertToValues(rs, entityClazz, null);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("Flash record found "+ flash_record_info);
        return flash_record_info;
    }
}
