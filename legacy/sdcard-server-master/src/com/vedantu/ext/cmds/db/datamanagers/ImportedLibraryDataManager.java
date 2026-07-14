package com.vedantu.ext.cmds.db.datamanagers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vedantu.ext.cmds.db.models.ImportedLibrary;
import com.vedantu.ext.cmds.db.models.SDCard;
import com.vedantu.ext.cmds.utils.commons.ConstantGlobal;
import com.vedantu.ext.cmds.utils.db.SQLDBUtils;

public class ImportedLibraryDataManager extends AbstractDataManager<ImportedLibrary> {

    private static final String                    TABLE    = "libraries";
    public static final ImportedLibraryDataManager INSTANCE = new ImportedLibraryDataManager();

    private ImportedLibraryDataManager() {

        super(ImportedLibrary.class);
    }

    @Override
    protected String getTableName() {

        return TABLE;
    }

    public List<ImportedLibrary> getSyncedLibraries(int orgKeyId,String state) {

        List<ImportedLibrary> libraries = new ArrayList<ImportedLibrary>();
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        if( state != null){
          sb.append(KEYWORD_AND);
          addStringEqualSQLQuery(ImportedLibrary.STATE, state, sb,true);
        }
        ResultSet rs = rawQuery(sb.toString());
        try {
            while (rs.next()) {
                ImportedLibrary library = SQLDBUtils.convertToValues(rs, ImportedLibrary.class,
                        null);
                libraries.add(library);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        closeResultSet(rs);
        return libraries;

    }

    public ImportedLibrary getSyncedLibrary(int orgKeyId, String id) {

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, null);
        sb.append(KEYWORD_WHERE);
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        sb.append(KEYWORD_AND);
        addStringEqualSQLQuery(ImportedLibrary.ID, id, sb, true);

        ResultSet rs = rawQuery(sb.toString());
        try {
            while (rs.next()) {
                ImportedLibrary library = SQLDBUtils.convertToValues(rs, ImportedLibrary.class, null);
                return library;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            closeResultSet(rs);
        }
        return null;
    }

    @Override
    protected String getUpsertQueryCondition(ImportedLibrary model) {

        StringBuilder sb = new StringBuilder();
        sb.append(ConstantGlobal.ID);
        sb.append("='");
        sb.append(model.id);
        sb.append("'");
        sb.append(KEYWORD_AND);
        sb.append(ConstantGlobal.TYPE);
        sb.append("='");
        sb.append(model.type);
        sb.append("'");
        return sb.toString();
    }

    public static void createTables(List<String> tableQuery) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(ConstantGlobal.NAME + " text not null,");
        sb.append(ConstantGlobal.ID + " text not null,");
        sb.append(ConstantGlobal.TYPE + " text not null,");
        sb.append(SDCard.FIELD_SIZE + " UNSIGNED BIG INT not null,");
        sb.append(ImportedLibrary.DOWNLOADED_SIZE + " UNSIGNED BIG INT not null,");
        sb.append(ImportedLibrary.DOWNLOADED + " BOOLEAN  not null,");
        sb.append(ImportedLibrary.STATE + " String not null");

        endCreateTableQuery(sb);
        tableQuery.add(sb.toString());
        tableQuery.add(createIndexQuery(TABLE, "imported_library_index", true, ConstantGlobal.ID,
                ConstantGlobal.TYPE));
    }

}
