package com.vedantu.ext.cmds.db.datamanagers;

import com.vedantu.ext.cmds.db.models.Resource;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceDataManager extends AbstractDataManager<Resource> {

    private static final String       TABLE    = "resource_info";

    public static ResourceDataManager INSTANCE = new ResourceDataManager();

    private ResourceDataManager() {

        super(Resource.class);
    }

    public Resource getResource(String name, String type) {

        Resource resource = null;
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        if (addStringEqualSQLQuery(ConstantGlobal.NAME, name, sb, false) && type != null) {
            sb.append(KEYWORD_AND);
        }
        addStringEqualSQLQuery(ConstantGlobal.TYPE, type, sb, false);

        ResultSet rs = rawQuery(sb.toString());
        try {
            if (rs.next()) {
                resource = SQLDBUtils.convertToValues(rs, entityClazz, null);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return resource;
    }

    public Map<String, Integer> getResourceCount(List<String> targetIds, String targetType) {

        StringBuilder sb = new StringBuilder();

        sb.append("select targetId, count(*) from " + TABLE + KEYWORD_WHERE + "targetId")
                .append(SQLDBUtils.prepareStringsForInQuery(KEYWORD_IN, targetIds))
                .append(KEYWORD_AND).append(" targetType=")
                .append("\'").append(targetType).append("\'").append(" ").append(KEYWORD_GROUPBY).append(" targetId");

        ResultSet rs = rawQueryUnprepared(sb.toString());

        Map<String, Integer> resourceCounts = new HashMap<String, Integer>();
        try {
            LOGGER.debug("ResultSet" + rs.getFetchSize());

            while (rs.next()) {
                LOGGER.debug("Result set values" + rs.getString(0));
                resourceCounts.put(rs.getNString(Resource.TARGET_ID),
                        Integer.parseInt(rs.getNString("count")));
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);
        }
        LOGGER.debug("Values " + resourceCounts.size());
        return resourceCounts;
    }

    public int getResourceCount(String targetId, String targetType) {

        StringBuilder sb = new StringBuilder();

        sb.append("select  count(*) from " + TABLE + KEYWORD_WHERE + "targetId = ? " + KEYWORD_AND
                + " targetType= ? " + " " + KEYWORD_GROUPBY + " targetId");
        List<Object> values = new ArrayList<Object>();
        values.add(targetId);
        values.add(targetType);

        ResultSet rs = rawQuery(sb.toString(), values);

        try {
            LOGGER.debug("ResultSet size is" + rs.getFetchSize());

            while (rs.next()) {
                LOGGER.debug("Result set values" + rs.getInt(1));
                return rs.getInt(1);

            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);
        }

        return 0;
    }

    public List<Resource> getResources(String targetId, String targetType, String includeType) {

        List<Resource> resources = new ArrayList<Resource>();
        if (StringUtils.isEmpty(targetId) || StringUtils.isEmpty(targetType)) {
            return resources;
        }

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);

        addStringEqualSQLQuery(ConstantGlobal.TARGET_ID, targetId, sb, false);

        sb.append(KEYWORD_AND);

        addStringEqualSQLQuery(ConstantGlobal.TARGET_TYPE, targetType, sb, false);

        if (!StringUtils.isEmpty(includeType)) {
            sb.append(KEYWORD_AND);
            addStringEqualSQLQuery(ConstantGlobal.TYPE, includeType, sb, false);
        }

        addLimitFilter(sb, ConstantGlobal.C_NAME, SQLDBUtils.ORDER_ASC, 0, SQLDBUtils.NO_LIMIT);
        ResultSet rs = rawQuery(sb.toString());
        try {
            while (!rs.isAfterLast() && rs.next()) {
                resources.add(SQLDBUtils.convertToValues(rs, entityClazz, null));
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        closeResultSet(rs);

        return resources;
    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(ConstantGlobal.ID + " text not null,");
        sb.append(ConstantGlobal.TYPE + " text not null,");
        sb.append(ConstantGlobal.NAME + " text not null,");
        sb.append(ConstantGlobal.C_NAME + " text not null,");
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(ConstantGlobal.TARGET_ID + " text not null,");
        sb.append(ConstantGlobal.TARGET_TYPE + " text not null,");
        sb.append(ConstantGlobal.SUB_TYPE + " text,");
        sb.append(ConstantGlobal.THUMBNAIL + " text,");
        sb.append(ConstantGlobal.SIZE + " text,");
        sb.append("extraInfo" + " text");

        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());
        tableQuery.add(createIndexQuery(TABLE, "resource_name_index", true,
                ConstantGlobal.TARGET_ID, ConstantGlobal.TARGET_TYPE, ConstantGlobal.C_NAME,
                ConstantGlobal.TYPE));
    }

    @Override
    protected String getUpsertQueryCondition(Resource model) {

        return "targetId='" + model.targetId + "' and targetType='" + model.targetType + "' and "
                + ConstantGlobal.C_NAME + "='" + model.cName + "'";
    }

}
