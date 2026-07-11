package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter

@Document(value = "usermailboxinfo")
@CompoundIndexes(@CompoundIndex(name = "userId", unique = true))
public class UserMailBoxInfo extends VedantuBaseMongoModel {

    public String userId;
    public long conversationCount;
    public long unreadConversationCount;
    public long sentCount;


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
