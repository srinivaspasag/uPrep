package com.vedantu.comm.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

import play.Logger;
import play.Logger.ALogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.hbase.AbstractHbaseModels;
import com.vedantu.commons.hbase.HBaseUtil;

public class HbaseTableWrapper<E> {

    private static final ALogger LOGGER                              = Logger.of(HbaseTableWrapper.class);
    private static final String  UNIFORM_LENGTH_FORMATTER_FOR_TIME   = "%020d";
    private static final int     HBASE_ROWKEY_MILLISECOND_TIME_INDEX = 1;
    private static final int     HBASE_ROWKEY_PREFIX_INDEX           = 0;
    private static final String  HBASE_KEY_SEPARATOR                 = "_";
    private Class<?>             clazz;
    Gson                         gson                                = null;
    transient private HTable     hbaseTable                          = null;
    String                       tableName                           = null;

    private void init() {

        hbaseTable = HBaseUtil.getTable(tableName);

    }

    private void destroy() {

        if (hbaseTable != null) {
            HBaseUtil.returnTable(hbaseTable);
        }
    }

    // private final static long maximumLookbackTimeInMS = Integer.parseInt(
    // Play.configuration.getProperty( "vedantu.newsfeed.lookbackdays") )
    // * DateUtils.MILLIS_PER_DAY; // TODO check this limit

    public HbaseTableWrapper(String tableName, Class<?> clazz) {

        this.gson = new Gson();
        this.tableName = tableName;
        this.clazz = clazz;
    }

    public void
            addData(String rowKey, String columnFamily, String column, AbstractHbaseModels data)
                    throws VedantuException {

        Map<String, AbstractHbaseModels> rowKeyMap = new HashMap<String, AbstractHbaseModels>();
        rowKeyMap.put(rowKey, data);
        addData(rowKeyMap, columnFamily, column);

    }

