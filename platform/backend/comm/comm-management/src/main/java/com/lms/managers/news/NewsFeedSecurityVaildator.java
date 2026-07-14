package com.lms.managers.news;

import com.lms.common.vedantu.enums.EventType;

import java.util.ArrayList;
import java.util.List;


public class NewsFeedSecurityVaildator {
    private static final List<EventType> publicDisplayableType = new ArrayList<EventType>();
    private static final ThreadLocal<UserSecuritySet> securitySet = new ThreadLocal<UserSecuritySet>();

    public static UserSecuritySet get() {
        return securitySet.get();
    }

    public static void set(UserSecuritySet userSecuritySet) {
        securitySet.set(userSecuritySet);
    }

    public static void addPublicEvent(EventType eventType) {
        publicDisplayableType.add(eventType);
    }

    public static void removePublicEvent(EventType eventType) {
        publicDisplayableType.remove(eventType);
    }

    public static boolean isPublicEvent(EventType type) {
        return publicDisplayableType.contains(type);

    }
}
