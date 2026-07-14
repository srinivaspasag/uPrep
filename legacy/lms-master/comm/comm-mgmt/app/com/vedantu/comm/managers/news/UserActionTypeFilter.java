package com.vedantu.comm.managers.news;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import play.Logger;

import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.news.NewsActivity;

public class UserActionTypeFilter implements Filter {
	
	private Set<UserActionType> actionTypes;
	public UserActionTypeFilter(List<UserActionType> filteredTypes ) {
		Logger.info("Created eventTypeFilter :" + filteredTypes  );	
		if( actionTypes != null ){
			actionTypes = new HashSet<UserActionType>();
		}
		actionTypes.addAll(filteredTypes);
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean accept(NewsActivity activity) {
		// TODO Auto-generated method stu
		Logger.info("Applying eventTypeFilters "  );
		if( actionTypes.contains( activity.info.actionType ) )
		{
			return true;
		}
		return false;
	}
}
