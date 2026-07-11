package com.lms.repository;

import com.lms.models.messages.MessageSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageSummaryRepo extends MongoRepository<MessageSummary, String> {
    MessageSummary findByUserMessageIdAndUserId(String userMessageId, String userId);

    MessageSummary findByUserMessageIdAndUserIdAndConversationId(String userMessageId, String userId, String conversationId);

    MessageSummary findByUserIdAndConversationId(String userId, String conversationId);
}
