package com.lms.repository;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.ConversationSummary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationSummaryRepo extends MongoRepository<ConversationSummary, String> {

    ConversationSummary findByConversationIdAndUserId(String conversationId, String userId);

    List<ConversationSummary> findByUserId(String userId);

    ConversationSummary findByConversationIdAndUserIdAndRecordState(String summaryId, String userId,
                                                                    VedantuRecordState active);

    //MessageSummary findByUserMessageIdAndUserId(String userMessageId, String userId);
}
