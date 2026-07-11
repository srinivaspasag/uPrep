package com.vedantu.ext.cmds.db.datamanagers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.vedantu.ext.cmds.db.models.Organization;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.commons.StringUtils;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class OrgDataManager extends AbstractDataManager<Organization> {

    private static final String  TABLE    = "org_info";
    public static OrgDataManager INSTANCE = new OrgDataManager();

    private OrgDataManager() {

        super(Organization.class);
    }

    public Organization getOrganization() {

        return getOrganization(null);
    }

    public Organization getOrganization(String slug) {

        return getOrganization(slug, null);
    }

    public Organization getOrganization(String slug, String orgId) {

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);

        if (StringUtils.isNotEmpty(slug) || StringUtils.isNotEmpty(orgId)) {
            sb.append(KEYWORD_WHERE);
        }

        if (StringUtils.isNotEmpty(slug)) {
            addStringEqualSQLQuery(ConstantGlobal.SLUG, slug, sb, true);
        }

        if (StringUtils.isNotEmpty(orgId)) {
            if (StringUtils.isNotEmpty(slug)) {
                sb.append(KEYWORD_AND);
            }
            addStringEqualSQLQuery(ConstantGlobal.ID, orgId, sb, true);
        }

        ResultSet rs = rawQuery(sb.toString());
        Organization org = null;
        try {
            if (rs.next()) {
                org = SQLDBUtils.convertToValues(rs, entityClazz, null);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        closeResultSet(rs);
        LOGGER.debug("org : " + org);
        return org;
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(ConstantGlobal.ADMIN_USER_ID + " text not null,");
        sb.append(ConstantGlobal.NAME + " text not null,");
        sb.append(ConstantGlobal.THUMB + " text,");
        sb.append(ConstantGlobal.ID + " text not null,");
        sb.append(ConstantGlobal.SLUG + " text not null,");
        sb.append(ConstantGlobal.MAC + " text not null,");
        sb.append(ConstantGlobal.AUTH_TOKEN + " text,");
        sb.append(ConstantGlobal.SECRET_KEY + " text,");
        sb.append(ConstantGlobal.HOST + " text not null,");
        sb.append(ConstantGlobal.KEY + " text");

        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());
        tableQuery.add(createIndexQuery(TABLE, "org_sug_index", true, ConstantGlobal.HOST,
                ConstantGlobal.SLUG));
    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    @Override
    protected String getUpsertQueryCondition(Organization model) {

        return "id='" + model.id + "'";
    }
}
