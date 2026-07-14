package com.vedantu.comm.models.mongo;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "usermailboxinfo", noClassnameStored = true)
@Indexes(@Index(value = "userId", unique=true))
public class UserMailBoxInfo extends VedantuBaseMongoModel {
	
	public String	userId;
	public long		conversationCount;
	public long		unreadConversationCount;
	public long		sentCount;

	public UserMailBoxInfo() {

	}

	public UserMailBoxInfo(String userId) {
		super();
		this.userId = userId;
	}

	public void decrementUnread() {
		if (unreadConversationCount > 0) {
			unreadConversationCount--;
		}
	}

	public void incrementUnread() {

		unreadConversationCount++;

	}

	public void decrementConversationCount() {
		if (conversationCount > 0) {
			conversationCount--;
		}
	}

	public void incrementConversationCount() {
		conversationCount++;
	}

	private void decrementSentCount() {
		if (sentCount > 0) {
			sentCount--;
		}
	}

	public void incrementSentCount() {

		sentCount++;

	}
	
	
}
