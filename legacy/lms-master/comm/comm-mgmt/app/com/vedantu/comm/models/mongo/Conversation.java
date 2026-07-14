package com.vedantu.comm.models.mongo;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.comm.pojos.AddedMember;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "conversations", noClassnameStored = true)
public class Conversation extends VedantuBaseMongoModel {

    public String            orgId;
    public String            userId;

    public String            conversationId;   // current conversation id from hbase;

    public String            subject;          // Subject of first message
                                                // in conversation

    public String            recentMessageId;
    public long              recentMessageTime;

    // timings for this message is also attributed at timeCreated for this conversation
    public String            firstMesssageId;

    public List<AddedMember> participants;
    public int               totalParticipants;
    public long              messageCount = 0;

}
