package com.lms.user.vedantu.user.repository;

import com.lms.user.vedantu.user.model.UserEmailUnsubscription;
import com.lms.common.vedantu.enums.MailCategory;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.user.vedantu.user.pojo.UserRestrictedEmailCategory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEmailUnSubScriptionRepo extends MongoRepository<UserEmailUnsubscription, String> {
    UserEmailUnsubscription findByUserIdAndEmail(String username, String email);

    UserEmailUnsubscription findByUserIdAndEmailAndRecordStateAndRestrictionsIn(String targetUserId, String restrictedEmail, VedantuRecordState active, MailCategory all);



    //UserEmailUnsubscription findByUserIdAndEmailAndRestrictionsAndRecordState(String targetUserId, String email, UserRestrictedEmailCategory emailCategory, VedantuRecordState active);


    //UserEmailUnsubscription findByIdAndEmail(ObjectId id, String email);

    UserEmailUnsubscription findByUserIdAndEmailAndRecordState(String targetUserId, String email, VedantuRecordState active);
}
