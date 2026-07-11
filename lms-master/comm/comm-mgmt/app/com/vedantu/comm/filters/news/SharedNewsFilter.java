package com.vedantu.comm.filters.news;

import java.util.List;

import com.vedantu.comm.news.details.NewsEntity;
import com.vedantu.commons.news.NewsActivity;

public class SharedNewsFilter implements NewsActivityFilter {
	
	private List<NewsEntity> sharedWith; 
	@Override
	public boolean accept(NewsActivity newsActivity) {
		// TODO Auto-generated method stub
//		if( newsActivity.eType == EventType.SHARE_ENTITY 
//		&& CollectionUtils.intersection(sharedWith, newsActivity.sharedWith) != null){
//			return true;
//		}
		return false;
	}
}
