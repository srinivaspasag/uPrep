package com.vedantu.comm.managers.news;

import java.util.List;

import org.apache.hadoop.hbase.client.Scan;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.pojos.news.NewsFeedInfo;

public class NewsActivityManager {
	private static final ALogger	LOGGER	= Logger.of(NewsAggregator.class);

	// get All corresponding news activity
	public void scanResults(Scan scan, List<NewsFeedInfo> newsFeeds, int maxCount ){
//
//		HTable newsFeedTable = null;
//		try {
//			newsFeedTable = HBaseUtil.getTable(NewsAggregator.TABLE_NAME_NEWSACTIVITY);
//			ResultScanner s = newsFeedTable.getScanner(scan);
//			for (Result r : s) {
//				String rowId = Bytes.toString(r.getRow());
//
//				if (null != excludeRow) {
//					if (StringUtils.equals(excludeRow, rowId)) {
//						LOGGER.info("excluding rowId : " + rowId);
//						continue;
//					}
//				}
//
//				String newsActivityData = Bytes.toString(r.getValue(
//						Bytes.toBytes("act"), Bytes.toBytes("data")));
//				NewsFeedInfo newsFeedInfo = gsonBuilder.create().fromJson(
//						newsActivityData, NewsFeedInfo.class);
//				newsFeedInfo.newsFeedId = null;
//				newsFeedInfo.newsActivityId = rowId;
//
//				newsFeeds.add(newsFeedInfo);
//				foundSomeResult = true;
//
//				if (count != ALL) {
//					count--;
//					if (count == 0) {
//						break;
//					}
//				}
//			}
//		} catch (IOException e) {
//			LOGGER.error("could not consume scan - startRowId : "
//					+ startRowId + ", stopRowId : " + stopRowId, e);
//			// System.out.println("could not consume scan - startRowId : "
//			// + startRowId + ", stopRowId : " + stopRowId);
//			// e.printStackTrace();
//		} finally {
//			HBaseUtil.returnTable(newsFeedTable);
//		}

	}
//	
//	getResults();
//	
//	deleteResults()
	
}
