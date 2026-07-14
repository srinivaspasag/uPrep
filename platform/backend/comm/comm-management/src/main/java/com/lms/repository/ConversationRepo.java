package com.lms.repository;

import com.lms.models.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepo extends MongoRepository<Conversation, String> {
    Conversation findByConversationIdAndUserId(String conversationId, String userId);

    Conversation findByConversationId(String conversationId);

    //  MessageSummary findByUserMessageIdAndUserIdAndConversationId(String userMessageId, String userId, String conversationId);

    Conversation findByUserIdAndConversationId(String userId, String conversationId);

    Conversation findByIdAndUserId(String conversationId, String userId);

    Conversation findByUserIdAndFirstMesssageId(String userId, String userMessageId);

    // MessageSummary findByUserMessageIdAndUserId(String userMessageId, String userId);
}
