package com.lms.user.vedantu.user.model;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.user.vedantu.user.enums.EmailBlacklistAction;
import com.lms.user.vedantu.user.pojo.EmailBlacklistInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "emailblacklists")
@Setter
@Getter
public class EmailBlacklist extends VedantuBaseMongoModel {
    @Indexed(unique = true)
    public String                   email;
    public boolean                  blacklisted;
    public List<EmailBlacklistInfo> infos;

    public EmailBlacklist() {


    }

    public EmailBlacklist(String email) {

        super();
        this.email = email.toLowerCase().trim();
    }

    public EmailBlacklistInfo addBlacklistInfo(String reason, EmailBlacklistAction action) {

        EmailBlacklistInfo info = new EmailBlacklistInfo(reason, action);
        if (this.infos == null) {
            this.infos = new ArrayList<EmailBlacklistInfo>();
        }
        infos.add(info);
        this.blacklisted = info.action == EmailBlacklistAction.BLOCKED;
        return info;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{email:").append(email).append(", blacklisted:").append(blacklisted)
                .append(", infos:").append(infos).append(", id:").append(id)
                .append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
