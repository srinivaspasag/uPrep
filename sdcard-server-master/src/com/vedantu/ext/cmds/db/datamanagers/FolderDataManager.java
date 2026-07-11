package com.vedantu.ext.cmds.db.datamanagers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.vedantu.ext.cmds.db.models.Folder;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class FolderDataManager extends AbstractDataManager<Folder> {

    private static final String           TABLE    = "folder_info";
    public static final FolderDataManager INSTANCE = new FolderDataManager();

    private FolderDataManager() {

        super(Folder.class);
    }

    public Folder getFolder(String name) {

        Folder folder = null;
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        addStringEqualSQLQuery(ConstantGlobal.NAME, name, sb, false);

        ResultSet rs = rawQuery(sb.toString());
        try {
            if (rs.next()) {
                folder = SQLDBUtils.convertToValues(rs, entityClazz, null);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return folder;
    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    @Override
    protected String getUpsertQueryCondition(Folder model) {

        return "id='" + model.id + "'";
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(ConstantGlobal.ID + " text not null,");
        sb.append(ConstantGlobal.NAME + " text not null,");
        sb.append(ConstantGlobal.C_NAME + " text not null,");
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(Folder.FILED_PARENT + " text");

        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());
        tableQuery.add(createIndexQuery(TABLE, "folder_name_index", true, Folder.FILED_PARENT,
                ConstantGlobal.C_NAME));
        tableQuery.add(createIndexQuery(TABLE, "folder_id_index", true, ConstantGlobal.ID));
    }

}
