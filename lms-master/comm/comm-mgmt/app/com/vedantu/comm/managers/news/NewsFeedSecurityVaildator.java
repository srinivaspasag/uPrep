package com.vedantu.comm.managers.news;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.commons.enums.EventType;

public class NewsFeedSecurityVaildator {
	private static List<EventType> publicDisplayableType = new ArrayList<EventType>();
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
	
	public static boolean isPublicEvent( EventType type){
		return publicDisplayableType.contains(type);
		
	}
}
