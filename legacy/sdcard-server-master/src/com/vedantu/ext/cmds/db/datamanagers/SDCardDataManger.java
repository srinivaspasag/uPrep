package com.vedantu.ext.cmds.db.datamanagers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vedantu.ext.cmds.db.models.SDCard;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class SDCardDataManger extends AbstractDataManager<SDCard> {

    private static final String          TABLE    = "sd_cards";

    public static final SDCardDataManger INSTANCE = new SDCardDataManger();

    private SDCardDataManger() {

        super(SDCard.class);
    }

    public List<SDCard> getSDCards(String groupId) {

        List<SDCard> cards = new ArrayList<SDCard>();
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        addStringEqualSQLQuery(SDCard.FIELD_GROUP_ID, groupId, sb, false);
        ResultSet rs = rawQuery(sb.toString());
        try {
            while (rs.next()) {
                SDCard card = SQLDBUtils.convertToValues(rs, SDCard.class, null);
                cards.add(card);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);

        }
        return cards;
    }

    public SDCard getSDCard(String id) {

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        addStringEqualSQLQuery(SDCard.FIELD_ID, id, sb, false);

        ResultSet rs = rawQuery(sb.toString());
        try {
            while (rs.next()) {
                SDCard card = SQLDBUtils.convertToValues(rs, SDCard.class, null);
                return card;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);
        }
        return null;
    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    @Override
    protected String getUpsertQueryCondition(SDCard model) {

        return "id='" + model.id + "'";
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(ConstantGlobal.NAME + " text not null,");
        sb.append(ConstantGlobal.ID + " text not null,");
        sb.append(SDCard.FIELD_SIZE + " text not null,");
        sb.append(SDCard.FIELD_CONTENT_SIZE + " text not null,");
        sb.append(SDCard.FIELD_GROUP_ID + " text not null,");
        sb.append(SDCard.FIELD_COUNT + " UNSIGNED BIG INT not null,");
        sb.append(SDCard.FIELD_DOWNLOADED_SIZE + " UNSIGNED BIG INT not null,");
        sb.append(SDCard.FIELD_DOWNLOADED + " BOOLEAN  not null,");
        sb.append(SDCard.FIELD_STATE + " String not null");

        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());
        tableQuery.add(createIndexQuery(TABLE, "sdcard_index", true, ConstantGlobal.ID));
        tableQuery.add(createIndexQuery(TABLE, "sdcard_group_id_index", false,
                SDCard.FIELD_GROUP_ID));
    }

}