    private void addData(Map<String, AbstractHbaseModels> rowKeyModelMap, String columnFamily,
            String column) throws VedantuException {

        try {
            init();
            List<Put> puts = new ArrayList<Put>();
            for (String rowKey : rowKeyModelMap.keySet()) {
                LOGGER.debug(" Adding data to " + this.tableName + "  for rowKey:" + rowKey
                        + " ColumnFamily: " + columnFamily + " Column:" + column);
                Put put = new Put(Bytes.toBytes(rowKey));
                AbstractHbaseModels data = rowKeyModelMap.get(rowKey);
                if (data == null) {
                    continue;
                }
                Gson gson = new GsonBuilder().setExclusionStrategies(
                        new HBaseModelExclusionStrategy()).create();
                put.add(Bytes.toBytes(columnFamily), Bytes.toBytes(column), data.getTimestamp(),
                        Bytes.toBytes(gson.toJson(data)));
                puts.add(put);
            }

            this.hbaseTable.put(puts);
        } catch (IOException exp) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);

        } finally {
            destroy();
        }
    }

    public void update(String oldKey, Row row, String columnFamily, String column, Object data)
            throws VedantuException {

        // try {
        // LOGGER.debug(" Adding data to " + this.tableName
        // + "  for rowKey:" + rowKey + " ColumnFamily: "
        // + columnFamily + " Column:" + column);
        // Put newPut = new Put(Bytes.toBytes(rowKey));
        // Gson gson = new GsonBuilder().setExclusionStrategies( new
        // HBaseModelExclusionStrategy() ).create();
        // newPut.add(Bytes.toBytes(columnFamily), Bytes.toBytes(column),
        // Bytes.toBytes(gson.toJson(data)));
        // this.hbaseTable.put(newPut);
        //
        // RowLock rowLock = hbaseTable.lockRow(oldKey.getBytes());
        // Delete put = new Delete(Bytes.toBytes(rowKey),rowLock);
        //
        // Gson gson = new GsonBuilder().setExclusionStrategies( new
        // HBaseModelExclusionStrategy() ).create();
        // put.add(Bytes.toBytes(columnFamily), Bytes.toBytes(column),
        // Bytes.toBytes(gson.toJson(data)));
        // this.hbaseTable.put(put);
        //
        // } catch (IOException exp) {
        // throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        // }
    }

    public boolean delete(List<String> rowKeys) throws VedantuException {

        List<Delete> deletes = new ArrayList<Delete>();
        for (String rowKey : rowKeys) {
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            deletes.add(delete);
        }

        try {
            init();
            this.hbaseTable.delete(deletes);

            return true;
        } catch (IOException exp) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        } finally {
            destroy();
        }
    }

    public boolean delete(String rowKey) throws VedantuException {

        Delete delete = new Delete(Bytes.toBytes(rowKey));
        try {
            init();
            this.hbaseTable.delete(delete);
            LOGGER.debug(" Deleted data from " + this.tableName + "  for rowKey:" + rowKey);
            return true;
        } catch (IOException exp) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        } finally {
            destroy();
        }
    }

    public boolean doesExist(final String rowKey) throws VedantuException {

        Get get = new Get(Bytes.toBytes(rowKey));
        try {
            init();
            return this.hbaseTable.exists(get);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        } finally {
            destroy();
        }
    }

    public E getNextData(final String rowKey, String columnFamily, String qualifier,
            String rowPrefix) throws VedantuException {

        List<E> list = getData(rowKey, columnFamily, qualifier, 1, null);
        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public E getExact(final String rowKey, String family, String qualifier) throws VedantuException {

        LOGGER.info(" Getting a data from " + this.tableName + "  for rowKey:" + rowKey
                + " ColumnFamily: " + family + " Column:" + qualifier);
        E obj = null;
        Get get = null;
        if (rowKey != null) {
            get = new Get(Bytes.toBytes(rowKey));

            try {
                init();
                Result result = this.hbaseTable.get(get);

                byte[] resultValue = result.getValue(Bytes.toBytes(family),
                        Bytes.toBytes(qualifier));
                if (resultValue != null) {
                    obj = (E) this.gson.fromJson(new String(resultValue), this.clazz);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOGGER.error("Retrieval failed", e);
                throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
            }
            finally {
                destroy();
            }
            LOGGER.info("Retrived :" + obj);
        }
        return obj;
    }

    public List<E> getData(final String rowKey, final String columnFamily, final String qualifier,
            final int count, final String rowPrefix) throws VedantuException {

        return getData(rowKey, columnFamily, qualifier, count, rowPrefix, false);

    }

    public List<E>
            getListedData(final String rowKey, final String columnFamily, final String qualifier,
                    final int count, final String rowPrefix, List<String> rowKeyList)
                    throws VedantuException {

        return getRowListedData(rowKey, columnFamily, qualifier, count, rowPrefix, false,
                rowKeyList);

    }

    public List<E> getDataRegex(final String rowKey, String columnFamily, String qualifier,
            int count, String regexRowPrefix) throws VedantuException {

        return getData(rowKey, columnFamily, qualifier, count, regexRowPrefix, true);

    }

    private List<E> getRowListedData(final String rowKey, final String columnFamily,
            final String qualifier, final int count, final String rowPrefix,
            final boolean isRowPrefixRegex, List<String> rowKeyList) throws VedantuException {

        LOGGER.info(" Getting a data from " + this.tableName + " for rowKey:" + rowKey
                + " ColumnFamily: " + columnFamily + " Column:" + qualifier + " count: " + count
                + " rowPrefix " + rowPrefix + " isRowRegex" + isRowPrefixRegex);

        // this will ensure that start row will be excluded from search
        // our use cases are such 1) exact rowKey is provided 2) similar partial key is provided so
        // in both cases we
        // can exclude start rows
        Scan scan = new Scan();

        if (StringUtils.isNotEmpty(rowKey)) {
            byte[] rowBytes = Arrays.copyOf(rowKey.getBytes(), rowKey.getBytes().length + 1);
            scan.setStartRow(rowBytes);
        }
        //
        //        scan.setBatch(count);
        //        scan.setCaching(count);

        FilterList filterList = new FilterList();
        PageFilter pageFilter = new PageFilter(count);
        filterList.addFilter(pageFilter);

        if (StringUtils.isNotEmpty(rowPrefix)) {
            if (isRowPrefixRegex) {

                RowFilter regexRowFilter = new RowFilter(CompareOp.EQUAL,
                        new RegexStringComparator(rowPrefix));
                filterList.addFilter(regexRowFilter);
            } else {
                if (rowPrefix != null) {
                    PrefixFilter rowPrefixFilter2 = new PrefixFilter(rowPrefix.getBytes());
                    filterList.addFilter(rowPrefixFilter2);
                }
            }
        }
        // if (rowKey != null) {
        // LOGGER.debug(" Adding Skip filter with rowKey" + rowKey);
        // SkipFilter filter = new SkipFilter(new RowFilter(CompareOp.EQUAL, new BinaryComparator(
        // rowKey.getBytes())));
        // filterList.addFilter(filter);
        // }

        scan.setFilter(filterList);

        return getData(scan, columnFamily, qualifier, rowKeyList);

    }

    public List<E> getData(final String rowKey, final String columnFamily, final String qualifier,
            final int count, final String rowPrefix, final boolean isRowPrefixRegex)
            throws VedantuException {

        return getRowListedData(rowKey, columnFamily, qualifier, count, rowPrefix,
                isRowPrefixRegex, null);

    }

    public List<E> getDataWithTimeRange(final String rowKey, String columnFamily, String qualifier,
            int count, String rowPrefix, boolean isRowPrefixRegex, long minTimestamp,
            long maxTimestamp) throws VedantuException, IOException {

        LOGGER.info(" Getting a data from " + this.tableName + " for rowKey:" + rowKey
                + " ColumnFamily: " + columnFamily + " Column:" + qualifier + " count: " + count
                + " rowPrefix " + rowPrefix + " isRowRegex" + isRowPrefixRegex
                + " \n minTimeStamp " + minTimestamp + " \n maxTimestamp " + maxTimestamp);

        Scan scan = new Scan();
        if (rowKey != null) {
            byte[] rowBytes = Arrays.copyOf(rowKey.getBytes(), rowKey.getBytes().length + 1);
            scan.setStartRow(rowBytes);
        }

        // scan.setBatch(count);

        // scan.setCaching(count);

        if (minTimestamp < 0) {
            minTimestamp = 0;
        }
        if (maxTimestamp < 0) {
            maxTimestamp = 0;

        }
        scan.setTimeRange(minTimestamp, maxTimestamp);
        FilterList filterList = new FilterList();
        // PageFilter pageFilter = new PageFilter(count);
        scan.setBatch(count);

        if (StringUtils.isNotEmpty(rowPrefix)) {
            if (isRowPrefixRegex) {

                RowFilter regexRowFilter = new RowFilter(CompareOp.EQUAL,
                        new RegexStringComparator(rowPrefix));
                filterList.addFilter(regexRowFilter);
            } else {
                if (rowPrefix != null) {
                    PrefixFilter rowPrefixFilter2 = new PrefixFilter(rowPrefix.getBytes());
                    filterList.addFilter(rowPrefixFilter2);
                }
            }
        }

        // filterList.addFilter(pageFilter);

        scan.setFilter(filterList);
        return getData(scan, columnFamily, qualifier, null);
    }

    public List<E> getData(final String rowKey, final String columnFamily, final String qualifier,
            final Integer count) throws VedantuException {

        LOGGER.info(" Getting a data from " + this.tableName + " for rowKey:" + rowKey
                + " ColumnFamily: " + columnFamily + " Column:" + qualifier + " count: " + count);

        Scan scan = new Scan();
        if (rowKey != null) {
            byte[] rowBytes = Arrays.copyOf(rowKey.getBytes(), rowKey.getBytes().length + 1);
            scan.setStartRow(rowBytes);
        }
        if (count != null) {
            scan.setBatch(count);
        }

        return getData(scan, columnFamily, qualifier, null);

    }

    @SuppressWarnings("unchecked")
    private List<E> getData(Scan scan, String columnFamily, String qualifier,
            List<String> rowKeyList) throws VedantuException {

        ResultScanner s;
        List<E> objList = new ArrayList<E>();
        try {
            init();
            s = this.hbaseTable.getScanner(scan);
            E obj = null;

            for (Result r : s) {
                String key = new String(r.getRow());

                try {
                    obj = (E) this.gson.fromJson(
                            new String(r.getValue(Bytes.toBytes(columnFamily),
                                    Bytes.toBytes(qualifier))), this.clazz);
                    List<KeyValue> values = r.list();

                    LOGGER.info("TimeStamp value" + values.get(0).getTimestamp());
                    if (obj != null) {
                        if (rowKeyList != null) {
                            rowKeyList.add(key);
                        }
                        objList.add(obj);
                    }

                } catch (JsonSyntaxException exp) {
                    LOGGER.info("Invalid JSON for notice with rowId: " + new String(r.getRow()));
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        } finally {
            destroy();
        }

        return objList;
    }

    public boolean delete(String rowKey, String rowPattern) throws VedantuException {

        try {
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            hbaseTable.delete(delete);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);
        } finally {
            destroy();
        }
        return false;
    }

    public long getTimeFromKey(String rowKey) {

        String[] splittedKey = rowKey.split(HBASE_KEY_SEPARATOR);
        return Long.parseLong(splittedKey[HBASE_ROWKEY_MILLISECOND_TIME_INDEX]);
    }

    public String getPrefixKey(String rowKey) {

        String[] splittedKey = rowKey.split(HBASE_KEY_SEPARATOR);
        return splittedKey[HBASE_ROWKEY_PREFIX_INDEX];
    }
}
