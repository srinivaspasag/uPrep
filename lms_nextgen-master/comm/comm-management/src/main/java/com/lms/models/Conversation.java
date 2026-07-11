package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.pojos.AddedMember;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value = "conversations")
@Getter
@Setter
public class Conversation extends VedantuBaseMongoModel {

	public String orgId;
	public String userId;

	public String conversationId; // current conversation id from hbase;

	public String subject; // Subject of first message
	// in conversation

	public String recentMessageId;
	public long recentMessageTime;

	// timings for this message is also attributed at timeCreated for this
	// conversation
	public String firstMesssageId;

	public List<AddedMember> participants;
	public int totalParticipants;
	public long messageCount = 0;

}
