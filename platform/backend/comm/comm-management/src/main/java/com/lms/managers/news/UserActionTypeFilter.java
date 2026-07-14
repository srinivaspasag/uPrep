package com.lms.managers.news;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.enums.UserActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserActionTypeFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(UserActionTypeFilter.class);

    private Set<UserActionType> actionTypes;

    public UserActionTypeFilter(List<UserActionType> filteredTypes) {
        logger.info("Created eventTypeFilter :" + filteredTypes);
        if (actionTypes != null) {
            actionTypes = new HashSet<UserActionType>();
        }
        actionTypes.addAll(filteredTypes);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean accept(NewsActivity activity) {
        // TODO Auto-generated method stu
        logger.info("Applying eventTypeFilters ");
        return actionTypes.contains(activity.info.actionType);
    }


}
