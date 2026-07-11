package com.vedantu.commons.hbase;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.enums.EntityType;

public class HBaseUtil {

    private static ALogger       LOGGER      = Logger.of(HBaseUtil.class);
    private static HTablePool    hTablePool;
    private static Configuration hbaseConfig = null;
    static {
        hbaseConfig = new Configuration();
        // hbaseConfig.addResource(Play.configuration.getProperty("hbase.config.file"));
        // config.addResource(new
        // Path("/usr/local/hbase-0.92.0/conf/hbase-site.xml"));

        // TODO: find the config file from where these hbase properties can be
        // read and add that resource
        hbaseConfig.set("hbase.zookeeper.quorum",
                Play.application().configuration().getString("hbase.zookeeper.quorum"));
        // config.set("hbase.zookeeper.quorum", "localhost");
        hbaseConfig.set("hbase.zookeeper.property.clientPort", Play.application().configuration()
                .getString("hbase.zookeeper.property.clientPort"));
        // config.set("hbase.zookeeper.property.clientPort", "2181");

        Integer maxSize = Integer.parseInt(Play.application().configuration()
                .getString("hbase.tablepool.maxsize"));
        // Integer maxSize = 20;
        hTablePool = new HTablePool(hbaseConfig, maxSize);
    }

    public static Configuration getHbaseConfig() {

        return hbaseConfig;
    }

    public static HTablePool getHTablePool() {

        return hTablePool;
    }

    public static HTable getTable(String tableName) {

        return (HTable) hTablePool.getTable(tableName);
    }

    public static void returnTable(HTable table) {

        if (null != table) {
            try {
                table.close();
            } catch (IOException e) {
                LOGGER.error("table not closed", e);
            }
        }
    }

    private static final int ROW_ID_COMPONENT_COUNT = 3;

    public static boolean isValidRowId(String rowId) {

        String[] tokens = StringUtils.split(rowId, "_");
        if (tokens.length < ROW_ID_COMPONENT_COUNT) {
            LOGGER.error("invalid no. of id-components (" + tokens.length + ") in rowId : " + rowId);
            return false;
        }

        final String entityId = tokens[0];
        final String entityType = tokens[1];
        final String timeValue = tokens[2];

        if (StringUtils.isEmpty(entityId)) {
            LOGGER.error("invalid user in rowId : " + rowId);
            return false;
        }

        if (!EntityType.isValidQualifierChar(entityType)) {
            LOGGER.error("invalid entityType (" + entityType + ") in rowId : " + rowId);
            return false;
        }
        if (StringUtils.isEmpty(timeValue) || !NumberUtils.isNumber(timeValue)) {
            LOGGER.error("invalid timeValue (" + timeValue + ") in rowId : " + rowId);
            return false;
        }

        return true;
    }

    private static final int TIME_VALUE_INDEX = 2;

    public static long getTimeValue(String rowId) {

        LOGGER.debug("time from row " + rowId);
        return Long.MAX_VALUE - Long.parseLong(StringUtils.split(rowId, "_")[TIME_VALUE_INDEX]);
    }

}
